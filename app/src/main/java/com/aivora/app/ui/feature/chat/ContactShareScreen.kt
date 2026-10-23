package com.aivora.app.ui.feature.chat

import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.ContactCardDto
import com.aivora.app.data.model.SendMessageBody
import com.aivora.app.data.toLocalMessage
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.PeopleTapList
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.components.displayLabel
import com.aivora.app.ui.components.rememberPeopleSearch
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ContactShareScreen(
    chatId: String = "1",
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val myUserId = remember { SessionStore.userId(context) }
    var query by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val search = rememberPeopleSearch(query, excludeIds = setOfNotNull(myUserId))

    AivoraScaffold(title = "Share contact", onBack = onBack) {
        InfoBanner("Pick someone to send as a contact card")
        SearchField(query, { query = it }, "Search name or phone")
        search.error?.let { Text(it, color = AivoraColors.Danger, fontSize = 13.sp) }
        PeopleTapList(
            people = search.people,
            onPersonClick = { user ->
                val uid = myUserId ?: return@PeopleTapList
                if (busy) return@PeopleTapList
                busy = true
                scope.launch {
                    runCatching {
                        NetworkModule.api().postConversationsIdMessages(
                            chatId,
                            SendMessageBody(
                                clientId = UUID.randomUUID().toString(),
                                contact = ContactCardDto(
                                    name = user.displayLabel(),
                                    phones = listOfNotNull(user.phoneDisplay ?: user.phone),
                                    userId = user.id,
                                ),
                            ),
                        )
                    }.onSuccess {
                        InMemoryChatStore.upsertMessage(chatId, it.toLocalMessage(uid))
                        onBack?.invoke()
                    }.onFailure {
                        busy = false
                        Toast.makeText(context, "Could not share contact", Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
    }
}
