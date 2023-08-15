

package com.tezov.lib_adr_ui_core.extension

import androidx.compose.runtime.Composable
import com.tezov.lib_kmm_core.extension.ExtensionList.popOrNull
import com.tezov.lib_kmm_core.extension.ExtensionList.push
import com.tezov.lib_kmm_core.extension.ExtensionList.NULL_INDEX

object ExtensionComposable {

    @Composable
    fun <T> List<T>.loopOver(action: @Composable LoopOver<T>.Scope.() -> Unit)
        = LoopOver(data = this, action = action).apply { loop() }

    class LoopOver<T>(
        private var index: Int = NULL_INDEX,
        private val data: List<T>,
        private val action: @Composable LoopOver<T>.Scope.() -> Unit
    ) {
        private var isDone = false
        private val buffer: ArrayDeque<Entry<T>> = ArrayDeque()

        @Composable
        internal fun loop() {
            if (data.isNotEmpty()) {
                Scope().also {
                    while (!isDone) { it.action() }
                }
            }
        }

        data class Entry<T>(val index: Int, val data: T)

        inner class Scope {
            val isStackEmpty get() = buffer.isEmpty()
            val hasReachEnd get() = index >= data.size
            val nextOrNull
                get():Entry<T>? = buffer.popOrNull() ?: run {
                    data.getOrNull(++index)?.let {
                        Entry(index, it)
                    }
                }

            val next
                get():Entry<T>? = buffer.popOrNull() ?: run {
                    Entry(++index, data[index])
                }

            fun push(p: Entry<T>) = buffer.push(p)
            fun done() { isDone = true }
        }
    }
}