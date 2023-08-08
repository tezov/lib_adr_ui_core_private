

package com.tezov.lib_adr_ui_core.animation

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tezov.lib_kmm_core.delegate.DelegateNullFallBack
import com.tezov.lib_kmm_core.type.collection.ListEntry

abstract class AnimationCompound<KEY : Any, STEP_PARENT : Any>(
    private val animators: ListEntry<KEY, Animator<Any>> = ListEntry()
) {

    interface Animator<STEP_PARENT : Any> {

        @Composable
        fun update(transition: Transition<STEP_PARENT>)

        fun animate(modifier: Modifier): Modifier

    }

    interface AnimatorChild<STEP_PARENT : Any, STEP_CHILD : Any>: Animator<STEP_CHILD> {

        fun createStepTransformer(): ((STEP_PARENT) -> STEP_CHILD)
    }

    companion object {
        fun Modifier.animate(animation: Animator<*>) = animation.animate(this)
    }

    var step: MutableTransitionState<STEP_PARENT> by DelegateNullFallBack.Ref(null,
        fallBackValue = { MutableTransitionState(firstStep) })

    val isDone get() = step.currentState == doneStep

    protected abstract val doneStep: STEP_PARENT
    protected abstract val originStep: STEP_PARENT
    protected abstract val firstStep: STEP_PARENT

    @Suppress("UNCHECKED_CAST")
    fun add(key: KEY, animator: Animator<*>) {
        animators.add(key, animator as Animator<Any>)
    }

    fun get(key: KEY): Animator<*>? = animators.getValue(key)

    fun start() {
        with(step) {
            if (isIdle && (currentState == originStep || currentState == doneStep)) {
                targetState = firstStep
            }
        }
    }

    @Composable
    protected abstract fun MutableTransitionState<STEP_PARENT>.updateStep()

    @Composable
    open fun onUpdateTransition() {
    }

    @OptIn(ExperimentalTransitionApi::class)
    @Composable
    fun updateTransition(key: Any = Unit, start: Boolean = true) {
        onUpdateTransition()
        with(step) {
            if (isIdle) { updateStep() }
            updateTransition(this, label = "").also { transition ->
                animators.forEach { entry ->
                    val animator = entry.value
                    @Suppress("UNCHECKED_CAST")
                    val animatorTransition:Transition<Any> = when(animator){
                        is AnimatorChild<*, *> -> {
                            val stepTransformer = remember {
                                val transformer:(@Composable (parentState: STEP_PARENT) -> Any) = {
                                    (animator as AnimatorChild<Any, *>).createStepTransformer().invoke(it)
                                }
                                transformer
                            }
                            transition.createChildTransition(
                                label = "",
                                transformToChildState = stepTransformer
                            )
                        }
                        else -> transition as Transition<Any>
                    }
                    animator.update(transition = animatorTransition)
                }
            }
        }
        LaunchedEffect(key) {
            if (start && !isDone) {
                start()
            }
        }
    }

    class AnimatorIdle<STEP_PARENT:Any>(
        var duration_ms:Int,
        private val transformer: ((STEP_PARENT) -> Boolean)
    ) : AnimatorChild<STEP_PARENT, Boolean> {

        private lateinit var waitFactor: State<Float>

        override fun createStepTransformer(): (STEP_PARENT) -> Boolean = transformer

        @SuppressLint("UnusedTransitionTargetStateParameter")
        @Composable
        override fun update(transition: Transition<Boolean>) {
            waitFactor = transition.animateFloat(
                transitionSpec = { snap(delayMillis = duration_ms) },
                label = ""
            ) { step ->
                when (step) {
                    false -> 0.0f
                    else -> 1.0f
                }
            }
        }

        override fun animate(modifier: Modifier) =
            throw IllegalStateException("Should never be called!")
    }

}