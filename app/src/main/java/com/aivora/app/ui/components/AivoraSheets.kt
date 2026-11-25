package com.aivora.app.ui.components

import com.aivora.app.ui.icons.AppIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.aivora.app.ui.icons.AivoraIconChrome
import com.aivora.app.ui.icons.AivoraIconSlot
import com.aivora.app.ui.theme.AivoraColors

/**
 * Same clearance the home bottom nav uses — sits above the system nav, never on it.
 */
@Composable
fun aivoraSystemNavClearance(): Dp {
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return maxOf(navBottom, 12.dp) + 8.dp
}

private const val SheetDimAmount = 0.45f

/** Lets the system dim everything behind the popup, including the area the popup can't reach. */
private fun dimBehind(view: View) {
    val params = view.layoutParams as? WindowManager.LayoutParams ?: return
    val dimmed = params.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND != 0
    if (!view.isAttachedToWindow || (dimmed && params.dimAmount == SheetDimAmount)) return
    params.flags = params.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
    params.dimAmount = SheetDimAmount
    view.context.getSystemService(WindowManager::class.java)?.updateViewLayout(view, params)
}

/** The sheet sits behind the system nav buttons, so they need to contrast with its surface. */
@Suppress("DEPRECATION")
private fun darkNavigationIcons(view: View, dark: Boolean) {
    if (Build.VERSION.SDK_INT >= 30) {
        val mask = WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        view.windowInsetsController?.setSystemBarsAppearance(if (dark) mask else 0, mask)
    } else {
        val flag = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        view.systemUiVisibility =
            if (dark) view.systemUiVisibility or flag else view.systemUiVisibility and flag.inv()
    }
}

/**
 * Bottom sheet in the *activity* window (Popup), so [WindowInsets.navigationBars]
 * match the bottom nav. Dialog windows report 0 insets and cover system nav.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AivoraBottomSheet(
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val navClearance = aivoraSystemNavClearance()
    val popupSize = remember(view) {
        IntSize(view.rootView.width, view.rootView.height)
    }
    val positionProvider = remember(popupSize) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset =
                // Android 14 and older cap a popup's height at the space between the system
                // bars, so anchor it to the bottom edge of the screen.
                IntOffset(0, (popupSize.height - popupContentSize.height).coerceAtLeast(0))
        }
    }

    // While the keyboard is up, Back should only hide the keyboard (like WhatsApp),
    // not close the sheet and lose what was typed.
    val imeVisible = androidx.compose.foundation.layout.WindowInsets.isImeVisible
    var popupRoot by remember { mutableStateOf<View?>(null) }
    Popup(
        onDismissRequest = onDismiss,
        popupPositionProvider = positionProvider,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = !imeVisible,
            dismissOnClickOutside = true,
            clippingEnabled = false,
        ),
    ) {
        val popupView = LocalView.current
        SideEffect { popupRoot = popupView.rootView }
        Box(
            modifier = Modifier
                .size(
                    width = with(density) { popupSize.width.toDp() },
                    height = with(density) { popupSize.height.toDp() },
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss,
                    ),
            )
            Column(
                modifier = modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {},
                        )
                        // The sheet reaches the screen edge; its content stays above system nav.
                        .padding(bottom = 8.dp + navClearance),
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AivoraColors.BorderMuted),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (leading != null) {
                            leading()
                            Spacer(Modifier.width(12.dp))
                        }
                        Text(
                            title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AivoraColors.Ink,
                        )
                        IconButton(onClick = onDismiss) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AivoraColors.SurfaceMuted),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    AppIcons.Close,
                                    contentDescription = "Close",
                                    tint = AivoraColors.InkMuted,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                    content()
                }
            }
        }
    }
    // Declared after the Popup so it runs after the popup has (re)written its window flags.
    val root = popupRoot
    val darkTheme = AivoraColors.isDark
    SideEffect {
        if (root != null) {
            dimBehind(root)
            darkNavigationIcons(root, dark = !darkTheme)
        }
    }
}

data class AivoraSheetAction(
    val label: String,
    val icon: ImageVector,
    val danger: Boolean = false,
    val onClick: () -> Unit,
)

@Composable
fun AivoraSheetActionList(
    actions: List<AivoraSheetAction>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AivoraColors.SurfaceMuted),
    ) {
        actions.forEachIndexed { index, action ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = action.onClick)
                    .padding(horizontal = 18.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AivoraIconSlot(
                    chrome = if (action.danger) AivoraIconChrome.SheetListDanger else AivoraIconChrome.SheetList,
                    imageVector = action.icon,
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    action.label,
                    fontSize = 16.sp,
                    color = if (action.danger) AivoraColors.Danger else AivoraColors.Ink,
                )
            }
            if (index < actions.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = AivoraColors.Divider,
                )
            }
        }
    }
}

@Composable
fun AivoraSheetActionGrid(
    actions: List<AivoraSheetAction>,
    modifier: Modifier = Modifier,
    columns: Int = 4,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        actions.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                row.forEach { action ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = action.onClick)
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AivoraIconSlot(
                            chrome = AivoraIconChrome.SheetTile,
                            imageVector = action.icon,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            action.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = AivoraColors.InkMuted,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                    }
                }
                repeat(columns - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
