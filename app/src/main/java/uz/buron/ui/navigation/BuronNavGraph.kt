package uz.buron.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import uz.buron.R
import uz.buron.data.repository.AuthRepository
import uz.buron.ui.screens.auth.LoginScreen
import uz.buron.ui.screens.auth.RegisterScreen
import uz.buron.ui.screens.contact.ContactScreen
import uz.buron.ui.screens.home.HomeScreen
import uz.buron.ui.screens.mybookings.MyBookingsScreen
import uz.buron.ui.screens.search.SearchScreen
import uz.buron.ui.screens.venue.VenueDetailScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import javax.inject.Inject

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search?query={query}") {
        fun createRoute(query: String = "") = "search?query=${UriEncoder.encode(query)}"
    }
    data object VenueDetail : Screen("venue/{venueId}") {
        fun createRoute(venueId: String) = "venue/$venueId"
    }
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object MyBookings : Screen("my_bookings")
    data object Contact : Screen("contact")
}

private object UriEncoder {
    fun encode(value: String): String = java.net.URLEncoder.encode(value, Charsets.UTF_8.name())
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    val authState = authRepository.authState

    suspend fun validateSession() {
        authRepository.validateSession()
    }

    fun logout() {
        authRepository.logout()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuronApp(appViewModel: AppViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val authState by appViewModel.authState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        appViewModel.validateSession()
    }

    val bottomNavRoutes = setOf(
        Screen.Home.route,
        Screen.Search.route.substringBefore("?"),
        Screen.MyBookings.route,
        Screen.Contact.route
    )

    val showBottomBar = currentRoute?.let { route ->
        bottomNavRoutes.any { route.startsWith(it) }
    } ?: true

    val showTopBar = currentRoute !in setOf(Screen.Login.route, Screen.Register.route)

    Scaffold(
        topBar = {
            if (showTopBar) {
                BuronTopBar(
                    isLoggedIn = authState.isLoggedIn,
                    userName = authState.user?.firstName,
                    onLogin = { navController.navigate(Screen.Login.route) },
                    onRegister = { navController.navigate(Screen.Register.route) },
                    onLogout = {
                        appViewModel.logout()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_home)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute?.startsWith("search") == true,
                        onClick = {
                            navController.navigate(Screen.Search.createRoute()) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Search, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_search)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.MyBookings.route,
                        onClick = {
                            if (authState.isLoggedIn) {
                                navController.navigate(Screen.MyBookings.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                navController.navigate(Screen.Login.route)
                            }
                        },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_bookings)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Contact.route,
                        onClick = {
                            navController.navigate(Screen.Contact.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.ContactPhone, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_contact)) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSearch = { query ->
                        navController.navigate(Screen.Search.createRoute(query))
                    },
                    onNavigateToVenue = { id ->
                        navController.navigate(Screen.VenueDetail.createRoute(id))
                    },
                    onNavigateToContact = {
                        navController.navigate(Screen.Contact.route)
                    }
                )
            }

            composable(
                route = "search?query={query}",
                arguments = listOf(navArgument("query") { type = NavType.StringType; defaultValue = "" })
            ) { backStackEntry ->
                val query = backStackEntry.arguments?.getString("query").orEmpty()
                SearchScreen(
                    initialQuery = query,
                    onNavigateToVenue = { id ->
                        navController.navigate(Screen.VenueDetail.createRoute(id))
                    }
                )
            }

            composable(
                route = Screen.VenueDetail.route,
                arguments = listOf(navArgument("venueId") { type = NavType.StringType })
            ) { backStackEntry ->
                val venueId = backStackEntry.arguments?.getString("venueId") ?: return@composable
                VenueDetailScreen(
                    venueId = venueId,
                    snackbarHostState = snackbarHostState
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    snackbarHostState = snackbarHostState
                )
            }

            composable(Screen.MyBookings.route) {
                MyBookingsScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.createRoute())
                    },
                    onNavigateToVenue = { id ->
                        navController.navigate(Screen.VenueDetail.createRoute(id))
                    },
                    snackbarHostState = snackbarHostState
                )
            }

            composable(Screen.Contact.route) {
                ContactScreen(snackbarHostState = snackbarHostState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuronTopBar(
    isLoggedIn: Boolean,
    userName: String?,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Buron") },
        actions = {
            if (isLoggedIn) {
                Text(
                    text = userName.orEmpty(),
                    modifier = Modifier.padding(end = 4.dp)
                )
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.logout)) },
                        onClick = {
                            menuExpanded = false
                            onLogout()
                        }
                    )
                }
            } else {
                TextButton(onClick = onLogin) {
                    Text(stringResource(R.string.login_title))
                }
                TextButton(onClick = onRegister) {
                    Text(stringResource(R.string.register_title))
                }
            }
        }
    )
}
