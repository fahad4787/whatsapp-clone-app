package com.aivora.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aivora.app.BuildConfig
import com.aivora.app.ui.feature.auth.LoginScreen
import com.aivora.app.ui.feature.auth.ProfileSetupScreen
import com.aivora.app.ui.feature.calls.AudioCallScreen
import com.aivora.app.ui.feature.calls.NewCallScreen
import com.aivora.app.ui.feature.calls.ScheduledCallsScreen
import com.aivora.app.ui.feature.calls.VideoCallScreen
import com.aivora.app.ui.feature.camera.CameraCaptureScreen
import com.aivora.app.ui.feature.channels.ChannelSlugScreen
import com.aivora.app.ui.feature.channels.ChannelsScreen
import com.aivora.app.ui.feature.chat.ChatIdScreen
import com.aivora.app.ui.feature.chat.ChatMediaScreen
import com.aivora.app.ui.feature.chat.LocationShareScreen
import com.aivora.app.ui.feature.chat.MessageInfoChatMessageScreen
import com.aivora.app.ui.feature.chat.NewChatScreen
import com.aivora.app.ui.feature.chat.PollCreateScreen
import com.aivora.app.ui.feature.community.CommunityIdScreen
import com.aivora.app.ui.feature.community.NewGroupScreen
import com.aivora.app.ui.feature.events.EventCreateChatScreen
import com.aivora.app.ui.feature.events.EventDetailsChatMessageScreen
import com.aivora.app.ui.feature.events.EventEditChatMessageScreen
import com.aivora.app.ui.feature.home.HomeScreen
import com.aivora.app.ui.feature.locked.LockedChatsScreen
import com.aivora.app.ui.feature.profile.ProfileIdScreen
import com.aivora.app.ui.feature.profile.YourDetailsScreen
import com.aivora.app.ui.feature.settings.HelpCenterScreen
import com.aivora.app.ui.feature.settings.HelpFeedbackScreen
import com.aivora.app.ui.feature.settings.InviteFriendScreen
import com.aivora.app.ui.feature.settings.SettingsAccountAddScreen
import com.aivora.app.ui.feature.settings.SettingsAccountChangeNumberScreen
import com.aivora.app.ui.feature.settings.SettingsAccountDeletedScreen
import com.aivora.app.ui.feature.settings.SettingsAccountEmailScreen
import com.aivora.app.ui.feature.settings.SettingsAccountPasskeysScreen
import com.aivora.app.ui.feature.settings.SettingsAccountRequestInfoScreen
import com.aivora.app.ui.feature.settings.SettingsAccountScreen
import com.aivora.app.ui.feature.settings.SettingsAccountSecurityNotificationsScreen
import com.aivora.app.ui.feature.settings.SettingsAccountTwoStepNewPinScreen
import com.aivora.app.ui.feature.settings.SettingsAppearanceScreen
import com.aivora.app.ui.feature.settings.SettingsBlockedScreen
import com.aivora.app.ui.feature.settings.SettingsChatsArchivedScreen
import com.aivora.app.ui.feature.settings.SettingsChatsBackupEncryptedScreen
import com.aivora.app.ui.feature.settings.SettingsChatsBackupFrequencyScreen
import com.aivora.app.ui.feature.settings.SettingsChatsBackupLocationScreen
import com.aivora.app.ui.feature.settings.SettingsChatsBackupOpenScreen
import com.aivora.app.ui.feature.settings.SettingsChatsBackupScreen
import com.aivora.app.ui.feature.settings.SettingsChatsExportScreen
import com.aivora.app.ui.feature.settings.SettingsChatsFontSizeScreen
import com.aivora.app.ui.feature.settings.SettingsChatsHistoryScreen
import com.aivora.app.ui.feature.settings.SettingsChatsScreen
import com.aivora.app.ui.feature.settings.SettingsChatsTranscriptsScreen
import com.aivora.app.ui.feature.settings.SettingsChatsTransferScreen
import com.aivora.app.ui.feature.settings.SettingsCommunitiesScreen
import com.aivora.app.ui.feature.settings.SettingsDeleteAccountScreen
import com.aivora.app.ui.feature.settings.SettingsDevicesLinkScreen
import com.aivora.app.ui.feature.settings.SettingsDevicesScreen
import com.aivora.app.ui.feature.settings.SettingsListsEditIdScreen
import com.aivora.app.ui.feature.settings.SettingsListsNewScreen
import com.aivora.app.ui.feature.settings.SettingsListsReorderScreen
import com.aivora.app.ui.feature.settings.SettingsManageStorageScreen
import com.aivora.app.ui.feature.settings.SettingsNotificationsScreen
import com.aivora.app.ui.feature.settings.SettingsPermissionsScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyAboutScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyAdvancedScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyAppLockScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyCallsScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyChatLockScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyChatLockSecretCodeScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyCheckupScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyDefaultTimerScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyGroupsScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyLastSeenScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyLiveLocationScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyProfilePhotoScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyScreen
import com.aivora.app.ui.feature.settings.SettingsPrivacyStatusScreen
import com.aivora.app.ui.feature.settings.SettingsProfileScreen
import com.aivora.app.ui.feature.settings.SettingsQrScreen
import com.aivora.app.ui.feature.settings.SettingsStarredScreen
import com.aivora.app.ui.feature.settings.SettingsStorageScreen
import com.aivora.app.ui.feature.settings.SettingsTwoStepScreen
import com.aivora.app.ui.feature.status.NewStatusScreen
import com.aivora.app.ui.feature.status.StatusViewerScreen
import com.aivora.app.ui.feature.transfer.TransferNewPhoneScreen

@Composable
fun AivoraNavHost(
    navController: NavHostController,
    startDestination: String = Routes.Login,
    modifier: Modifier = Modifier,
) {
    val go: (String) -> Unit = { route -> navController.navigateSingleTop(route) }
    val back: () -> Unit = { navController.safePopBackStack() }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = NavEnter,
        exitTransition = NavExit,
        popEnterTransition = NavPopEnter,
        popExitTransition = NavPopExit,
    ) {
        composable(
            route = Routes.ChannelSlug,
            arguments = listOf(navArgument("slug") { type = NavType.StringType }),
        ) { entry ->
            ChannelSlugScreen(
                slug = entry.arguments?.getString("slug").orEmpty(),
                onBack = back,
                onNavigate = go,
            )
        }
        composable(Routes.Channels) {
            ChannelsScreen(onBack = back, onNavigate = go)
        }
        composable(
            route = Routes.Camera,
            arguments = listOf(navArgument("target") { type = NavType.StringType }),
        ) { entry ->
            val raw = entry.arguments?.getString("target").orEmpty()
            CameraCaptureScreen(
                target = android.net.Uri.decode(raw),
                onBack = back,
                onNavigate = go,
            )
        }
        composable(
            route = Routes.PollCreate,
            arguments = listOf(navArgument("chat") { type = NavType.StringType }),
        ) { entry ->
            PollCreateScreen(
                chatId = Uri.decode(entry.arguments?.getString("chat").orEmpty()),
                onBack = back,
                onNavigate = go,
            )
        }
        composable(
            route = Routes.LocationShare,
            arguments = listOf(navArgument("chat") { type = NavType.StringType }),
        ) { entry ->
            LocationShareScreen(
                chatId = Uri.decode(entry.arguments?.getString("chat").orEmpty()),
                onBack = back,
                onNavigate = go,
            )
        }
        composable(
            route = Routes.ChatId,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            ChatIdScreen(
                chatId = entry.arguments?.getString("id").orEmpty(),
                onBack = back,
                onNavigate = go,
            )
        }
        composable(
            route = Routes.ChatMedia,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            ChatMediaScreen(
                chatId = entry.arguments?.getString("id").orEmpty(),
                onBack = back,
            )
        }
        composable(
            route = Routes.CommunityId,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            CommunityIdScreen(
                communityId = entry.arguments?.getString("id").orEmpty(),
                onBack = back,
                onNavigate = go,
            )
        }
        composable(
            route = Routes.EventCreateChat,
            arguments = listOf(navArgument("chat") { type = NavType.StringType }),
        ) { entry ->
            EventCreateChatScreen(
                chatId = entry.arguments?.getString("chat").orEmpty(),
                onBack = back,
                onNavigate = go,
            )
        }
        composable(
            route = Routes.EventDetailsChatMessage,
            arguments = listOf(
                navArgument("chat") { type = NavType.StringType },
                navArgument("message") { type = NavType.StringType },
            ),
        ) { entry ->
            EventDetailsChatMessageScreen(
                chatId = entry.arguments?.getString("chat").orEmpty(),
                messageId = entry.arguments?.getString("message").orEmpty(),
                onBack = back,
                onNavigate = go,
            )
        }
        composable(
            route = Routes.EventEditChatMessage,
            arguments = listOf(
                navArgument("chat") { type = NavType.StringType },
                navArgument("message") { type = NavType.StringType },
            ),
        ) { entry ->
            EventEditChatMessageScreen(
                chatId = entry.arguments?.getString("chat").orEmpty(),
                messageId = entry.arguments?.getString("message").orEmpty(),
                onBack = back,
                onNavigate = go,
            )
        }
        composable(Routes.Home) {
            HomeScreen(onNavigate = go)
        }
        composable(
            route = Routes.LockedChats,
        ) {
            LockedChatsScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.Login) {
            LoginScreen(
                onNavigate = go,
                onVerified = {
                    // Debug: same as OTP skip — go straight home. Release still shows profile setup.
                    if (BuildConfig.DEV_AUTO_OTP) {
                        navController.navigateClearing(Routes.Home, Routes.Login, inclusive = true)
                    } else {
                        navController.navigateClearing(Routes.ProfileSetup, Routes.Login, inclusive = false)
                    }
                },
                onSkipHome = {
                    navController.navigateClearing(Routes.Home, Routes.Login, inclusive = true)
                },
            )
        }
        composable(
            route = Routes.MessageInfoChatMessage,
            arguments = listOf(
                navArgument("chat") { type = NavType.StringType },
                navArgument("message") { type = NavType.StringType },
            ),
        ) { entry ->
            MessageInfoChatMessageScreen(
                chatId = entry.arguments?.getString("chat").orEmpty(),
                messageId = entry.arguments?.getString("message").orEmpty(),
                onBack = back,
                onNavigate = go,
            )
        }
        composable(Routes.NewCall) {
            NewCallScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.Keypad) {
            NewCallScreen(onBack = back, onNavigate = go, startKeypad = true)
        }
        composable(
            route = Routes.AudioCall,
            arguments = listOf(navArgument("name") { type = NavType.StringType }),
        ) { entry ->
            AudioCallScreen(
                name = Uri.decode(entry.arguments?.getString("name").orEmpty()).ifBlank { "Contact" },
                onEnd = back,
            )
        }
        composable(
            route = Routes.VideoCall,
            arguments = listOf(navArgument("name") { type = NavType.StringType }),
        ) { entry ->
            VideoCallScreen(
                name = Uri.decode(entry.arguments?.getString("name").orEmpty()).ifBlank { "Contact" },
                onEnd = back,
            )
        }
        composable(
            route = Routes.NewChat,
        ) {
            NewChatScreen(onBack = back, onNavigate = go)
        }
        composable(
            route = Routes.NewGroup,
        ) {
            NewGroupScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.NewStatus) {
            NewStatusScreen(onBack = back, onNavigate = go)
        }
        composable(
            route = Routes.StatusId,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            StatusViewerScreen(
                statusId = entry.arguments?.getString("id").orEmpty(),
                onBack = back,
            )
        }
        composable(Routes.ProfileSetup) {
            ProfileSetupScreen(
                onBack = back,
                onFinished = {
                    navController.navigateClearing(Routes.Home, Routes.Login, inclusive = true)
                },
            )
        }
        composable(
            route = Routes.ProfileId,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            ProfileIdScreen(
                profileId = entry.arguments?.getString("id").orEmpty(),
                onBack = back,
                onNavigate = go,
            )
        }
        composable(Routes.ScheduledCalls) {
            ScheduledCallsScreen(onBack = back, onNavigate = go)
        }
        composable(
            route = Routes.SettingsAccount,
        ) {
            SettingsAccountScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsAccountAdd) {
            SettingsAccountAddScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsAccountChangeNumber) {
            SettingsAccountChangeNumberScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsAccountDeleted) {
            SettingsAccountDeletedScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsAccountEmail) {
            SettingsAccountEmailScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsAccountPasskeys) {
            SettingsAccountPasskeysScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsAccountRequestInfo) {
            SettingsAccountRequestInfoScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsAccountSecurityNotifications) {
            SettingsAccountSecurityNotificationsScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsAccountTwoStepNewPin) {
            SettingsAccountTwoStepNewPinScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsBlocked) {
            SettingsBlockedScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsChats) {
            SettingsChatsScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsChatsArchived) {
            SettingsChatsArchivedScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsChatsBackup) {
            SettingsChatsBackupScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsChatsBackupEncrypted) {
            SettingsChatsBackupEncryptedScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsChatsBackupFrequency) {
            SettingsChatsBackupFrequencyScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsChatsBackupLocation) {
            SettingsChatsBackupLocationScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsChatsBackupOpen) {
            SettingsChatsBackupOpenScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsChatsExport) {
            SettingsChatsExportScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsChatsFontSize) {
            SettingsChatsFontSizeScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsChatsHistory) {
            SettingsChatsHistoryScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsChatsTranscripts) {
            SettingsChatsTranscriptsScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsChatsTransfer) {
            SettingsChatsTransferScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsCommunities) {
            SettingsCommunitiesScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsDeleteAccount) {
            SettingsDeleteAccountScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsDevices) {
            SettingsDevicesScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsDevicesLink) {
            SettingsDevicesLinkScreen(onBack = back, onNavigate = go)
        }
        composable(
            route = Routes.SettingsListsEditId,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            SettingsListsEditIdScreen(
                listId = entry.arguments?.getString("id").orEmpty(),
                onBack = back,
                onNavigate = go,
            )
        }
        composable(Routes.SettingsListsNew) {
            SettingsListsNewScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsListsReorder) {
            SettingsListsReorderScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPermissions) {
            SettingsPermissionsScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.HelpFeedback) {
            HelpFeedbackScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.HelpCenter) {
            HelpCenterScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.InviteFriend) {
            InviteFriendScreen(onBack = back, onNavigate = go)
        }
        composable(
            route = Routes.SettingsPrivacy,
        ) {
            SettingsPrivacyScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPrivacyAbout) {
            SettingsPrivacyAboutScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPrivacyAdvanced) {
            SettingsPrivacyAdvancedScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPrivacyAppLock) {
            SettingsPrivacyAppLockScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPrivacyCalls) {
            SettingsPrivacyCallsScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPrivacyChatLock) {
            SettingsPrivacyChatLockScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPrivacyChatLockSecretCode) {
            SettingsPrivacyChatLockSecretCodeScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPrivacyCheckup) {
            SettingsPrivacyCheckupScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPrivacyDefaultTimer) {
            SettingsPrivacyDefaultTimerScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPrivacyGroups) {
            SettingsPrivacyGroupsScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPrivacyLastSeen) {
            SettingsPrivacyLastSeenScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPrivacyLiveLocation) {
            SettingsPrivacyLiveLocationScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPrivacyProfilePhoto) {
            SettingsPrivacyProfilePhotoScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsPrivacyStatus) {
            SettingsPrivacyStatusScreen(onBack = back, onNavigate = go)
        }
        composable(
            route = Routes.SettingsProfile,
        ) {
            SettingsProfileScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsQr) {
            SettingsQrScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsStarred) {
            SettingsStarredScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsStorage) {
            SettingsStorageScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsManageStorage) {
            SettingsManageStorageScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsAppearance) {
            SettingsAppearanceScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsNotifications) {
            SettingsNotificationsScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.SettingsTwoStep) {
            SettingsTwoStepScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.TransferNewPhone) {
            TransferNewPhoneScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.YourDetails) {
            YourDetailsScreen(onBack = back, onNavigate = go)
        }
    }
}
