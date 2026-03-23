package com.pan_de_casa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pan_de_casa.ui.admin.AdminCatalogScreen
import com.pan_de_casa.ui.admin.AdminDashboardScreen
import com.pan_de_casa.ui.admin.AdminOrdersScreen
import com.pan_de_casa.ui.admin.ProductFormScreen
import com.pan_de_casa.ui.auth.LoginScreen
import com.pan_de_casa.ui.auth.RegisterScreen
import com.pan_de_casa.ui.cart.CartScreen
import com.pan_de_casa.ui.order.CheckoutScreen
import com.pan_de_casa.ui.order.OrderConfirmationScreen
import com.pan_de_casa.ui.order.OrderStatusScreen
import com.pan_de_casa.ui.product.ClientCatalogScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = { role ->
                    if (role.name == "ADMIN") {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.ClientCatalog.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { 
                    navController.navigate(Screen.ClientCatalog.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ClientCatalog.route) {
            ClientCatalogScreen(
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Cart.route) {
            CartScreen(
                onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onOrderConfirmed = { orderId ->
                    navController.navigate("confirmation/$orderId") {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "confirmation/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderConfirmationScreen(
                orderId = orderId,
                onTrackOrder = { navController.navigate("status/$orderId") },
                onGoHome = { 
                    navController.navigate(Screen.ClientCatalog.route) {
                        popUpTo(Screen.ClientCatalog.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "status/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderStatusScreen(
                orderId = orderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Admin Routes
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateToOrders = { navController.navigate(Screen.AdminOrders.route) },
                onNavigateToCatalog = { navController.navigate(Screen.AdminCatalog.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.AdminOrders.route) {
            AdminOrdersScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminCatalog.route) { 
            AdminCatalogScreen(
                onNavigateToProductForm = { productId ->
                    navController.navigate(
                        Screen.ProductForm.route.replace("{productId}", productId ?: "{productId}")
                    )
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.ProductForm.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductFormScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
