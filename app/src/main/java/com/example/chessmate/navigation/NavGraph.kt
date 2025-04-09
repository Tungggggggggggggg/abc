package com.example.chessmate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.example.chessmate.ui.screen.*

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "home"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("home") { HomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("find_friends") { FindFriendsScreen(navController) }
        composable("loading") { LoadingScreen(navController = navController) }
        composable("reset_password") { ResetPasswordScreen(navController) }
        composable("main_screen") { MainScreen(navController) }
        composable("match_history") { MatchHistoryScreen(navController) }
        composable("chat") { ChatScreen(navController) }
        composable("play_with_ai") { PlayWithAIScreen(navController) }
        composable("play_with_friend") { PlayWithFriendScreen(navController) }
        composable(
            route = "play_with_opponent/{matchId}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "chessmate://play_with_opponent/{matchId}" }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            PlayWithOpponentScreen(
                navController = navController,
                matchId = matchId
            ) // Gọi với tham số matchId
        }
    }
}