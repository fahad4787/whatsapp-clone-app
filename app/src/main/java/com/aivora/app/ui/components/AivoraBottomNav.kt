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
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.ui.theme.AivoraColors

private data class NavDest(
    val label: String,
    val icon: ImageVector,
)

/**
 * Floating pill dock with a spring-sliding selection glow.
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
            NavDest("Chats", Icons.Default.Chat),
            NavDest("Updates", Icons.Outlined.Update),
            NavDest("Calls", Icons.Default.Call),
            NavDest("Profile", Icons.Default.Person),
        )
    }
    val shape = RoundedCornerShape(28.dp)
    val surface = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = shape,
                    ambientColor = AivoraColors.Ink.copy(alpha = 0.12f),
                    spotColor = AivoraColors.Primary.copy(alpha = 0.18f),
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
            val indicatorWidth by animateDpAsState(
                targetValue = slotWidth - 10.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                label = "navIndicatorW",
            )

            // Sliding selection pill
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset + 5.dp)
                    .width(indicatorWidth)
                    .fillMaxHeight()
                    .padding(vertical = 7.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AivoraColors.Primary.copy(alpha = 0.14f)),
            )

            Row(modifier = Modifier.fillMaxSize()) {
                destinations.forEachIndexed { index, dest ->
                    val selectedNow = selected == index
                    val interaction = remember { MutableInteractionSource() }
                    val scale by animateFloatAsState(
                        targetValue = if (selectedNow) 1.08f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                        label = "navIconScale$index",
                    )
                    val tint by animateColorAsState(
                        targetValue = if (selectedNow) AivoraColors.Primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "navIconTint$index",
                    )
                    val labelWeight by animateFloatAsState(
                        targetValue = if (selectedNow) 1f else 0.85f,
                        label = "navLabelWeight$index",
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = interaction,
                                indication = ripple(bounded = false, radius = 28.dp),
                                onClick = { onSelect(index) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    dest.icon,
                                    contentDescription = dest.label,
                                    tint = tint,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        },
                                )
                                if (index == 0 && unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = 8.dp, y = (-4).dp)
                                            .size(16.dp)
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
                                color = tint,
                                fontSize = 10.sp,
                                fontWeight = if (selectedNow) FontWeight.SemiBold else FontWeight.Medium,
                                modifier = Modifier.graphicsLayer { alpha = 0.55f + 0.45f * labelWeight },
                            )
                        }
                    }
                }
            }
        }
    }
}
