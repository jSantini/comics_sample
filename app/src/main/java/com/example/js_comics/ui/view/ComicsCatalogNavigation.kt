package com.example.js_comics.ui.view

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/** One shared catalog; destinations carry only an ID, never serialized content. */
@Composable
fun ComicsCatalogNavigation(
    state: ComicsUiState,
    onRetry: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController, startDestination = "catalog") {
        composable("catalog") {
            ComicsListScreen(state, onRetry) { id ->
                navController.navigate("comic/$id") { launchSingleTop = true }
            }
        }
        composable("comic/{comicId}", arguments = listOf(navArgument("comicId") { type = NavType.StringType })) { entry ->
            ComicDetailsScreen(
                state, entry.arguments?.getString("comicId")?.toIntOrNull(),
                onBack = {
                    if (!navController.popBackStack()) navController.navigate("catalog") { launchSingleTop = true }
                },
                onRetry = onRetry,
            )
        }
    }
}
