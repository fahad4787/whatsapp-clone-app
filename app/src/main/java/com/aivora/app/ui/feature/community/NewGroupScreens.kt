package com.aivora.app.ui.feature.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SeedData
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors

@Composable
fun NewGroupScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<String>()) }
    val people = remember(query) {
        SeedData.contacts.filter {
            query.isBlank() || it.first.contains(query, true) || it.second.contains(query, true)
        }
    }

    AivoraScaffold(title = "New group", onBack = onBack) {
        SearchField(query, { query = it }, "Search contacts")
        Text(
            "Choose the people you want in this group.",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.5f), RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(people, key = { it.first }) { (name, about) ->
                    val isOn = name in selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (isOn) selected - name else selected + name
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(name, size = 40)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(about, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ThemeSelectCircle(selected = isOn)
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 66.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(0.35f),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        PrimaryButton(
            text = "Next",
            onClick = {
                if (selected.isEmpty()) return@PrimaryButton
                val title = selected.take(3).joinToString(", ")
                val id = InMemoryChatStore.openOrCreateDirect(title.ifBlank { "New group" })
                onNavigate(Routes.ChatId.replace("{id}", id))
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun ThemeSelectCircle(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(AivoraColors.Primary)
                } else {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = AivoraColors.OnPrimary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
