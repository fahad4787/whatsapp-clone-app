package com.aivora.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.ui.theme.AivoraColors

private data class NavDest(
    val label: String,
    val iconSelected: ImageVector,
    val iconIdle: ImageVector,
)

private val IdleGray = Color(0xFF8A9499)
private val IdleGrayDark = Color(0xFF6B757A)

/**
 * Floating dock — sliding pill only (no circular ripple).
 * Idle icons stay soft gray; selected uses brand green + filled glyph.
 */
@Composable
fun AivoraBottomNav(
    selected: Int,
    unreadCount: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val destinations = remember {
        listOf(
            NavDest("Chats", Icons.Filled.Chat, Icons.Outlined.Chat),
            NavDest("Updates", Icons.Outlined.Update, Icons.Outlined.Update),
            NavDest("Calls", Icons.Filled.Call, Icons.Outlined.Call),
            NavDest("My Profile", Icons.Filled.Person, Icons.Outlined.Person),
        )
    }
    val shape = RoundedCornerShape(26.dp)
    val surface = MaterialTheme.colorScheme.surface
    val idleTint = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) IdleGrayDark else IdleGray

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = shape,
                    ambientColor = AivoraColors.Ink.copy(alpha = 0.08f),
                    spotColor = AivoraColors.Ink.copy(alpha = 0.1f),
                )
                .clip(shape)
                .background(surface),
        ) {
            val slotWidth = maxWidth / destinations.size
            val indicatorOffset by animateDpAsState(
                targetValue = slotWidth * selected,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "navIndicatorX",
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset + 6.dp)
                    .width(slotWidth - 12.dp)
                    .fillMaxHeight()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AivoraColors.Primary.copy(alpha = 0.10f)),
            )

            Row(modifier = Modifier.fillMaxSize()) {
                destinations.forEachIndexed { index, dest ->
                    val selectedNow = selected == index
                    val interaction = remember { MutableInteractionSource() }
                    val scale by animateFloatAsState(
                        targetValue = if (selectedNow) 1.04f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                        label = "navIconScale$index",
                    )
                    val tint by animateColorAsState(
                        targetValue = if (selectedNow) AivoraColors.Primary else idleTint,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "navIconTint$index",
                    )
                    val labelAlpha by animateFloatAsState(
                        targetValue = if (selectedNow) 1f else 0.72f,
                        label = "navLabelAlpha$index",
                    )

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
                                    Avatar(
                                        name = "You",
                                        size = 22,
                                        modifier = Modifier.graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        },
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (selectedNow) dest.iconSelected else dest.iconIdle,
                                        contentDescription = dest.label,
                                        tint = tint,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                            },
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
                                color = tint.copy(alpha = labelAlpha),
                                fontSize = 10.sp,
                                fontWeight = if (selectedNow) FontWeight.SemiBold else FontWeight.Normal,
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
