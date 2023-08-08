

package com.tezov.lib_adr_ui_core.theme.style

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import com.tezov.lib_adr_ui_core.theme.theme.ThemeColorsExtended
import com.tezov.lib_kmm_core.delegate.DelegateNullFallBack
import androidx.compose.ui.graphics.Color as ColorImport

typealias OutfitTextStateColor = OutfitText.StateColor.Style

object OutfitText {

    object StateColor {

        class StyleBuilder internal constructor(
            style: Style
        ) {
            var typo = style.typo
            var outfitState = style.outfitState

            fun get() = Style(
                typo = typo,
                outfitState = outfitState,
            )
        }

        class Style(
            typo: TextStyle? = null,
            outfitState: OutfitState.Style<ColorImport>? = null,
        ) {

            val typo: TextStyle by DelegateNullFallBack.Ref(
                typo,
                fallBackValue = {
                    ThemeColorsExtended.Dummy.textStyle
                }
            )
            val outfitState: OutfitState.Style<ColorImport> by DelegateNullFallBack.Ref(
                outfitState,
                fallBackValue = { OutfitStateNull() }
            )

            companion object {

                @Composable
                fun Style.copy(
                    scope: @Composable StyleBuilder.() -> Unit = {}
                ) = StyleBuilder(this).also {
                    it.scope()
                }.get()

                inline val OutfitTextStateColor.asPaletteSize: OutfitPaletteSize<OutfitTextStateColor>
                    get() = OutfitPaletteSize(normal = this)

                inline val TextStyle.asTextStateColor: OutfitTextStateColor
                    get() = Style(typo = this)

            }

            constructor(style: Style) : this(
                typo = style.typo,
                outfitState = style.outfitState,
            )

            fun resolve(selector: Any? = null) =
                outfitState.resolve(selector, ColorImport::class)?.let {
                    typo.copy(color = it)
                } ?: typo

        }

    }

}