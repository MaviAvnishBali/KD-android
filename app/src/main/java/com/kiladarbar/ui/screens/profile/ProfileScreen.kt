package com.kiladarbar.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiladarbar.data.remote.dto.AddressDto
import com.kiladarbar.ui.components.*
import com.kiladarbar.ui.theme.*

@Composable
fun ProfileScreen(
    onBack:         () -> Unit,
    onOrdersClick:  () -> Unit,
    onLoyaltyClick: () -> Unit,
    onLogout:       () -> Unit,
    viewModel:      ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar("Error: $it"); viewModel.clearMessage() }
    }

    // Edit profile sheet
    if (state.showEditProfile) {
        EditProfileSheet(state = state, vm = viewModel)
    }

    // Address add/edit sheet
    if (state.showAddressSheet) {
        AddressSheet(state = state, vm = viewModel)
    }

    Scaffold(
        containerColor   = Obsidian,
        snackbarHost     = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier              = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ivory) }
                Text("MY PROFILE", style = MaterialTheme.typography.titleSmall, color = Ivory, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = viewModel::openEditProfile) { Icon(Icons.Filled.Edit, null, tint = Gold) }
            }
        },
    ) { padding ->
        when {
            state.isLoading -> ProfileShimmer(Modifier.padding(padding))
            else -> ProfileContent(
                state        = state,
                modifier     = Modifier.padding(padding),
                onOrdersClick = onOrdersClick,
                onLoyaltyClick = onLoyaltyClick,
                onEditProfile  = viewModel::openEditProfile,
                onAddAddress   = viewModel::openAddAddress,
                onEditAddress  = viewModel::openEditAddress,
                onDeleteAddress = viewModel::deleteAddress,
                onSetDefault   = viewModel::setDefaultAddress,
                onLogout       = { viewModel.logout(onLogout) },
            )
        }
    }
}

/* ── Main scrollable content ── */
@Composable
private fun ProfileContent(
    state:           ProfileUiState,
    modifier:        Modifier,
    onOrdersClick:   () -> Unit,
    onLoyaltyClick:  () -> Unit,
    onEditProfile:   () -> Unit,
    onAddAddress:    () -> Unit,
    onEditAddress:   (AddressDto) -> Unit,
    onDeleteAddress: (String) -> Unit,
    onSetDefault:    (String) -> Unit,
    onLogout:        () -> Unit,
) {
    Column(
        modifier            = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(4.dp))

        // Profile header card
        ProfileHeaderCard(state = state, onEdit = onEditProfile)

        // Loyalty card
        LoyaltyProgressCard(points = state.loyaltyPoints, tier = state.loyaltyTier, onClick = onLoyaltyClick)

        // Stats row
        StatsRow(orders = state.totalOrders, spent = state.totalSpent, addresses = state.addresses.size)

        // Addresses section
        AddressesSection(
            addresses       = state.addresses,
            onAdd           = onAddAddress,
            onEdit          = onEditAddress,
            onDelete        = onDeleteAddress,
            onSetDefault    = onSetDefault,
        )

        // Menu sections
        ProfileMenuGroup(
            title = "Orders & Bookings",
            items = listOf(
                MenuItem("My Orders",          Icons.Filled.ReceiptLong,  if (state.totalOrders > 0) "${state.totalOrders} orders" else "No orders yet", onOrdersClick),
                MenuItem("Table Bookings",      Icons.Filled.TableBar,     "Reserve a table", {}),
                MenuItem("Catering Enquiries",  Icons.Filled.Celebration,  "Plan a feast",    {}),
            ),
        )

        ProfileMenuGroup(
            title = "Preferences",
            items = listOf(
                MenuItem("Payment Methods",   Icons.Filled.CreditCard,    "Manage cards & UPI",  {}),
                MenuItem("Notifications",     Icons.Filled.Notifications, "Manage alerts",       {}),
                MenuItem("Language",          Icons.Filled.Language,      "English",             {}),
            ),
        )

        ProfileMenuGroup(
            title = "About & Support",
            items = listOf(
                MenuItem("Help & FAQs",        Icons.Filled.HelpOutline, "Get help",    {}),
                MenuItem("Rate the App",        Icons.Filled.StarRate,    "⭐⭐⭐⭐⭐",  {}),
                MenuItem("Refer a Friend",      Icons.Filled.CardGiftcard,"Earn ₹100",  {}),
                MenuItem("Privacy Policy",      Icons.Filled.Policy,      "",            {}),
                MenuItem("App Version",         Icons.Filled.Info,        "v1.0.0",     {}),
            ),
        )

        // Sign out
        if (!state.isGuest) {
            SignOutButton(onLogout = onLogout)
        } else {
            // Guest upsell
            GuestUpsell()
        }

        Spacer(Modifier.height(80.dp))
    }
}

/* ── Profile header card ── */
@Composable
private fun ProfileHeaderCard(state: ProfileUiState, onEdit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Surface1)
            .border(1.dp, GoldBorder, RoundedCornerShape(24.dp))
            .clickable { onEdit() }
            .padding(20.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Avatar
        Box(
            modifier = Modifier.size(76.dp).clip(CircleShape).background(LuxuryGradients.goldHorizontal),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = state.profile?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "G",
                fontSize   = 34.sp,
                color      = Obsidian,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                state.profile?.name ?: "Guest User",
                style      = MaterialTheme.typography.titleLarge,
                color      = Ivory,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            Text(
                state.profile?.phone ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = IvoryDim.copy(0.6f),
            )
            if (!state.profile?.email.isNullOrBlank()) {
                Text(
                    state.profile!!.email!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = IvoryDim.copy(0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RoyalBadge("👑 ${state.loyaltyTier} Member", isGold = true)
                if (state.profile?.verified == true) {
                    RoyalBadge("✓ Verified", isGold = false)
                }
            }
        }
        Icon(Icons.Filled.ChevronRight, null, tint = Gold.copy(0.6f), modifier = Modifier.size(20.dp))
    }
}

/* ── Loyalty progress card ── */
@Composable
private fun LoyaltyProgressCard(points: Int, tier: String, onClick: () -> Unit) {
    val tiers     = listOf("Silver" to 0, "Gold" to 2000, "Platinum" to 5000)
    val tierIdx   = tiers.indexOfFirst { it.first == tier }.coerceAtLeast(0)
    val nextTier  = tiers.getOrNull(tierIdx + 1)
    val progress  = if (nextTier != null)
        ((points - tiers[tierIdx].second).toFloat() / (nextTier.second - tiers[tierIdx].second)).coerceIn(0f, 1f)
    else 1f

    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(colorStops = arrayOf(0f to GoldDark.copy(0.2f), 1f to MaroonDark.copy(0.25f))))
            .border(1.dp, Gold.copy(0.25f), RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                EyebrowLabel("Royal Rewards")
                Text("$points pts", style = MaterialTheme.typography.headlineMedium, color = Gold, fontWeight = FontWeight.Bold)
            }
            Text("👑", fontSize = 42.sp)
        }
        if (nextTier != null) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text(tier, style = MaterialTheme.typography.labelSmall, color = Gold)
                    Text(nextTier.first, style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.5f))
                }
                Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Surface2)) {
                    val anim by animateFloatAsState(progress, tween(900, easing = FastOutSlowInEasing), label = "lp")
                    Box(Modifier.fillMaxWidth(anim).height(6.dp).clip(RoundedCornerShape(3.dp)).background(LuxuryGradients.goldHorizontal))
                }
                Text("${nextTier.second - points} pts to ${nextTier.first}", style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.5f))
            }
        }
    }
}

/* ── Stats row ── */
@Composable
private fun StatsRow(orders: Int, spent: Double, addresses: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(
            Triple(if (orders > 0) "$orders" else "0",          "Orders",    "📦"),
            Triple("₹%.0f".format(spent),                       "Total Spent","💰"),
            Triple(if (addresses > 0) "$addresses" else "0",    "Addresses", "📍"),
        ).forEach { (value, label, emoji) ->
            Column(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Surface1)
                    .border(0.5.dp, GoldBorder, RoundedCornerShape(16.dp)).padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(emoji, fontSize = 20.sp)
                Text(value, style = MaterialTheme.typography.titleMedium, color = Gold, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(label, style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.5f), fontSize = 10.sp)
            }
        }
    }
}

/* ── Addresses section ── */
@Composable
private fun AddressesSection(
    addresses:    List<AddressDto>,
    onAdd:        () -> Unit,
    onEdit:       (AddressDto) -> Unit,
    onDelete:     (String) -> Unit,
    onSetDefault: (String) -> Unit,
) {
    Column(
        modifier            = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Surface1)
            .border(1.dp, GoldBorder, RoundedCornerShape(20.dp)),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            EyebrowLabel("Saved Addresses")
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Gold.copy(0.15f))
                    .border(0.5.dp, Gold.copy(0.4f), RoundedCornerShape(8.dp)).clickable { onAdd() }.padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.Add, null, tint = Gold, modifier = Modifier.size(13.dp))
                    Text("Add", style = MaterialTheme.typography.labelSmall, color = Gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
        GoldDivider()

        if (addresses.isEmpty()) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("📍", fontSize = 32.sp)
                Text("No saved addresses yet", style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.5f))
                GhostButton("Add Your First Address", onAdd, Modifier.fillMaxWidth())
            }
        } else {
            addresses.forEachIndexed { i, addr ->
                AddressRow(addr = addr, onEdit = { onEdit(addr) }, onDelete = { addr.id?.let(onDelete) }, onSetDefault = { addr.id?.let(onSetDefault) })
                if (i < addresses.size - 1) GoldDivider()
            }
        }
    }
}

@Composable
private fun AddressRow(addr: AddressDto, onEdit: () -> Unit, onDelete: () -> Unit, onSetDefault: () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(if (addr.isDefault) Gold.copy(0.2f) else Surface2)
                .border(1.dp, if (addr.isDefault) Gold.copy(0.5f) else GoldBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.LocationOn, null, tint = if (addr.isDefault) Gold else IvoryDim.copy(0.5f), modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    addr.label ?: "Address",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = Ivory,
                    fontWeight = FontWeight.SemiBold,
                )
                if (addr.isDefault) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Gold.copy(0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    ) { Text("DEFAULT", style = MaterialTheme.typography.labelSmall, color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Text(addr.addressLine1, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${addr.city}, ${addr.state} ${addr.pincode}", style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.45f), fontSize = 10.sp)
        }
        // Actions
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Filled.Edit, null, tint = Gold, modifier = Modifier.size(14.dp))
            }
            if (!addr.isDefault) {
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Filled.Delete, null, tint = Color(0xFFEF5350).copy(0.7f), modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = onSetDefault, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Filled.CheckCircle, null, tint = IvoryDim.copy(0.3f), modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

/* ── Generic menu section ── */
private data class MenuItem(val label: String, val icon: ImageVector, val value: String, val onClick: () -> Unit)

@Composable
private fun ProfileMenuGroup(title: String, items: List<MenuItem>) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Surface1)
            .border(1.dp, GoldBorder, RoundedCornerShape(20.dp)),
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) { EyebrowLabel(title) }
        GoldDivider()
        items.forEachIndexed { i, item ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { item.onClick() }.padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.size(34.dp).clip(CircleShape).background(Gold.copy(0.12f)), Alignment.Center) {
                    Icon(item.icon, null, tint = Gold, modifier = Modifier.size(16.dp))
                }
                Text(item.label, style = MaterialTheme.typography.bodyMedium, color = Ivory, modifier = Modifier.weight(1f))
                if (item.value.isNotEmpty()) Text(item.value, style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.45f), maxLines = 1)
                Icon(Icons.Filled.ChevronRight, null, tint = IvoryDim.copy(0.25f), modifier = Modifier.size(15.dp))
            }
            if (i < items.size - 1) GoldDivider()
        }
    }
}

/* ── Sign out ── */
@Composable
private fun SignOutButton(onLogout: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor   = Surface1,
            shape            = RoundedCornerShape(20.dp),
            title = { Text("Sign Out?", color = Ivory, fontWeight = FontWeight.Bold) },
            text  = { Text("You'll be returned to the login screen.", color = IvoryDim.copy(0.6f), style = MaterialTheme.typography.bodySmall) },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onLogout() }) {
                    Text("Sign Out", color = Color(0xFFEF5350), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel", color = IvoryDim.copy(0.6f))
                }
            },
        )
    }
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .border(1.dp, Maroon.copy(0.4f), RoundedCornerShape(14.dp)).background(Maroon.copy(0.07f))
            .clickable { showConfirm = true }.padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Logout, null, tint = MaroonLight, modifier = Modifier.size(18.dp))
            Text("Sign Out", style = MaterialTheme.typography.labelLarge, color = MaroonLight, fontWeight = FontWeight.SemiBold)
        }
    }
}

/* ── Guest upsell ── */
@Composable
private fun GuestUpsell() {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(colorStops = arrayOf(0f to MaroonDark, 1f to Color(0xFF1A0608))))
            .border(1.dp, GoldBorder, RoundedCornerShape(20.dp)).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("👑", fontSize = 40.sp)
        Text("Create a Royal Account", style = MaterialTheme.typography.titleMedium, color = Ivory, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("Sign in to earn loyalty points, save addresses, track orders, and unlock exclusive royal offers.", style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.65f), textAlign = TextAlign.Center, lineHeight = 18.sp)
    }
}

/* ── Edit profile bottom sheet ── */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(state: ProfileUiState, vm: ProfileViewModel) {
    ModalBottomSheet(
        onDismissRequest = vm::closeEditProfile,
        containerColor   = Surface1,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Edit Profile", style = MaterialTheme.typography.titleLarge, color = Ivory, fontWeight = FontWeight.Bold)
            GoldDivider()

            ProfileField("Full Name", state.editName, Icons.Filled.Person, vm::onEditName, KeyboardType.Text)
            ProfileField("Email Address", state.editEmail, Icons.Filled.Email, vm::onEditEmail, KeyboardType.Email)

            // Gender picker
            Text("Gender", style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.6f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("MALE", "FEMALE", "OTHER").forEach { g ->
                    val selected = state.editGender == g
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Gold.copy(0.2f) else Surface2)
                            .border(1.dp, if (selected) Gold.copy(0.5f) else GoldBorder, RoundedCornerShape(10.dp))
                            .clickable { vm.onEditGender(g) }.padding(horizontal = 16.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(g.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium, color = if (selected) Gold else IvoryDim.copy(0.6f), fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            GoldButton(if (state.isSaving) "Saving…" else "Save Changes", vm::saveProfile, Modifier.fillMaxWidth(), enabled = !state.isSaving)
            GhostButton("Cancel", vm::closeEditProfile, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String, icon: ImageVector, onValueChange: (String) -> Unit, keyboardType: KeyboardType) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.6f))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Surface2)
                .border(1.dp, GoldBorder, RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(icon, null, tint = Gold.copy(0.7f), modifier = Modifier.size(16.dp))
            androidx.compose.foundation.text.BasicTextField(
                value         = value,
                onValueChange = onValueChange,
                modifier      = Modifier.weight(1f),
                textStyle     = MaterialTheme.typography.bodyMedium.copy(color = Ivory),
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty()) Text(label, style = MaterialTheme.typography.bodyMedium, color = IvoryDim.copy(0.3f))
                        inner()
                    }
                },
            )
        }
    }
}

/* ── Address bottom sheet ── */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressSheet(state: ProfileUiState, vm: ProfileViewModel) {
    ModalBottomSheet(
        onDismissRequest = vm::closeAddressSheet,
        containerColor   = Surface1,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (state.editingAddress != null) "Edit Address" else "Add New Address",
                style = MaterialTheme.typography.titleLarge,
                color = Ivory,
                fontWeight = FontWeight.Bold,
            )
            GoldDivider()

            AddressField("Label (Home / Work / Other)", state.addrLabel, Icons.Filled.Label, vm::onAddrLabel)
            AddressField("Address Line 1 *", state.addrLine1, Icons.Filled.Home, vm::onAddrLine1)
            AddressField("Address Line 2 (Apt, Floor, etc.)", state.addrLine2, Icons.Filled.Apartment, vm::onAddrLine2)
            AddressField("Landmark", state.addrLandmark, Icons.Filled.Place, vm::onAddrLandmark)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("City *", style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.6f))
                    AddressFieldBox(state.addrCity, "Bangalore", vm::onAddrCity)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("State *", style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.6f))
                    AddressFieldBox(state.addrState, "Karnataka", vm::onAddrState)
                }
            }

            Column(modifier = Modifier.fillMaxWidth(0.5f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Pincode *", style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.6f))
                AddressFieldBox(state.addrPincode, "560001", vm::onAddrPincode, KeyboardType.Number)
            }

            Spacer(Modifier.height(4.dp))
            GoldButton(if (state.isSaving) "Saving…" else "Save Address", vm::saveAddress, Modifier.fillMaxWidth(), enabled = !state.isSaving)
            GhostButton("Cancel", vm::closeAddressSheet, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun AddressField(label: String, value: String, icon: ImageVector, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.6f))
        AddressFieldBox(value, label, onChange)
    }
}

@Composable
private fun AddressFieldBox(value: String, placeholder: String, onChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Surface2)
            .border(1.dp, GoldBorder, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value         = value,
            onValueChange = onChange,
            modifier      = Modifier.fillMaxWidth(),
            textStyle     = MaterialTheme.typography.bodyMedium.copy(color = Ivory),
            singleLine    = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = IvoryDim.copy(0.3f))
                    inner()
                }
            },
        )
    }
}

/* ── Shimmer ── */
@Composable
private fun ProfileShimmer(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Spacer(Modifier.height(4.dp))
        ShimmerBox(Modifier.fillMaxWidth().height(110.dp), RoundedCornerShape(24.dp))
        ShimmerBox(Modifier.fillMaxWidth().height(120.dp), RoundedCornerShape(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) { ShimmerBox(Modifier.weight(1f).height(80.dp), RoundedCornerShape(16.dp)) }
        }
        ShimmerBox(Modifier.fillMaxWidth().height(160.dp), RoundedCornerShape(20.dp))
        ShimmerBox(Modifier.fillMaxWidth().height(200.dp), RoundedCornerShape(20.dp))
    }
}
