package com.educonsult.crm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.educonsult.crm.ui.auth.AuthEvent
import com.educonsult.crm.ui.auth.AuthViewModel
import com.educonsult.crm.ui.dashboard.DashboardScreen
import com.educonsult.crm.ui.conflicts.ConflictListScreen
import com.educonsult.crm.ui.leads.detail.LeadDetailScreen
import com.educonsult.crm.ui.leads.edit.LeadEditScreen
import com.educonsult.crm.ui.leads.list.LeadListScreen
import com.educonsult.crm.ui.courses.list.CourseListScreen
import com.educonsult.crm.ui.courses.detail.CourseDetailScreen

@Composable
fun EduConsultNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToHome -> {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
                is AuthEvent.NavigateToLogin -> {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                is AuthEvent.ShowError -> {
                    // Handle error display (Snackbar, Toast, etc.)
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth Navigation
        composable(Screen.Onboarding.route) {
            // TODO: OnboardingScreen()
        }

        composable(Screen.Login.route) {
            // TODO: LoginScreen(
            //     viewModel = authViewModel,
            //     onNavigateToRegister = { navController.navigate(Screen.Register.route) },
            //     onNavigateToForgotPassword = { }
            // )
        }

        composable(Screen.Register.route) {
            // TODO: RegisterScreen(
            //     onNavigateToOtp = { phone -> 
            //         navController.navigate(Screen.OtpVerification.createRoute(phone)) 
            //     },
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }

        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            // TODO: OtpVerificationScreen(
            //     phone = phone,
            //     onVerificationSuccess = { 
            //         navController.navigate(Screen.Dashboard.route) {
            //             popUpTo(Screen.Login.route) { inclusive = true }
            //         }
            //     },
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }

        // Main Navigation
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToCourses = {
                    navController.navigate(Screen.CourseList.route)
                },
                onNavigateToConflicts = {
                    navController.navigate(Screen.Conflicts.route)
                }
            )
        }

        // Lead Navigation
        composable(Screen.LeadList.route) {
            LeadListScreen(
                onLeadClick = { leadId ->
                    navController.navigate(Screen.LeadDetail.createRoute(leadId))
                },
                onAddLeadClick = {
                    navController.navigate(Screen.EditLead.createRoute("new"))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.LeadDetail.route,
            arguments = listOf(navArgument("leadId") { type = NavType.StringType })
        ) { backStackEntry ->
            val leadId = backStackEntry.arguments?.getString("leadId") ?: ""
            LeadDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditLead.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.EditLead.route,
            arguments = listOf(navArgument("leadId") { type = NavType.StringType })
        ) { backStackEntry ->
            val leadId = backStackEntry.arguments?.getString("leadId") ?: "new"
            val isNewLead = leadId == "new"
            LeadEditScreen(
                onNavigateBack = { navController.popBackStack() },
                onLeadSaved = { savedLeadId ->
                    navController.popBackStack()
                    if (isNewLead) {
                        navController.navigate(Screen.LeadDetail.createRoute(savedLeadId)) {
                            popUpTo(Screen.LeadList.route)
                        }
                    }
                }
            )
        }

        // Settings Navigation
        composable(Screen.Settings.route) {
            // TODO: SettingsScreen(
            //     onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
            //     onLogout = { authViewModel.logout() },
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }

        composable(Screen.CourseList.route) {
            CourseListScreen(
                onNavigateBack = { navController.popBackStack() },
                onCourseClick = { courseId ->
                    navController.navigate(Screen.CourseDetail.createRoute(courseId))
                }
            )
        }

        composable(Screen.Conflicts.route) {
            ConflictListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CourseDetail.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) {
            CourseDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            // TODO: ProfileScreen(
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }
    }
}
