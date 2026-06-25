package com.kiladarbar.ui.screens.partyhall

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiladarbar.data.remote.dto.PartyHallBookingDto
import com.kiladarbar.ui.components.*
import com.kiladarbar.ui.theme.*

private enum class PartyTab { Book, Bookings }

@Composable
fun PartyHallScreen(
    onBack:    () -> Unit,
    viewModel: PartyHallViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(PartyTab.Book) }

    Scaffold(
        containerColor = Obsidian,
        topBar = {
            Column(Modifier.background(Obsidian)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ivory)
                    }
                    Text(
                        "PARTY HALL",
                        style         = MaterialTheme.typography.titleSmall,
                        color         = Ivory,
                        letterSpacing = 2.sp,
                        fontWeight    = FontWeight.Bold,
                    )
                    Spacer(Modifier.size(48.dp))
                }
                PartyTabs(tab = tab, onSelect = { tab = it })
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                PartyTab.Book     -> BookContent(uiState, viewModel, onSeeBookings = { tab = PartyTab.Bookings })
                PartyTab.Bookings -> BookingsContent(uiState, viewModel, onBookNew = { tab = PartyTab.Book })
            }
        }
    }

    // Success dialog
    uiState.justBooked?.let { booking ->
        BookingConfirmedDialog(
            booking        = booking,
            onViewBookings = { viewModel.dismissConfirmation(); tab = PartyTab.Bookings },
            onDismiss      = { viewModel.dismissConfirmation() },
        )
    }
}

/* ── Tabs ── */
@Composable
private fun PartyTabs(tab: PartyTab, onSelect: (PartyTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Surface1)
            .border(1.dp, GoldBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PartyTabButton("Book an Event", tab == PartyTab.Book, Modifier.weight(1f)) { onSelect(PartyTab.Book) }
        PartyTabButton("My Bookings",   tab == PartyTab.Bookings, Modifier.weight(1f)) { onSelect(PartyTab.Bookings) }
    }
}

@Composable
private fun PartyTabButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) LuxuryGradients.goldHorizontal else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.labelMedium,
            color      = if (selected) Obsidian else IvoryDim,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

/* ── Book content ── */
@Composable
private fun BookContent(
    uiState:       PartyHallUiState,
    viewModel:     PartyHallViewModel,
    onSeeBookings: () -> Unit,
) {
    val form = uiState.form
    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(Obsidian),
        contentPadding      = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item { PartyHero() }

        // Occasions
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EyebrowLabel("What's the occasion?")
                FlowOccasions(selected = form.eventType, onSelect = viewModel::selectOccasion)
            }
        }

        // Packages
        item { EyebrowLabel("Choose a package", Modifier.padding(horizontal = 16.dp)) }
        item {
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(PARTY_PACKAGES) { pkg ->
                    PackageCard(
                        pkg      = pkg,
                        selected = form.packageType == pkg.type,
                        onSelect = { viewModel.selectPackage(pkg.type) },
                    )
                }
            }
        }

        // Booking form
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                EyebrowLabel("Your details")

                PartyField("Full Name", form.customerName, Icons.Filled.Person) {
                    viewModel.updateForm { f -> f.copy(customerName = it) }
                }
                PartyField("Phone Number", form.customerPhone, Icons.Filled.Phone, keyboard = KeyboardType.Phone) {
                    viewModel.updateForm { f -> f.copy(customerPhone = it.filter(Char::isDigit).take(10)) }
                }
                PartyField("Email (optional)", form.customerEmail, Icons.Filled.Email, keyboard = KeyboardType.Email) {
                    viewModel.updateForm { f -> f.copy(customerEmail = it) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PartyField("Guests", form.guestCount, Icons.Filled.Groups, keyboard = KeyboardType.Number, modifier = Modifier.weight(1f)) {
                        viewModel.updateForm { f -> f.copy(guestCount = it.filter(Char::isDigit).take(3)) }
                    }
                    PartyField("Date", form.preferredDate, Icons.Filled.CalendarMonth, placeholder = "YYYY-MM-DD", modifier = Modifier.weight(1.4f)) {
                        viewModel.updateForm { f -> f.copy(preferredDate = it) }
                    }
                }
                PartyField("Preferred Time", form.preferredTime, Icons.Filled.Schedule, placeholder = "e.g. 7:00 PM") {
                    viewModel.updateForm { f -> f.copy(preferredTime = it) }
                }
                PartyField("Special Requests (optional)", form.specialRequests, Icons.Filled.Notes, singleLine = false) {
                    viewModel.updateForm { f -> f.copy(specialRequests = it) }
                }

                AnimatedVisibility(uiState.submitError != null) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEF5350).copy(0.12f))
                            .padding(12.dp),
                    ) {
                        Icon(Icons.Filled.ErrorOutline, null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                        Text(uiState.submitError ?: "", style = MaterialTheme.typography.bodySmall, color = Color(0xFFEF5350))
                    }
                }

                Spacer(Modifier.height(4.dp))

                val selectedPkg = PARTY_PACKAGES.first { it.type == form.packageType }
                if (uiState.submitting) {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface2).padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = Gold, strokeWidth = 2.dp, modifier = Modifier.size(22.dp)) }
                } else {
                    GoldButton(
                        text     = "Request Booking · ₹${"%,d".format(selectedPkg.price)}",
                        onClick  = viewModel::submit,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(
                    "Our events team will call to confirm availability & finalise the menu.",
                    style     = MaterialTheme.typography.labelSmall,
                    color     = IvoryDim.copy(0.45f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/* ── Hero ── */
@Composable
private fun PartyHero() {
    val infinite = rememberInfiniteTransition(label = "heroGlow")
    val glow by infinite.animateFloat(
        0.08f, 0.18f,
        infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(colorStops = arrayOf(0f to MaroonDark, 0.55f to Color(0xFF3D0F1A), 1f to Color(0xFF1A0608))))
            .border(1.dp, GoldBorder, RoundedCornerShape(24.dp)),
    ) {
        Box(
            Modifier.size(220.dp).align(Alignment.CenterEnd).offset(x = 30.dp)
                .background(Brush.radialGradient(listOf(Gold.copy(glow), Color.Transparent)))
        )
        Text("🎉", fontSize = 96.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).alpha(0.28f))
        Column(
            modifier            = Modifier.align(Alignment.CenterStart).padding(start = 22.dp, end = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RoyalBadge("Celebrate in style ✦")
            Text(
                "Host Your Party\nat Kila Darbar",
                style      = MaterialTheme.typography.headlineSmall,
                color      = Ivory,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp,
            )
            Text(
                "Birthdays · Ring ceremonies · Get-togethers up to 100 guests",
                style    = MaterialTheme.typography.bodySmall,
                color    = IvoryDim.copy(0.75f),
                maxLines = 2,
            )
        }
    }
}

/* ── Occasions ── */
@Composable
private fun FlowOccasions(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PARTY_OCCASIONS.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (name, emoji) ->
                    val isSel = selected == name
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSel) Gold.copy(0.15f) else Surface1)
                            .border(1.dp, if (isSel) Gold.copy(0.5f) else GoldBorder, RoundedCornerShape(14.dp))
                            .clickable { onSelect(name) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(emoji, fontSize = 18.sp)
                        Text(
                            name,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = if (isSel) Gold else IvoryDim,
                            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                        )
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/* ── Package card ── */
@Composable
private fun PackageCard(pkg: PartyPackage, selected: Boolean, onSelect: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1f else 0.97f, spring(Spring.DampingRatioMediumBouncy), label = "pkgScale")
    Column(
        modifier = Modifier
            .width(230.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Brush.linearGradient(listOf(Color(0xFF2A1C00), Surface1)) else Brush.linearGradient(listOf(Surface1, Surface1)))
            .border(if (selected) 1.5.dp else 1.dp, if (selected) Gold.copy(0.6f) else GoldBorder, RoundedCornerShape(20.dp))
            .clickable { onSelect() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
            Text(pkg.emoji, fontSize = 34.sp)
            if (pkg.featured) RoyalBadge("Popular")
            else if (selected) Icon(Icons.Filled.CheckCircle, null, tint = Gold, modifier = Modifier.size(22.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(pkg.name, style = MaterialTheme.typography.titleMedium, color = Ivory, fontWeight = FontWeight.Bold)
            Text(pkg.tagline, style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("₹${"%,d".format(pkg.price)}", style = MaterialTheme.typography.titleLarge, color = Gold, fontWeight = FontWeight.Bold)
            Text("onwards", style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.5f), modifier = Modifier.padding(bottom = 3.dp))
        }
        GoldDivider()
        pkg.perks.forEach { perk ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Check, null, tint = Gold, modifier = Modifier.size(14.dp))
                Text(perk, style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.85f))
            }
        }
    }
}

/* ── Form field ── */
@Composable
private fun PartyField(
    label:       String,
    value:       String,
    icon:        androidx.compose.ui.graphics.vector.ImageVector,
    modifier:    Modifier = Modifier,
    placeholder: String = "",
    keyboard:    KeyboardType = KeyboardType.Text,
    singleLine:  Boolean = true,
    onChange:    (String) -> Unit,
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        modifier      = modifier.fillMaxWidth(),
        label         = { Text(label) },
        placeholder   = { if (placeholder.isNotEmpty()) Text(placeholder, color = IvoryDim.copy(0.35f)) },
        leadingIcon   = { Icon(icon, null, tint = Gold.copy(0.7f), modifier = Modifier.size(18.dp)) },
        singleLine    = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape         = RoundedCornerShape(12.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Gold.copy(0.6f),
            unfocusedBorderColor = GoldBorder,
            focusedTextColor     = Ivory,
            unfocusedTextColor   = Ivory,
            cursorColor          = Gold,
            focusedLabelColor    = Gold,
            unfocusedLabelColor  = IvoryDim.copy(0.6f),
            focusedContainerColor   = Surface1,
            unfocusedContainerColor = Surface1,
        ),
    )
}

/* ── My bookings ── */
@Composable
private fun BookingsContent(
    uiState:   PartyHallUiState,
    viewModel: PartyHallViewModel,
    onBookNew: () -> Unit,
) {
    when {
        uiState.bookingsLoading -> Column(
            Modifier.fillMaxSize().background(Obsidian).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) { repeat(3) { ShimmerBox(Modifier.fillMaxWidth().height(150.dp), RoundedCornerShape(20.dp)) } }

        uiState.bookings.isEmpty() -> Column(
            Modifier.fillMaxSize().background(Obsidian).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🎈", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text("No bookings yet", style = MaterialTheme.typography.headlineSmall, color = Ivory, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Plan your next celebration with us — birthdays, ring ceremonies and more.",
                style = MaterialTheme.typography.bodyMedium, color = IvoryDim.copy(0.55f), textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            GoldButton("Book an Event", onBookNew, Modifier.fillMaxWidth())
        }

        else -> LazyColumn(
            modifier            = Modifier.fillMaxSize().background(Obsidian),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiState.bookings, key = { it.id }) { booking ->
                BookingCard(
                    booking      = booking,
                    cancelling   = uiState.cancellingId == booking.id,
                    onCancel     = { viewModel.cancelBooking(booking.id) },
                )
            }
            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}

@Composable
private fun BookingCard(booking: PartyHallBookingDto, cancelling: Boolean, onCancel: () -> Unit) {
    val statusColor = when (booking.status.uppercase()) {
        "CONFIRMED" -> Color(0xFF4CAF50)
        "CANCELLED" -> Color(0xFFEF5350)
        else        -> Color(0xFFFFA726)
    }
    val pkg = PARTY_PACKAGES.firstOrNull { it.type == booking.packageType }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface1)
            .border(1.dp, GoldBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                        .background(Brush.radialGradient(listOf(Maroon.copy(0.5f), Surface2))),
                    contentAlignment = Alignment.Center,
                ) { Text(pkg?.emoji ?: "🎉", fontSize = 24.sp) }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(booking.eventType, style = MaterialTheme.typography.titleSmall, color = Ivory, fontWeight = FontWeight.SemiBold)
                    Text("${pkg?.name ?: booking.packageType} · ${booking.guestCount} guests", style = MaterialTheme.typography.labelSmall, color = Gold)
                }
            }
            Box(
                Modifier.clip(RoundedCornerShape(6.dp)).background(statusColor.copy(0.12f))
                    .border(0.5.dp, statusColor.copy(0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(booking.status, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
            }
        }

        GoldDivider()

        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            DetailColumn("Date", booking.preferredDate)
            DetailColumn("Time", booking.preferredTime)
            booking.totalAmount?.let { DetailColumn("Amount", "₹${"%,.0f".format(it)}") }
        }
        booking.specialRequests?.takeIf { it.isNotBlank() }?.let {
            Text("\"$it\"", style = MaterialTheme.typography.bodySmall, color = IvoryDim.copy(0.6f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }

        if (booking.status.uppercase() != "CANCELLED") {
            if (cancelling) {
                Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                }
            } else {
                GhostButton("Cancel Booking", onCancel, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun DetailColumn(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = IvoryDim.copy(0.4f), fontSize = 9.sp, letterSpacing = 1.sp)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Ivory, fontWeight = FontWeight.SemiBold)
    }
}

/* ── Confirmation dialog ── */
@Composable
private fun BookingConfirmedDialog(
    booking:        PartyHallBookingDto,
    onViewBookings: () -> Unit,
    onDismiss:      () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Surface1)
                .border(1.dp, Gold.copy(0.4f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier.size(72.dp).clip(CircleShape).background(Gold.copy(0.15f)),
                contentAlignment = Alignment.Center,
            ) { Text("🎉", fontSize = 38.sp) }
            Text("Booking Requested!", style = MaterialTheme.typography.titleLarge, color = Ivory, fontWeight = FontWeight.Bold)
            Text(
                "Your ${booking.eventType} for ${booking.guestCount} guests is in. Our events team will call ${booking.customerPhone} to confirm.",
                style = MaterialTheme.typography.bodyMedium, color = IvoryDim.copy(0.7f), textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            GoldButton("View My Bookings", onViewBookings, Modifier.fillMaxWidth())
            GhostButton("Done", onDismiss, Modifier.fillMaxWidth())
        }
    }
}
