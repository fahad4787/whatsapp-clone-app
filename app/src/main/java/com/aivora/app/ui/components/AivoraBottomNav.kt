package com.aivora.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.zIndex
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
 * Figma bottom nav (`13:86`): white floating bar with soft separation shadow.
 * Active = green label + selected icon only — no green pill behind the item.
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

    val navClearance = aivoraSystemNavClearance()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.10f),
                    clip = false,
                )
                .clip(shape)
                .background(surface)
                .border(1.dp, Color.Black.copy(alpha = 0.04f), shape)
                .padding(top = 14.dp, bottom = 10.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEachIndexed { index, dest ->
                val selectedNow = activeIndex == index
                val interaction = remember { MutableInteractionSource() }
                val tint = if (selectedNow) AivoraColors.Primary else idleTint

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            onClick = { onSelect(index) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(width = 36.dp, height = 26.dp),
                            contentAlignment = Alignment.Center,
                        ) {
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
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-4).dp)
                                        .zIndex(1f)
                                        .height(16.dp)
                                        .clip(CircleShape)
                                        .background(AivoraColors.Primary)
                                        .border(1.5.dp, surface, CircleShape)
                                        .padding(horizontal = 5.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        if (unreadCount > 99) "99+" else "$unreadCount",
                                        color = AivoraColors.OnPrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 10.sp,
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            dest.label,
                            color = tint,
                            fontSize = 10.6.sp,
                            fontWeight = if (selectedNow) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(navClearance))
    }
}

private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}
