package com.pan_de_casa.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ClientCatalog : Screen("client_catalog")
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object OrderStatus : Screen("order_status")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminOrders : Screen("admin_orders")
    object AdminCatalog : Screen("admin_catalog")
    object ProductForm : Screen("product_form/{productId}") {
        fun createRoute(productId: String) = "product_form/$productId"
    }
}
