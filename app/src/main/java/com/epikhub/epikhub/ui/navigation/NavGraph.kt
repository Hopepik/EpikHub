package com.epikhub.epikhub.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.epikhub.epikhub.data.model.UserRole
import com.epikhub.epikhub.ui.components.*
import com.epikhub.epikhub.ui.screens.admin.*
import com.epikhub.epikhub.ui.screens.auth.*
import com.epikhub.epikhub.ui.screens.customer.*
import com.epikhub.epikhub.util.DefaultNetworkObserver

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val networkObserver = remember { DefaultNetworkObserver(context) }
    val isOnline by networkObserver.isOnline.collectAsStateWithLifecycle()
    val isOffline = !isOnline

    DisposableEffect(Unit) {
        onDispose {
            networkObserver.unregister()
        }
    }

    val currentUser = authState.user
    val isLoggedIn = authViewModel.isLoggedIn
    val isCompanyAdmin = currentUser?.isCompanyAdmin == true
    val isCustomer = currentUser?.isCustomer == true
    val isMissingProfile = authState.isMissingProfile
    val companyId = currentUser?.companyId ?: ""

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.CustomerRequests.route,
        Screen.CustomerProfile.route,
        Screen.AdminDashboard.route,
        Screen.AdminServices.route,
        Screen.AdminRequests.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    if (isCompanyAdmin) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard") },
                            selected = currentRoute == Screen.AdminDashboard.route,
                            onClick = {
                                navController.navigate(Screen.AdminDashboard.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = "Requests") },
                            label = { Text("Requests") },
                            selected = currentRoute == Screen.AdminRequests.route,
                            onClick = {
                                navController.navigate(Screen.AdminRequests.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.MiscellaneousServices, contentDescription = "Services") },
                            label = { Text("Services") },
                            selected = currentRoute == Screen.AdminServices.route,
                            onClick = {
                                navController.navigate(Screen.AdminServices.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    } else {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Explore") },
                            selected = currentRoute == Screen.Home.route,
                            onClick = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        if (isLoggedIn) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Requests") },
                                label = { Text("My Requests") },
                                selected = currentRoute == Screen.CustomerRequests.route,
                                onClick = {
                                    navController.navigate(Screen.CustomerRequests.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("Profile") },
                                selected = currentRoute == Screen.CustomerProfile.route,
                                onClick = {
                                    navController.navigate(Screen.CustomerProfile.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OfflineBanner(isOffline = isOffline)

            NavHost(
                navController = navController,
                startDestination = if (isCompanyAdmin) Screen.AdminDashboard.route else Screen.Home.route,
                modifier = Modifier.weight(1f)
            ) {
                // Customer & Guest Routes
                composable(Screen.Home.route) {
                    val homeViewModel: HomeViewModel = viewModel()
                    HomeScreen(
                        viewModel = homeViewModel,
                        onCompanyClick = { cId -> navController.navigate(Screen.CompanyDetail.createRoute(cId)) },
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        isLoggedIn = isLoggedIn
                    )
                }

                composable(
                    route = Screen.CompanyDetail.route,
                    arguments = listOf(navArgument("companyId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val cId = backStackEntry.arguments?.getString("companyId") ?: ""
                    val companyDetailViewModel: CompanyDetailViewModel = viewModel()
                    CompanyDetailScreen(
                        companyId = cId,
                        viewModel = companyDetailViewModel,
                        onServiceClick = { sId -> navController.navigate(Screen.ServiceDetail.createRoute(sId)) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.ServiceDetail.route,
                    arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val sId = backStackEntry.arguments?.getString("serviceId") ?: ""
                    val serviceDetailViewModel: ServiceDetailViewModel = viewModel()
                    ServiceDetailScreen(
                        serviceId = sId,
                        viewModel = serviceDetailViewModel,
                        onRequestClick = { targetServiceId ->
                            if (isLoggedIn) {
                                if (isCustomer) {
                                    navController.navigate(Screen.SubmitRequest.createRoute(targetServiceId))
                                } else if (isMissingProfile || currentUser == null) {
                                    navController.navigate(Screen.Login.route)
                                }
                            } else {
                                navController.navigate(Screen.Login.route)
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.SubmitRequest.route,
                    arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val sId = backStackEntry.arguments?.getString("serviceId") ?: ""
                    val submitRequestViewModel: SubmitRequestViewModel = viewModel()
                    SubmitRequestScreen(
                        serviceId = sId,
                        viewModel = submitRequestViewModel,
                        customerId = authViewModel.currentUserId ?: "",
                        customerName = currentUser?.fullName ?: "",
                        customerPhone = currentUser?.phone ?: "",
                        onRequestSubmitted = {
                            navController.popBackStack()
                            navController.navigate(Screen.CustomerRequests.route)
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.CustomerRequests.route) {
                    val customerRequestsViewModel: CustomerRequestsViewModel = viewModel()
                    CustomerRequestsScreen(
                        customerId = authViewModel.currentUserId ?: "",
                        viewModel = customerRequestsViewModel,
                        onRequestClick = { rId -> navController.navigate(Screen.RequestDetail.createRoute(rId)) }
                    )
                }

                composable(Screen.CustomerProfile.route) {
                    CustomerProfileScreen(
                        user = currentUser,
                        onUpdateProfile = { name, phone -> authViewModel.updateProfile(name, phone) },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0)
                            }
                        }
                    )
                }

                // Auth Routes
                composable(Screen.Login.route) {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                            authViewModel.checkCurrentUser()
                            navController.popBackStack()
                        },
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        authViewModel = authViewModel,
                        onRegisterSuccess = {
                            authViewModel.checkCurrentUser()
                            navController.popBackStack(Screen.Home.route, false)
                        },
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Admin Routes
                composable(Screen.AdminDashboard.route) {
                    val adminDashboardViewModel: AdminDashboardViewModel = viewModel()
                    AdminDashboardScreen(
                        companyId = companyId,
                        viewModel = adminDashboardViewModel,
                        onNavigateToServices = { navController.navigate(Screen.AdminServices.route) },
                        onNavigateToRequests = { navController.navigate(Screen.AdminRequests.route) },
                        onNavigateToEditCompany = { navController.navigate(Screen.CompanyProfileEdit.route) },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0)
                            }
                        }
                    )
                }

                composable(Screen.AdminServices.route) {
                    val adminServicesViewModel: AdminServicesViewModel = viewModel()
                    AdminServicesScreen(
                        companyId = companyId,
                        viewModel = adminServicesViewModel,
                        onAddServiceClick = { navController.navigate(Screen.AddEditService.createRoute(null)) },
                        onEditServiceClick = { sId -> navController.navigate(Screen.AddEditService.createRoute(sId)) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.AddEditService.route,
                    arguments = listOf(navArgument("serviceId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStackEntry ->
                    val sId = backStackEntry.arguments?.getString("serviceId")
                    val addEditServiceViewModel: AddEditServiceViewModel = viewModel()
                    AddEditServiceScreen(
                        companyId = companyId,
                        serviceId = sId,
                        viewModel = addEditServiceViewModel,
                        onSaved = { navController.popBackStack() },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminRequests.route) {
                    val adminRequestsViewModel: AdminRequestsViewModel = viewModel()
                    AdminRequestsScreen(
                        companyId = companyId,
                        viewModel = adminRequestsViewModel,
                        onRequestClick = { rId -> navController.navigate(Screen.AdminRequestDetail.createRoute(rId)) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.AdminRequestDetail.route,
                    arguments = listOf(navArgument("requestId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val rId = backStackEntry.arguments?.getString("requestId") ?: ""
                    val adminRequestDetailViewModel: AdminRequestDetailViewModel = viewModel()
                    AdminRequestDetailScreen(
                        requestId = rId,
                        viewModel = adminRequestDetailViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.CompanyProfileEdit.route) {
                    val companyProfileEditViewModel: CompanyProfileEditViewModel = viewModel()
                    CompanyProfileEditScreen(
                        companyId = companyId,
                        viewModel = companyProfileEditViewModel,
                        onSaved = { navController.popBackStack() },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
