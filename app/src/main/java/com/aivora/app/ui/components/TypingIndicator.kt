package com.aivora.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aivora.app.ui.theme.AivoraColors

/**
 * Static typing dots — InfiniteTransition was burning frames on Mate-class devices
 * while the list scrolled (and when typing sat inside LazyColumn).
 */
@Composable
fun TypingIndicatorBubble(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(
                color = AivoraColors.BubbleOtherLight,
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 18.dp,
                ),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(AivoraColors.TextSecondary.copy(alpha = 0.55f), CircleShape),
            )
        }
    }
}
