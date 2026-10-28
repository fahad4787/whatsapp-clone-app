package com.aivora.app.ui.icons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens

/**
 * Figma-locked icon chrome (file EWBSJebF5KWRv6BIGzq1VO).
 * Glyph source can be Material / local drawable — plate, tint, size, radius stay fixed.
 *
 * Settings rows use [List]: 24×24, no background, Ink #141414 (Action icon instances).
 * Attach grids use [SheetTile]: SoftMint plate, 16dp radius, Primary glyph.
 */
enum class AivoraIconChrome {
    /** Settings / list — 24dp glyph, no plate, Ink. */
    List,

    /** Toolbar / overflow — 24dp glyph, Ink. */
    Action,

    /** Share/attach grid tile — SoftMint 56dp, radius 16, Primary glyph. */
    SheetTile,

    /** Message overflow row — 36dp Primary soft circle, 18dp glyph. */
    SheetList,

    /** Message overflow danger — 36dp Danger soft circle. */
    SheetListDanger,

    /** Empty / hero symbol — 40dp glyph, no plate, Ink. */
    Hero,

    /** Same as [List] but Danger tint. */
    Danger,
}

private data class ChromeSpec(
    val box: Dp,
    val glyph: Dp,
    val tint: Color,
    val plate: Color?,
    val radius: Dp,
)

private fun AivoraIconChrome.spec(): ChromeSpec = when (this) {
    AivoraIconChrome.List, AivoraIconChrome.Action -> ChromeSpec(
        box = 24.dp,
        glyph = 24.dp,
        tint = AivoraColors.Ink,
        plate = null,
        radius = 0.dp,
    )
    AivoraIconChrome.SheetTile -> ChromeSpec(
        box = AivoraDimens.FabSize,
        glyph = 26.dp,
        tint = AivoraColors.Primary,
        plate = AivoraColors.SoftMint,
        radius = AivoraDimens.RadiusLg,
    )
    AivoraIconChrome.SheetList -> ChromeSpec(
        box = 36.dp,
        glyph = 18.dp,
        tint = AivoraColors.Primary,
        plate = AivoraColors.Primary.copy(alpha = 0.14f),
        radius = AivoraDimens.RadiusFull,
    )
    AivoraIconChrome.SheetListDanger -> ChromeSpec(
        box = 36.dp,
        glyph = 18.dp,
        tint = AivoraColors.Danger,
        plate = AivoraColors.Danger.copy(alpha = 0.14f),
        radius = AivoraDimens.RadiusFull,
    )
    AivoraIconChrome.Hero -> ChromeSpec(
        box = 40.dp,
        glyph = 40.dp,
        tint = AivoraColors.Ink,
        plate = null,
        radius = 0.dp,
    )
    AivoraIconChrome.Danger -> ChromeSpec(
        box = 24.dp,
        glyph = 24.dp,
        tint = AivoraColors.Danger,
        plate = null,
        radius = 0.dp,
    )
}

@Composable
fun AivoraIconSlot(
    chrome: AivoraIconChrome,
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = null,
    @DrawableRes painterRes: Int? = null,
    contentDescription: String? = null,
    /** Optional override — defaults come from [chrome] / Figma. */
    tint: Color? = null,
) {
    val spec = chrome.spec()
    val glyphTint = tint ?: spec.tint
    val glyph: @Composable () -> Unit = {
        when {
            painterRes != null -> Icon(
                painter = painterResource(painterRes),
                contentDescription = contentDescription,
                tint = glyphTint,
                modifier = Modifier.size(spec.glyph),
            )
            imageVector != null -> Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = glyphTint,
                modifier = Modifier.size(spec.glyph),
            )
        }
    }
    if (spec.plate != null) {
        Box(
            modifier = modifier
                .size(spec.box)
                .clip(RoundedCornerShape(spec.radius))
                .background(spec.plate),
            contentAlignment = Alignment.Center,
            content = { glyph() },
        )
    } else {
        Box(
            modifier = modifier.size(spec.box),
            contentAlignment = Alignment.Center,
            content = { glyph() },
        )
    }
}
