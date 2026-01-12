package com.educonsult.crm.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object OtpVerification : Screen("otp_verification/{phone}") {
        fun createRoute(phone: String) = "otp_verification/$phone"
    }

    // Main
    object Dashboard : Screen("dashboard")
    object LeadList : Screen("leads")
    object LeadDetail : Screen("lead/{leadId}") {
        fun createRoute(leadId: String) = "lead/$leadId"
    }
    object AddLead : Screen("edit_lead/new")
    object EditLead : Screen("edit_lead/{leadId}") {
        fun createRoute(leadId: String) = "edit_lead/$leadId"
    }

    // Settings
    object Settings : Screen("settings")
    object Profile : Screen("profile")
}
