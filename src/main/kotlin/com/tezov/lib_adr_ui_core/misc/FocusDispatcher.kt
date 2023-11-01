package com.tezov.lib_adr_ui_core.misc

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun rememberFocusDispatcher():FocusDispatcher {
    return remember {
        FocusDispatcher()
    }.also { it.compose() }
}

fun Modifier.focusId(
    id: FocusDispatcher.FocusId,
) = focusRequester(id.value)
    .onFocusChanged {
        if (it.isFocused) id.onFocus()
    }

@OptIn(ExperimentalComposeUiApi::class)
class FocusDispatcher {

    inner class FocusId internal constructor(internal val autoShowKeyboard: Boolean) {

        val value: FocusRequester = FocusRequester()

        fun onFocus() {
            onFocus(this)
        }

        val hasFocus get () = hasFocus(this)

        fun requestFocus() = requestFocus(this)

    }

    private val ids = mutableListOf<FocusDispatcher.FocusId>()
    private var keyboardController: SoftwareKeyboardController? = null
    private lateinit var coroutine: CoroutineScope
    private lateinit var focusManager: FocusManager
    private lateinit var focusOwner: MutableState<FocusDispatcher.FocusId?>

    fun createId(autoShowKeyboard: Boolean = true) = FocusId(autoShowKeyboard).also { ids.add(it) }

    fun destroyId(id: FocusDispatcher.FocusId) = ids.remove(id)

    @Composable
    fun compose() {
        coroutine = rememberCoroutineScope()
        keyboardController = LocalSoftwareKeyboardController.current
        focusManager = LocalFocusManager.current
        focusOwner = remember { mutableStateOf(null) }
    }

    fun showKeyboard() {
        coroutine.launch {
            delay(150)
            keyboardController?.show()
        }
    }

    fun hideKeyboard() {
        keyboardController?.hide()
    }

    fun requestClearFocus() {
        focusOwner.value = null
        focusManager.clearFocus(true)
        hideKeyboard()
    }

    private fun onFocus(id: FocusDispatcher.FocusId) {
        focusOwner.value = id
        if (id.autoShowKeyboard) {
            showKeyboard()
        } else {
            hideKeyboard()
        }
    }

    fun requestFocus(id: FocusDispatcher.FocusId) {
        if (focusOwner.value != id) {
            kotlin.runCatching {
                //TODO no idea why but throw "IllegalStateException" but the focus is obtained anyway ...
                //maybe add focus cemetery same I did in java?
                id.value.requestFocus()
            }
        }
    }

    fun hasFocus(id: FocusDispatcher.FocusId) = focusOwner.value == id

}