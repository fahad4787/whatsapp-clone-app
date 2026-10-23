package com.aivora.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.LaunchedEffect
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
import com.aivora.app.data.ChatRepository
import com.aivora.app.data.model.UserDto
import com.aivora.app.ui.theme.AivoraColors

/** Shared people list for New chat / New group / Add participants. */
@Composable
fun rememberPeopleSearch(query: String, excludeIds: Set<String> = emptySet()): PeopleSearchState {
    var people by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val api = remember { ChatRepository.api() }
    LaunchedEffect(query, excludeIds) {
        val q = query.trim()
        runCatching {
            api.getPeopleSearch(q.ifBlank { null }, limit = 100).items
                .filter { it.id !in excludeIds }
        }.onSuccess {
            people = it
            error = null
        }.onFailure {
            error = it.message ?: "Could not load people"
        }
    }
    return PeopleSearchState(people, error)
}

data class PeopleSearchState(
    val people: List<UserDto>,
    val error: String?,
)

fun UserDto.displayLabel(): String =
    displayName?.takeIf { it.isNotBlank() }
        ?: phoneDisplay
        ?: phone
        ?: "User"

@Composable
fun PeopleSelectList(
    people: List<UserDto>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.5f), RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(people, key = { it.id.orEmpty() }) { user ->
                val id = user.id.orEmpty()
                if (id.isBlank()) return@items
                val name = user.displayLabel()
                val about = user.about ?: user.phone.orEmpty()
                val isOn = id in selectedIds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(id) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(name, size = 40)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(about, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    SelectCircle(selected = isOn)
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 66.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(0.35f),
                )
            }
        }
    }
}

@Composable
fun PeopleTapList(
    people: List<UserDto>,
    onPersonClick: (UserDto) -> Unit,
    header: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        if (header != null) {
            item { header() }
        }
        items(people, key = { it.id.orEmpty() }) { user ->
            PersonRow(
                name = user.displayLabel(),
                subtitle = user.phoneDisplay ?: user.phone ?: "",
                onClick = { onPersonClick(user) },
            )
        }
    }
}

@Composable
private fun SelectCircle(selected: Boolean) {
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
