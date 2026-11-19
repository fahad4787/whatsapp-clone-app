package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatListDto(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("conversation_ids") val conversationIds: List<String> = emptyList(),
)

@Serializable
data class ChatListListDto(
    @SerialName("items") val items: List<ChatListDto> = emptyList(),
)

@Serializable
data class ChatListCreateBody(
    @SerialName("name") val name: String,
    @SerialName("conversation_ids") val conversationIds: List<String>? = null,
)

@Serializable
data class ChatListPatchBody(
    @SerialName("name") val name: String? = null,
    @SerialName("conversation_ids") val conversationIds: List<String>? = null,
)

@Serializable
data class ChatListOrderBody(
    @SerialName("ids") val ids: List<String>,
)
