package org.eos.mynoti.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import org.eos.mynoti.di.AppContainer
import org.eos.mynoti.di.LocalAppContainer
import org.eos.mynoti.ui.calendar.CalendarRoute
import org.eos.mynoti.ui.components.BottomNavigationBar
import org.eos.mynoti.ui.home.HomeRoute
import org.eos.mynoti.ui.notification.NotificationDetailRoute
import org.eos.mynoti.ui.settings.SettingsRoute
import org.eos.mynoti.ui.summary.SummaryRoute

@Composable
fun MyNotiApp(container: AppContainer) {
    CompositionLocalProvider(LocalAppContainer provides container) {
        val navController = rememberNavController()
        MyNotiNavHost(navController = navController)
    }
}

@Composable
fun MyNotiNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeRoute(
                    onNotificationClick = { id ->
                        navController.navigate(Routes.notificationDetail(id))
                    },
                    onSettingsClick = {
                        navController.navigate(Routes.SETTINGS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Routes.SUMMARY) {
                SummaryRoute(
                    onNotificationClick = { id ->
                        navController.navigate(Routes.notificationDetail(id))
                    }
                )
            }
            composable(Routes.CALENDAR) {
                CalendarRoute(
                    onNotificationClick = { id ->
                        navController.navigate(Routes.notificationDetail(id))
                    },
                    onSettingsClick = {
                        navController.navigate(Routes.SETTINGS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsRoute(
                    onBack = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(
                route = Routes.NOTIFICATION_DETAIL,
                arguments = listOf(
                    navArgument("notificationId") { type = NavType.LongType }
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "mynoti://notification/{notificationId}" }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("notificationId") ?: return@composable
                NotificationDetailRoute(
                    notificationId = id,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
