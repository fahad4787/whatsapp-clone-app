package com.aivora.app.data.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * REST surface recovered from APK Retrofit interface (`te0`).
 * Base URL: https://wa.zirvik.com/
 * Method verbs inferred from R8 annotation usage (GET/POST/PUT/DELETE/PATCH).
 *
 * Return types intentionally use ResponseBody until each endpoint is typed
 * against [com.aivora.app.data.model] DTOs during rebuild.
 */
interface AivoraApi {

    @POST("api/media/presign")
    suspend fun postMediaPresign(
        @Body body: com.aivora.app.data.model.MediaPresignBody,
    ): com.aivora.app.data.model.MediaPresignDto

    @Multipart
    @POST("api/media/upload")
    suspend fun postMediaUpload(
        @Part file: okhttp3.MultipartBody.Part,
    ): com.aivora.app.data.model.MediaUploadResultDto

    @PUT
    suspend fun putMediaUpload(
        @Url url: String,
        @Body body: okhttp3.RequestBody,
    ): com.aivora.app.data.model.MediaUploadResultDto

    @POST("api/channels/{slug}/posts/{postId}/reaction")
    suspend fun postChannelsSlugPostsPostIdReaction(
        @Path("slug") slug: String,
        @Path("postId") postId: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @PUT("api/me/push-token")
    suspend fun putMePushToken(
        @Body body: com.aivora.app.data.model.PushTokenBody,
    ): com.aivora.app.data.model.DeviceDto

    @PATCH("api/me/devices/{id}/push-token")
    suspend fun patchMeDevicesIdPushToken(
        @Path("id") id: String,
        @Body body: com.aivora.app.data.model.PushTokenBody,
    ): com.aivora.app.data.model.DeviceDto

    @POST("api/auth/link/claim")
    suspend fun postAuthLinkClaim(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/people/search")
    suspend fun getPeopleSearch(
        @Query("q") q: String? = null,
    ): com.aivora.app.data.model.PeopleSearchDto

    @POST("api/reports")
    suspend fun postReports(
        @Body body: com.aivora.app.data.model.ReportRequestBody,
    ): okhttp3.ResponseBody

    @POST("api/conversations/{id}/clear-unstarred")
    suspend fun postConversationsIdClearUnstarred(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @PUT("api/me/privacy")
    suspend fun putMePrivacy(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/communities/{id}")
    suspend fun getCommunitiesId(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @POST("api/messages/{id}/view-once")
    suspend fun postMessagesIdViewOnce(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/communities/{id}/groups/{conversationId}/join")
    suspend fun postCommunitiesIdGroupsConversationIdJoin(
        @Path("id") id: String,
        @Path("conversationId") conversationId: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/conversations/{id}/media")
    suspend fun getConversationsIdMedia(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @POST("api/invites/{code}/join")
    suspend fun postInvitesCodeJoin(
        @Path("code") code: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/auth/passkey/options")
    suspend fun postAuthPasskeyOptions(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/call-links/{code}")
    suspend fun postCallLinksCode(
        @Path("code") code: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/conversations/{id}/pins")
    suspend fun getConversationsIdPins(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @DELETE("api/calls/history/{callId}")
    suspend fun deleteCallsHistoryCallId(
        @Path("callId") callId: String,
    ): okhttp3.ResponseBody

    @DELETE("api/messages/{id}/star")
    suspend fun deleteMessagesIdStar(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @PUT("api/conversations/{id}/members/{userId}")
    suspend fun putConversationsIdMembersUserId(
        @Path("id") id: String,
        @Path("userId") userId: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/messages/{id}/reactions")
    suspend fun postMessagesIdReactions(
        @Path("id") id: String,
        @Body body: com.aivora.app.data.model.ReactionBody,
    ): com.aivora.app.data.model.MessageDto

    @GET("api/me/two-step")
    suspend fun getMeTwoStep(): okhttp3.ResponseBody

    @POST("api/conversations/{id}/clear")
    suspend fun postConversationsIdClear(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @POST("api/blocks")
    suspend fun postBlocks(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/auth/link/cancel")
    suspend fun postAuthLinkCancel(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @DELETE("api/messages/{id}/rsvp")
    suspend fun deleteMessagesIdRsvp(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @POST("api/conversations/{id}/read")
    suspend fun postConversationsIdRead(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @POST("api/conversations/{id}/messages")
    suspend fun postConversationsIdMessages(
        @Path("id") id: String,
        @Body body: com.aivora.app.data.model.SendMessageBody,
    ): com.aivora.app.data.model.MessageDto

    @PUT("api/conversations/{id}/disappearing")
    suspend fun putConversationsIdDisappearing(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/scheduled-calls")
    suspend fun getScheduledCalls(): okhttp3.ResponseBody

    @GET("api/scheduled-calls/{id}")
    suspend fun getScheduledCallsId(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @GET("api/users/{id}/common-groups")
    suspend fun getUsersIdCommonGroups(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @PUT("api/me")
    suspend fun putMe(
        @Body body: com.aivora.app.data.model.MeUpdateBody,
    ): com.aivora.app.data.model.UserDto

    @POST("api/conversations/group")
    suspend fun postConversationsGroup(
        @Body body: com.aivora.app.data.model.GroupCreateBody,
    ): com.aivora.app.data.model.ConversationDto

    @PUT("api/conversations/{id}/privacy")
    suspend fun putConversationsIdPrivacy(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/me/auto-download")
    suspend fun getMeAutoDownload(): okhttp3.ResponseBody

    @PUT("api/me/privacy")
    suspend fun putMePrivacyAlt(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/app/config")
    suspend fun getAppConfig(): okhttp3.ResponseBody

    @POST("api/messages/delivered")
    suspend fun postMessagesDelivered(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/auth/passkey")
    suspend fun postAuthPasskey(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/auth/otp/verify")
    suspend fun postAuthOtpVerify(
        @Body body: com.aivora.app.data.model.OtpVerifyBody,
    ): com.aivora.app.data.model.RefreshResult

    @POST("api/auth/refresh")
    suspend fun postAuthRefresh(
        @Body body: com.aivora.app.data.model.RefreshBody,
    ): com.aivora.app.data.model.RefreshResult

    @POST("api/scheduled-calls")
    suspend fun postScheduledCalls(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @DELETE("api/status/{id}")
    suspend fun deleteStatusId(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @DELETE("api/messages/{id}")
    suspend fun deleteMessagesId(
        @Path("id") id: String,
        @Query("for") forScope: String = "me",
    ): okhttp3.ResponseBody

    @DELETE("api/me/devices/{id}")
    suspend fun deleteMeDevicesId(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @GET("api/me/live-locations")
    suspend fun getMeLiveLocations(): okhttp3.ResponseBody

    @POST("api/me/passkeys/options")
    suspend fun postMePasskeysOptions(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/me/chat-lock/unlock")
    suspend fun postMeChatLockUnlock(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/status/{id}/viewers")
    suspend fun getStatusIdViewers(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @POST("api/conversations/{id}/stickers")
    suspend fun postConversationsIdStickers(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @PUT("api/messages/{id}/live-location")
    suspend fun putMessagesIdLiveLocation(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/conversations/{id}/media/counts")
    suspend fun getConversationsIdMediaCounts(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @POST("api/calls")
    suspend fun postCalls(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @PATCH("api/me/username")
    suspend fun patchMeUsername(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/maps/reverse")
    suspend fun getMapsReverse(): okhttp3.ResponseBody

    @POST("api/auth/logout")
    suspend fun postAuthLogout(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/users/{id}")
    suspend fun getUsersId(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @POST("api/messages/{id}/vote")
    suspend fun postMessagesIdVote(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @PUT("api/messages/{id}")
    suspend fun putMessagesId(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/me/live-locations/stop")
    suspend fun postMeLiveLocationsStop(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @DELETE("api/conversations/{id}")
    suspend fun deleteConversationsId(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @GET("api/countries")
    suspend fun getCountries(): okhttp3.ResponseBody

    @DELETE("api/me/starred")
    suspend fun deleteMeStarred(): okhttp3.ResponseBody

    @GET("api/communities")
    suspend fun getCommunities(): okhttp3.ResponseBody

    @GET("api/me/privacy")
    suspend fun getMePrivacy(): okhttp3.ResponseBody

    @POST("api/status/{id}")
    suspend fun postStatusId(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/channels/{slug}/posts")
    suspend fun postChannelsSlugPosts(
        @Path("slug") slug: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/status")
    suspend fun getStatus(): okhttp3.ResponseBody

    @DELETE("api/scheduled-calls/{id}")
    suspend fun deleteScheduledCallsId(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @POST("api/auth/two-step/reset/request")
    suspend fun postAuthTwoStepResetRequest(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @DELETE("api/blocks/{id}")
    suspend fun deleteBlocksId(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @POST("api/me/delete")
    suspend fun postMeDelete(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/channels/{slug}/follow")
    suspend fun postChannelsSlugFollow(
        @Path("slug") slug: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/auth/link/start")
    suspend fun postAuthLinkStart(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/status/{id}/reaction")
    suspend fun postStatusIdReaction(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @PATCH("api/me/two-step")
    suspend fun patchMeTwoStep(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @DELETE("api/conversations/{id}/members/{userId}")
    suspend fun deleteConversationsIdMembersUserId(
        @Path("id") id: String,
        @Path("userId") userId: String,
    ): okhttp3.ResponseBody

    @POST("api/calls/{id}/invite")
    suspend fun postCallsIdInvite(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/search/messages")
    suspend fun getSearchMessages(): okhttp3.ResponseBody

    @DELETE("api/me/devices/{id}/push-token")
    suspend fun deleteMeDevicesIdPushToken(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @GET("api/channels/{slug}")
    suspend fun getChannelsSlug(
        @Path("slug") slug: String,
    ): okhttp3.ResponseBody

    @GET("api/conversations/{id}/messages")
    suspend fun getConversationsIdMessages(
        @Path("id") id: String,
    ): com.aivora.app.data.model.MessageListDto

    @GET("api/me/username")
    suspend fun getMeUsername(): okhttp3.ResponseBody

    @PUT("api/conversations/{id}/settings")
    suspend fun putConversationsIdSettings(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/calls/history")
    suspend fun getCallsHistory(): okhttp3.ResponseBody

    @POST("api/scheduled-calls/{id}/respond")
    suspend fun postScheduledCallsIdRespond(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/me/storage")
    suspend fun getMeStorage(): okhttp3.ResponseBody

    @GET("api/conversations")
    suspend fun getConversations(): com.aivora.app.data.model.ConversationListDto

    @GET("api/auth/link/approved/{code}")
    suspend fun getAuthLinkApprovedCode(
        @Path("code") code: String,
    ): okhttp3.ResponseBody

    @POST("api/contacts/sync")
    suspend fun postContactsSync(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/users/by-username")
    suspend fun getUsersByUsername(): okhttp3.ResponseBody

    @POST("api/messages/{id}/star")
    suspend fun postMessagesIdStar(
        @Path("id") id: String,
        @Body body: com.aivora.app.data.model.EmptyRequestBody,
    ): com.aivora.app.data.model.MessageDto

    @DELETE("api/me/passkeys/{id}")
    suspend fun deleteMePasskeysId(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @GET("api/conversations/{id}")
    suspend fun getConversationsId(
        @Path("id") id: String,
    ): com.aivora.app.data.model.ConversationDto

    @Multipart
    @POST("api/me/avatar")
    suspend fun postMeAvatar(
        @Part file: MultipartBody.Part,
    ): com.aivora.app.data.model.UserDto

    @GET("api/me/data")
    suspend fun getMeData(): okhttp3.ResponseBody

    @POST("api/conversations/{id}/avatar")
    suspend fun postConversationsIdAvatar(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/call-links")
    suspend fun postCallLinks(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/me/two-step/off")
    suspend fun postMeTwoStepOff(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/me/passkeys")
    suspend fun getMePasskeys(): okhttp3.ResponseBody

    @POST("api/conversations/direct")
    suspend fun postConversationsDirect(
        @Body body: com.aivora.app.data.model.DirectBody,
    ): com.aivora.app.data.model.ConversationDto

    @GET("api/blocks")
    suspend fun getBlocks(): okhttp3.ResponseBody

    @POST("api/status")
    suspend fun postStatus(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/calls/{id}")
    suspend fun getCallsId(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @DELETE("api/me/avatar")
    suspend fun deleteMeAvatar(): com.aivora.app.data.model.UserDto

    @POST("api/auth/email/request")
    suspend fun postAuthEmailRequest(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/me/delete/request")
    suspend fun postMeDeleteRequest(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/me/chat-lock")
    suspend fun getMeChatLock(): okhttp3.ResponseBody

    @GET("api/me/starred")
    suspend fun getMeStarred(): com.aivora.app.data.model.MessageListDto

    @GET("api/conversations/{id}/messages")
    suspend fun getConversationsIdMessagesAlt(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @POST("api/messages/{id}/rsvp")
    suspend fun postMessagesIdRsvp(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/users/lookup")
    suspend fun getUsersLookup(): okhttp3.ResponseBody

    @PUT("api/conversations/{id}/settings")
    suspend fun putConversationsIdSettingsAlt(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/channels")
    suspend fun getChannels(): okhttp3.ResponseBody

    @GET("api/channels/{slug}/posts")
    suspend fun getChannelsSlugPosts(
        @Path("slug") slug: String,
    ): okhttp3.ResponseBody

    @DELETE("api/messages/{id}/vote")
    suspend fun deleteMessagesIdVote(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @PUT("api/scheduled-calls/{id}")
    suspend fun putScheduledCallsId(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/me/devices")
    suspend fun getMeDevices(): com.aivora.app.data.model.DeviceListDto

    @GET("api/maps/search")
    suspend fun getMapsSearch(): okhttp3.ResponseBody

    @DELETE("api/calls/history")
    suspend fun deleteCallsHistory(): okhttp3.ResponseBody

    @GET("api/conversations/{id}/export")
    suspend fun getConversationsIdExport(
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @GET("api/contacts")
    suspend fun getContacts(): okhttp3.ResponseBody

    @POST("api/auth/two-step/reset/confirm")
    suspend fun postAuthTwoStepResetConfirm(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/messages/{id}/forward")
    suspend fun postMessagesIdForward(
        @Path("id") id: String,
        @Body body: com.aivora.app.data.model.ForwardMessageBody,
    ): com.aivora.app.data.model.MessageDto

    @POST("api/channels/{slug}/report")
    suspend fun postChannelsSlugReport(
        @Path("slug") slug: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/conversations/{id}/members")
    suspend fun postConversationsIdMembers(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/conversations/{id}/leave")
    suspend fun postConversationsIdLeave(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/auth/logout")
    suspend fun postAuthLogoutAlt(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @PUT("api/conversations/{id}/info")
    suspend fun putConversationsIdInfo(
        @Path("id") id: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/auth/email/verify")
    suspend fun postAuthEmailVerify(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/auth/otp/request")
    suspend fun postAuthOtpRequest(
        @Body body: com.aivora.app.data.model.OtpRequestBody,
    ): com.aivora.app.data.model.OtpRequestResult

    @GET("api/stickers")
    suspend fun getStickers(): okhttp3.ResponseBody

    @DELETE("api/channels/{slug}/follow")
    suspend fun deleteChannelsSlugFollow(
        @Path("slug") slug: String,
    ): okhttp3.ResponseBody

    @PATCH("api/me/auto-download")
    suspend fun patchMeAutoDownload(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/auth/two-step")
    suspend fun postAuthTwoStep(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @GET("api/me")
    suspend fun getMe(): com.aivora.app.data.model.UserDto

    @GET("api/me/starred")
    suspend fun getMeStarredAlt(): okhttp3.ResponseBody

    @POST("api/me/passkeys")
    suspend fun postMePasskeys(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/auth/link/approve")
    suspend fun postAuthLinkApprove(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

    @POST("api/calls/{id}/{action}")
    suspend fun postCallsIdAction(
        @Path("id") id: String,
        @Path("action") action: String,
            @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): okhttp3.ResponseBody

}
