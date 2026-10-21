package com.aivora.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.ui.icons.AivoraIcons
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import kotlin.math.roundToInt

private data class NavDest(
    val label: String,
    @DrawableRes val iconSelected: Int,
    @DrawableRes val iconIdle: Int,
    val tintSelected: Boolean = true,
)

private val IdleGray = Color(0xFF9B9B9B)
private val IdleGrayDark = Color(0xFF6B757A)

/**
 * Floating dock. [selectionProgress] is page + pageOffset so the pill tracks
 * the pager 1:1 during a swipe (no spring fighting the gesture).
 */
@Composable
fun AivoraBottomNav(
    selected: Int,
    unreadCount: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectionProgress: Float = selected.toFloat(),
) {
    val destinations = remember {
        listOf(
            NavDest("Chats", AivoraIcons.NavChatsSelected, AivoraIcons.NavChatsIdle, tintSelected = false),
            NavDest("Updates", AivoraIcons.NavUpdates, AivoraIcons.NavUpdatesIdle),
            NavDest("Calls", AivoraIcons.NavCalls, AivoraIcons.NavCallsIdle),
            NavDest("My Profile", 0, 0),
        )
    }
    val shape = RoundedCornerShape(AivoraDimens.RadiusMd)
    val surface = MaterialTheme.colorScheme.surface
    val idleTint = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) IdleGrayDark else IdleGray
    val activeIndex = selectionProgress.roundToInt().coerceIn(0, destinations.lastIndex)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = shape,
                    ambientColor = AivoraColors.Ink.copy(alpha = 0.06f),
                    spotColor = AivoraColors.Ink.copy(alpha = 0.08f),
                )
                .clip(shape)
                .background(surface),
        ) {
            val slotWidth = maxWidth / destinations.size
            val progress = selectionProgress.coerceIn(0f, (destinations.size - 1).toFloat())

            Box(
                modifier = Modifier
                    .offset(x = slotWidth * progress + 6.dp)
                    .width(slotWidth - 12.dp)
                    .fillMaxHeight()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AivoraColors.Primary.copy(alpha = 0.10f)),
            )

            Row(modifier = Modifier.fillMaxSize()) {
                destinations.forEachIndexed { index, dest ->
                    val selectedNow = activeIndex == index
                    val interaction = remember { MutableInteractionSource() }
                    val tint = if (selectedNow) AivoraColors.Primary else idleTint

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = interaction,
                                indication = null,
                                onClick = { onSelect(index) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                if (index == 3) {
                                    Avatar(name = "You", size = 22)
                                } else {
                                    val res = if (selectedNow) dest.iconSelected else dest.iconIdle
                                    val applyTint = !(selectedNow && !dest.tintSelected)
                                    Icon(
                                        painter = painterResource(res),
                                        contentDescription = dest.label,
                                        tint = if (applyTint) tint else Color.Unspecified,
                                        modifier = Modifier.size(AivoraDimens.BottomNavIcon),
                                    )
                                }
                                if (index == 0 && unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = 7.dp, y = (-3).dp)
                                            .size(15.dp)
                                            .clip(CircleShape)
                                            .background(AivoraColors.Primary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            if (unreadCount > 99) "99+" else "$unreadCount",
                                            color = AivoraColors.OnPrimary,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 8.sp,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                dest.label,
                                color = tint.copy(alpha = if (selectedNow) 1f else 0.72f),
                                fontSize = 10.6.sp,
                                fontWeight = if (selectedNow) FontWeight.Medium else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}
