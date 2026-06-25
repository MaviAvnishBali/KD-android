package com.kiladarbar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kiladarbar.data.local.SessionManager
import com.kiladarbar.ui.screens.auth.LoginScreen
import com.kiladarbar.ui.screens.auth.OtpScreen
import com.kiladarbar.ui.screens.home.HomeScreen
import com.kiladarbar.ui.screens.splash.SplashScreen
import com.kiladarbar.ui.screens.menu.MenuScreen
import com.kiladarbar.ui.screens.menu.ItemDetailScreen
import com.kiladarbar.ui.screens.cart.CartScreen
import com.kiladarbar.ui.screens.orders.OrdersScreen
import com.kiladarbar.ui.screens.orders.OrderTrackingScreen
import com.kiladarbar.ui.screens.partyhall.PartyHallScreen
import com.kiladarbar.ui.screens.profile.ProfileScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Splash        : Screen("splash")
    object Login         : Screen("login")
    object Otp           : Screen("otp/{phone}") {
        fun createRoute(phone: String) = "otp/$phone"
    }
    object Home          : Screen("home")
    object Menu          : Screen("menu?categoryId={categoryId}") {
        fun createRoute(categoryId: Int? = null) = if (categoryId != null) "menu?categoryId=$categoryId" else "menu"
    }
    object ItemDetail    : Screen("item/{itemId}") {
        fun createRoute(itemId: String) = "item/$itemId"
    }
    object Cart          : Screen("cart")
    object Checkout      : Screen("checkout")
    object Orders        : Screen("orders")
    object OrderTracking : Screen("orders/{orderId}/track") {
        fun createRoute(orderId: String) = "orders/$orderId/track"
    }
    object Reservations  : Screen("reservations")
    object PartyHall     : Screen("party-hall")
    object Profile       : Screen("profile")
    object Loyalty       : Screen("loyalty")
    object Offers        : Screen("offers")
}

@Composable
fun KilaDarbarNavGraph(
    navController: NavHostController = rememberNavController(),
    sessionManager: SessionManager,
) {
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onFinished = {
                    scope.launch {
                        val destination = if (sessionManager.isLoggedIn()) Screen.Home.route else Screen.Login.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onOtpSent = { phone ->
                    navController.navigate(Screen.Otp.createRoute(phone))
                },
                onLoggedIn = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Otp.route) { backStack ->
            val phone = backStack.arguments?.getString("phone") ?: ""
            OtpScreen(
                phone = phone,
                onVerified = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onMenuClick     = { navController.navigate(Screen.Menu.createRoute()) },
                onCategoryClick = { id -> navController.navigate(Screen.Menu.createRoute(id)) },
                onItemClick     = { id -> navController.navigate(Screen.ItemDetail.createRoute(id)) },
                onCartClick     = { navController.navigate(Screen.Cart.route) },
                onOrdersClick   = { navController.navigate(Screen.Orders.route) },
                onProfileClick  = { navController.navigate(Screen.Profile.route) },
                onPartyHallClick = { navController.navigate(Screen.PartyHall.route) },
            )
        }

        composable(Screen.PartyHall.route) {
            PartyHallScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Menu.route) { backStack ->
            val categoryId = backStack.arguments?.getString("categoryId")?.toIntOrNull()
            MenuScreen(
                initialCategoryId = categoryId,
                onItemClick = { id -> navController.navigate(Screen.ItemDetail.createRoute(id)) },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onBack      = { navController.popBackStack() },
            )
        }

        composable(Screen.ItemDetail.route) { backStack ->
            val itemId = backStack.arguments?.getString("itemId") ?: ""
            ItemDetailScreen(
                itemId      = itemId,
                onBack      = { navController.popBackStack() },
                onCartClick = { navController.navigate(Screen.Cart.route) },
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                onBack             = { navController.popBackStack() },
                onCheckout         = { navController.navigate(Screen.Checkout.route) },
                onContinueShopping = { navController.navigate(Screen.Menu.createRoute()) },
                onLoginRequired    = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Cart.route) { saveState = true }
                    }
                },
            )
        }

        composable(Screen.Orders.route) {
            OrdersScreen(
                onOrderClick = { orderId -> navController.navigate(Screen.OrderTracking.createRoute(orderId)) },
                onBack       = { navController.popBackStack() },
                onOrderNow   = { navController.navigate(Screen.Menu.createRoute()) },
            )
        }

        composable(Screen.OrderTracking.route) { backStack ->
            val orderId = backStack.arguments?.getString("orderId") ?: ""
            OrderTrackingScreen(
                orderId = orderId,
                onBack  = { navController.popBackStack() },
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack         = { navController.popBackStack() },
                onOrdersClick  = { navController.navigate(Screen.Orders.route) },
                onLoyaltyClick = { navController.navigate(Screen.Loyalty.route) },
                onLogout       = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
