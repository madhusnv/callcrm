package com.educonsult.crm.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.educonsult.crm.ui.leads.detail.LeadDetailScreen
import com.educonsult.crm.ui.leads.edit.LeadEditScreen
import com.educonsult.crm.ui.leads.list.LeadListScreen

sealed class LeadScreen(val route: String) {
    data object List : LeadScreen("leads")
    
    data object Detail : LeadScreen("leads/{leadId}") {
        fun createRoute(leadId: String) = "leads/$leadId"
    }
    
    data object Edit : LeadScreen("leads/{leadId}/edit") {
        fun createRoute(leadId: String?) = "leads/${leadId ?: "new"}/edit"
    }
}

fun NavGraphBuilder.leadNavGraph(navController: NavController) {
    navigation(
        startDestination = LeadScreen.List.route,
        route = "lead_graph"
    ) {
        composable(LeadScreen.List.route) {
            LeadListScreen(
                onLeadClick = { leadId ->
                    navController.navigate(LeadScreen.Detail.createRoute(leadId))
                },
                onAddLeadClick = {
                    navController.navigate(LeadScreen.Edit.createRoute(null))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = LeadScreen.Detail.route,
            arguments = listOf(
                navArgument("leadId") { type = NavType.StringType }
            )
        ) {
            LeadDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { leadId ->
                    navController.navigate(LeadScreen.Edit.createRoute(leadId))
                }
            )
        }

        composable(
            route = LeadScreen.Edit.route,
            arguments = listOf(
                navArgument("leadId") { type = NavType.StringType }
            )
        ) {
            LeadEditScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLeadSaved = { leadId ->
                    navController.popBackStack()
                    if (navController.currentDestination?.route != LeadScreen.Detail.route) {
                        navController.navigate(LeadScreen.Detail.createRoute(leadId)) {
                            popUpTo(LeadScreen.List.route)
                        }
                    }
                }
            )
        }
    }
}
