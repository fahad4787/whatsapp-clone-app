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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * Bottom sheet in the *activity* window (Popup), so [WindowInsets.navigationBars]
 * match the bottom nav. Dialog windows report 0 insets and cover system nav.
 */
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
            ): IntOffset = IntOffset.Zero
        }
    }

    Popup(
        onDismissRequest = onDismiss,
        popupPositionProvider = positionProvider,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            clippingEnabled = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = with(density) { popupSize.width.toDp() },
                    height = with(density) { popupSize.height.toDp() },
                ),
        ) {
            // Scrim stops above system nav (same clearance as bottom nav).
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = navClearance)
                    .background(Color.Black.copy(alpha = 0.45f))
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
                        .padding(bottom = 16.dp),
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
                // Identical to AivoraBottomNav — keep sheet off the system nav.
                Spacer(Modifier.height(navClearance))
            }
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
