package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupMembersBody(
    @SerialName("user_ids") val userIds: List<String>,
)

@Serializable
data class GroupMemberRoleBody(
    @SerialName("role") val role: String,
)

@Serializable
data class GroupInfoBody(
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
)

@Serializable
data class GroupPrivacyBody(
    @SerialName("only_admins_can_send") val onlyAdminsCanSend: Boolean? = null,
    @SerialName("only_admins_can_edit_info") val onlyAdminsCanEditInfo: Boolean? = null,
)
