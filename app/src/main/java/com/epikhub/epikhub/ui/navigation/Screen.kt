package com.epikhub.epikhub.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CompanyDetail : Screen("company_detail/{companyId}") {
        fun createRoute(companyId: String) = "company_detail/$companyId"
    }
    object ServiceDetail : Screen("service_detail/{serviceId}") {
        fun createRoute(serviceId: String) = "service_detail/$serviceId"
    }
    object SubmitRequest : Screen("submit_request/{serviceId}") {
        fun createRoute(serviceId: String) = "submit_request/$serviceId"
    }
    object CustomerRequests : Screen("customer_requests")
    object RequestDetail : Screen("request_detail/{requestId}") {
        fun createRoute(requestId: String) = "request_detail/$requestId"
    }
    object CustomerProfile : Screen("customer_profile")
    object Login : Screen("login")
    object Register : Screen("register")

    // Admin Routes
    object AdminDashboard : Screen("admin_dashboard")
    object AdminServices : Screen("admin_services")
    object AddEditService : Screen("add_edit_service?serviceId={serviceId}") {
        fun createRoute(serviceId: String? = null) = if (!serviceId.isNullOrEmpty()) "add_edit_service?serviceId=$serviceId" else "add_edit_service"
    }
    object AdminRequests : Screen("admin_requests")
    object AdminRequestDetail : Screen("admin_request_detail/{requestId}") {
        fun createRoute(requestId: String) = "admin_request_detail/$requestId"
    }
    object CompanyProfileEdit : Screen("company_profile_edit")
}
