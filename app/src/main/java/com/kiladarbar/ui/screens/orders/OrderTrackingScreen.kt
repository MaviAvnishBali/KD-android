package com.kiladarbar.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiladarbar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: OrderTrackingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
        viewModel.startTracking(orderId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopTracking() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Order #${uiState.orderNumber ?: orderId.take(8)}", fontWeight = FontWeight.Bold)
                        Text(
                            "Placed at ${uiState.orderTime ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalMaroon, titleContentColor = Color.White),
                actions = {
                    IconButton(onClick = { /* call support */ }) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Status progress
            OrderStatusTimeline(currentStatus = uiState.status ?: "PENDING")

            // ETA card
            uiState.estimatedMinutes?.let { eta ->
                EtaCard(estimatedMinutes = eta, status = uiState.status ?: "PENDING")
            }

            // Driver info (for delivery orders)
            uiState.driverInfo?.let { driver ->
                DriverCard(
                    driverName = driver.name,
                    driverPhone = driver.phone,
                    vehicleNumber = driver.vehicleNumber,
                    rating = driver.rating,
                    onCallDriver = { /* call driver */ },
                    onChatDriver = { /* chat */ },
                )
            }

            // Order items summary
            OrderItemsSummary(items = uiState.items ?: emptyList())

            // Amount summary
            OrderAmountCard(
                subtotal = uiState.subtotal ?: 0.0,
                deliveryCharge = uiState.deliveryCharge ?: 0.0,
                gst = uiState.gst ?: 0.0,
                discount = uiState.discount ?: 0.0,
                total = uiState.total ?: 0.0,
            )

            // Action buttons
            if (uiState.status == "PENDING" || uiState.status == "CONFIRMED") {
                OutlinedButton(
                    onClick = { viewModel.cancelOrder() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel Order")
                }
            }
        }
    }
}

@Composable
private fun OrderStatusTimeline(currentStatus: String) {
    val steps = listOf(
        "PENDING"           to "Order Placed",
        "CONFIRMED"         to "Confirmed",
        "PREPARING"         to "Preparing",
        "READY"             to "Ready",
        "OUT_FOR_DELIVERY"  to "Out for Delivery",
        "DELIVERED"         to "Delivered",
    )

    val currentIndex = steps.indexOfFirst { it.first == currentStatus }.takeIf { it >= 0 } ?: 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                "Order Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RoyalMaroon,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            steps.forEachIndexed { index, (status, label) ->
                val isDone    = index < currentIndex
                val isCurrent = index == currentIndex

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    color = when {
                                        isDone    -> RoyalMaroon
                                        isCurrent -> RoyalGold
                                        else      -> Color.LightGray
                                    },
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isDone) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                            } else if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color.White, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color.White, CircleShape)
                                )
                            }
                        }

                        if (index < steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(32.dp)
                                    .background(
                                        if (isDone) RoyalMaroon else Color.LightGray
                                    )
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isCurrent -> RoyalGold
                                isDone    -> MaterialTheme.colorScheme.onSurface
                                else      -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            }
                        )
                        if (index < steps.size - 1) Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EtaCard(estimatedMinutes: Int, status: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalGold.copy(alpha = 0.1f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = if (status == "DELIVERED") "Delivered!" else "Estimated Delivery",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Text(
                    text = if (status == "DELIVERED") "Your order has arrived" else "~$estimatedMinutes minutes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = RoyalMaroon,
                )
            }
            Icon(
                if (status == "DELIVERED") Icons.Default.CheckCircle else Icons.Default.Schedule,
                contentDescription = null,
                tint = RoyalGold,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

@Composable
private fun DriverCard(
    driverName: String,
    driverPhone: String,
    vehicleNumber: String?,
    rating: Float,
    onCallDriver: () -> Unit,
    onChatDriver: () -> Unit,
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(RoyalMaroon.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = RoyalMaroon)
                }
                Column {
                    Text(driverName, fontWeight = FontWeight.SemiBold)
                    Text(
                        "⭐ $rating · ${vehicleNumber ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onCallDriver,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = RoyalMaroon.copy(alpha = 0.1f)),
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = RoyalMaroon)
                }
                IconButton(
                    onClick = onChatDriver,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = RoyalGold.copy(alpha = 0.1f)),
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat", tint = RoyalGold)
                }
            }
        }
    }
}

@Composable private fun OrderItemsSummary(items: List<Any>) {}
@Composable private fun OrderAmountCard(subtotal: Double, deliveryCharge: Double, gst: Double, discount: Double, total: Double) {}
