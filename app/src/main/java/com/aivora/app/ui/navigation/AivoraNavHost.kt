package com.aivora.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aivora.app.ui.screens.ChannelSlugScreen
import com.aivora.app.ui.screens.ChannelsScreen
import com.aivora.app.ui.screens.ChatIdScreen
import com.aivora.app.ui.screens.CommunityIdScreen
import com.aivora.app.ui.screens.EventCreateChatScreen
import com.aivora.app.ui.screens.EventDetailsChatMessageScreen
import com.aivora.app.ui.screens.EventEditChatMessageScreen
import com.aivora.app.ui.screens.HomeScreen
import com.aivora.app.ui.screens.LockedChatsScreen
import com.aivora.app.ui.screens.LoginScreen
import com.aivora.app.ui.screens.MessageInfoChatMessageScreen
import com.aivora.app.ui.screens.NewCallScreen
import com.aivora.app.ui.screens.NewChatScreen
import com.aivora.app.ui.screens.NewGroupScreen
import com.aivora.app.ui.screens.NewStatusScreen
import com.aivora.app.ui.screens.ProfileIdScreen
import com.aivora.app.ui.screens.ProfileSetupScreen
import com.aivora.app.ui.screens.ScheduledCallsScreen
import com.aivora.app.ui.screens.SettingsAccountAddScreen
import com.aivora.app.ui.screens.SettingsAccountChangeNumberScreen
import com.aivora.app.ui.screens.SettingsAccountDeletedScreen
import com.aivora.app.ui.screens.SettingsAccountEmailScreen
import com.aivora.app.ui.screens.SettingsAccountPasskeysScreen
import com.aivora.app.ui.screens.SettingsAccountRequestInfoScreen
import com.aivora.app.ui.screens.SettingsAccountScreen
import com.aivora.app.ui.screens.SettingsAccountSecurityNotificationsScreen
import com.aivora.app.ui.screens.SettingsAccountTwoStepNewPinScreen
import com.aivora.app.ui.screens.SettingsBlockedScreen
import com.aivora.app.ui.screens.SettingsChatsArchivedScreen
import com.aivora.app.ui.screens.SettingsChatsBackupEncryptedScreen
import com.aivora.app.ui.screens.SettingsChatsBackupFrequencyScreen
import com.aivora.app.ui.screens.SettingsChatsBackupLocationScreen
import com.aivora.app.ui.screens.SettingsChatsBackupOpenScreen
import com.aivora.app.ui.screens.SettingsChatsBackupScreen
import com.aivora.app.ui.screens.SettingsChatsExportScreen
import com.aivora.app.ui.screens.SettingsChatsFontSizeScreen
import com.aivora.app.ui.screens.SettingsChatsHistoryScreen
import com.aivora.app.ui.screens.SettingsChatsScreen
import com.aivora.app.ui.screens.SettingsChatsTranscriptsScreen
import com.aivora.app.ui.screens.SettingsChatsTransferScreen
import com.aivora.app.ui.screens.SettingsCommunitiesScreen
import com.aivora.app.ui.screens.SettingsDeleteAccountScreen
import com.aivora.app.ui.screens.SettingsDevicesLinkScreen
import com.aivora.app.ui.screens.SettingsDevicesScreen
import com.aivora.app.ui.screens.SettingsListsEditIdScreen
import com.aivora.app.ui.screens.SettingsListsNewScreen
import com.aivora.app.ui.screens.SettingsListsReorderScreen
import com.aivora.app.ui.screens.SettingsPermissionsScreen
import com.aivora.app.ui.screens.SettingsPrivacyAboutScreen
import com.aivora.app.ui.screens.SettingsPrivacyAdvancedScreen
import com.aivora.app.ui.screens.SettingsPrivacyAppLockScreen
import com.aivora.app.ui.screens.SettingsPrivacyCallsScreen
import com.aivora.app.ui.screens.SettingsPrivacyChatLockScreen
import com.aivora.app.ui.screens.SettingsPrivacyChatLockSecretCodeScreen
import com.aivora.app.ui.screens.SettingsPrivacyCheckupScreen
import com.aivora.app.ui.screens.SettingsPrivacyDefaultTimerScreen
import com.aivora.app.ui.screens.SettingsPrivacyGroupsScreen
import com.aivora.app.ui.screens.SettingsPrivacyLastSeenScreen
import com.aivora.app.ui.screens.SettingsPrivacyLiveLocationScreen
import com.aivora.app.ui.screens.SettingsPrivacyProfilePhotoScreen
import com.aivora.app.ui.screens.SettingsPrivacyScreen
import com.aivora.app.ui.screens.SettingsPrivacyStatusScreen
import com.aivora.app.ui.screens.SettingsProfileScreen
import com.aivora.app.ui.screens.SettingsQrScreen
import com.aivora.app.ui.screens.SettingsStarredScreen
import com.aivora.app.ui.screens.SettingsStorageScreen
import com.aivora.app.ui.screens.SettingsTwoStepScreen
import com.aivora.app.ui.screens.TransferNewPhoneScreen
import com.aivora.app.ui.screens.YourDetailsScreen

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
        composable(Routes.LockedChats) {
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
        composable(Routes.NewChat) {
            NewChatScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.NewGroup) {
            NewGroupScreen(onBack = back, onNavigate = go)
        }
        composable(Routes.NewStatus) {
            NewStatusScreen(onBack = back, onNavigate = go)
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
        composable(Routes.SettingsAccount) {
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
        composable(Routes.SettingsPrivacy) {
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
        composable(Routes.SettingsProfile) {
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
}
