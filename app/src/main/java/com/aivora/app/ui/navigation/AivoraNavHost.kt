package com.aivora.app.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aivora.app.ui.feature.auth.LoginScreen
import com.aivora.app.ui.feature.auth.ProfileSetupScreen
import com.aivora.app.ui.feature.calls.NewCallScreen
import com.aivora.app.ui.feature.calls.ScheduledCallsScreen
import com.aivora.app.ui.feature.camera.CameraCaptureScreen
import com.aivora.app.ui.feature.channels.ChannelSlugScreen
import com.aivora.app.ui.feature.channels.ChannelsScreen
import com.aivora.app.ui.feature.chat.ChatIdScreen
import com.aivora.app.ui.feature.chat.MessageInfoChatMessageScreen
import com.aivora.app.ui.feature.chat.NewChatScreen
import com.aivora.app.ui.feature.community.CommunityIdScreen
import com.aivora.app.ui.feature.community.NewGroupScreen
import com.aivora.app.ui.feature.events.EventCreateChatScreen
import com.aivora.app.ui.feature.events.EventDetailsChatMessageScreen
import com.aivora.app.ui.feature.events.EventEditChatMessageScreen
import com.aivora.app.ui.feature.home.HomeScreen
import com.aivora.app.ui.feature.locked.LockedChatsScreen
import com.aivora.app.ui.feature.profile.ProfileIdScreen
import com.aivora.app.ui.feature.profile.YourDetailsScreen
import com.aivora.app.ui.feature.status.NewStatusScreen
import com.aivora.app.ui.feature.status.StatusViewerScreen
import com.aivora.app.ui.feature.settings.SettingsAccountAddScreen
import com.aivora.app.ui.feature.settings.SettingsAccountChangeNumberScreen
import com.aivora.app.ui.feature.settings.SettingsAccountDeletedScreen
import com.aivora.app.ui.feature.settings.SettingsAccountEmailScreen
import com.aivora.app.ui.feature.settings.SettingsAccountPasskeysScreen
import com.aivora.app.ui.feature.settings.SettingsAccountRequestInfoScreen
import com.aivora.app.ui.feature.settings.SettingsAccountScreen
import com.aivora.app.ui.feature.settings.SettingsAccountSecurityNotificationsScreen
import com.aivora.app.ui.feature.settings.SettingsAccountTwoStepNewPinScreen
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
import com.aivora.app.ui.feature.transfer.TransferNewPhoneScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AivoraNavHost(
    navController: NavHostController,
    startDestination: String = Routes.Login,
    modifier: Modifier = Modifier,
) {
    val go: (String) -> Unit = { route -> navController.navigateSingleTop(route) }
    val back: () -> Unit = { navController.safePopBackStack() }

    SharedTransitionLayout(modifier = modifier) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = FadeEnter,
        exitTransition = FadeExit,
        popEnterTransition = FadePopEnter,
        popExitTransition = FadePopExit,
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
            route = Routes.ChatId,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
            enterTransition = SlideFromRightEnter,
            exitTransition = StayPutExit,
            popEnterTransition = StayPutPopEnter,
            popExitTransition = SlideToRightExit,
        ) { entry ->
            ChatIdScreen(
                chatId = entry.arguments?.getString("id").orEmpty(),
                onBack = back,
                onNavigate = go,
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this,
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
            HomeScreen(
                onNavigate = go,
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this,
            )
        }
        composable(
            route = Routes.LockedChats,
            enterTransition = SlideFromRightEnter,
            exitTransition = StayPutExit,
            popEnterTransition = StayPutPopEnter,
            popExitTransition = SlideToRightExit,
        ) {
            LockedChatsScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.Login) {
            LoginScreen(
                onNavigate = go,
                onVerified = {
                    navController.navigateClearing(Routes.ProfileSetup, Routes.Login, inclusive = false)
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
        composable(
            route = Routes.NewChat,
            enterTransition = SlideFromRightEnter,
            exitTransition = StayPutExit,
            popEnterTransition = StayPutPopEnter,
            popExitTransition = SlideToRightExit,
        ) {
            NewChatScreen(onBack = back, onNavigate = go)
        }
        composable(
            route = Routes.NewGroup,
            enterTransition = SlideFromRightEnter,
            exitTransition = StayPutExit,
            popEnterTransition = StayPutPopEnter,
            popExitTransition = SlideToRightExit,
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
            enterTransition = SlideFromRightEnter,
            exitTransition = StayPutExit,
            popEnterTransition = StayPutPopEnter,
            popExitTransition = SlideToRightExit,
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
            enterTransition = SlideFromRightEnter,
            exitTransition = StayPutExit,
            popEnterTransition = StayPutPopEnter,
            popExitTransition = SlideToRightExit,
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
        composable(
            route = Routes.SettingsPrivacy,
            enterTransition = SlideFromRightEnter,
            exitTransition = StayPutExit,
            popEnterTransition = StayPutPopEnter,
            popExitTransition = SlideToRightExit,
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
            enterTransition = SlideFromRightEnter,
            exitTransition = StayPutExit,
            popEnterTransition = StayPutPopEnter,
            popExitTransition = SlideToRightExit,
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
    } // SharedTransitionLayout
}
