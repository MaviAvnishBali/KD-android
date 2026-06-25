package com.kiladarbar.ui.screens.partyhall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiladarbar.data.remote.dto.CreatePartyHallBookingRequest
import com.kiladarbar.data.remote.dto.PartyHallBookingDto
import com.kiladarbar.data.repository.NetworkResult
import com.kiladarbar.data.repository.PartyHallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/* ── Static catalogue ── */

data class PartyPackage(
    val type:     String,
    val name:     String,
    val price:    Int,
    val emoji:    String,
    val tagline:  String,
    val perks:    List<String>,
    val featured: Boolean = false,
)

val PARTY_PACKAGES = listOf(
    PartyPackage(
        type = "BASIC", name = "Basic", price = 15_000, emoji = "🎈",
        tagline = "Intimate celebrations done right",
        perks = listOf(
            "Up to 50 guests",
            "3-hour hall booking",
            "Basic décor & seating",
            "Welcome drinks",
        ),
    ),
    PartyPackage(
        type = "ROYAL", name = "Royal", price = 28_000, emoji = "👑",
        tagline = "Our most-loved package", featured = true,
        perks = listOf(
            "Up to 80 guests",
            "5-hour hall booking",
            "Themed royal décor",
            "Welcome drinks + starters",
            "Dedicated event host",
        ),
    ),
    PartyPackage(
        type = "GRAND", name = "Grand", price = 48_000, emoji = "🏛️",
        tagline = "The full Mughal darbar experience",
        perks = listOf(
            "Up to 100 guests",
            "Full-day hall booking",
            "Premium décor & stage",
            "Live counters + multi-cuisine buffet",
            "Photographer + event manager",
        ),
    ),
)

val PARTY_OCCASIONS = listOf(
    "Birthday"        to "🎂",
    "Ring Ceremony"   to "💍",
    "Anniversary"     to "💐",
    "Baby Shower"     to "🍼",
    "Kitty Party"     to "🎉",
    "Get Together"    to "🥂",
)

/* ── UI state ── */

data class PartyHallForm(
    val customerName:    String = "",
    val customerPhone:   String = "",
    val customerEmail:   String = "",
    val eventType:       String = "",
    val guestCount:      String = "",
    val preferredDate:   String = "",
    val preferredTime:   String = "",
    val packageType:     String = "ROYAL",
    val specialRequests: String = "",
)

data class PartyHallUiState(
    val form:        PartyHallForm = PartyHallForm(),
    val submitting:  Boolean = false,
    val submitError: String? = null,
    val justBooked:  PartyHallBookingDto? = null,
    val bookingsLoading: Boolean = true,
    val bookings:    List<PartyHallBookingDto> = emptyList(),
    val cancellingId: String? = null,
)

@HiltViewModel
class PartyHallViewModel @Inject constructor(
    private val repository: PartyHallRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartyHallUiState())
    val uiState = _uiState.asStateFlow()

    init { loadBookings() }

    /* ── Form mutations ── */
    fun updateForm(transform: (PartyHallForm) -> PartyHallForm) =
        _uiState.update { it.copy(form = transform(it.form), submitError = null) }

    fun selectPackage(type: String) = updateForm { it.copy(packageType = type) }
    fun selectOccasion(name: String) = updateForm { it.copy(eventType = name) }

    /* ── Validation ── */
    private fun validate(f: PartyHallForm): String? = when {
        f.customerName.isBlank()              -> "Please enter your name"
        f.customerPhone.length < 10           -> "Enter a valid 10-digit phone number"
        f.eventType.isBlank()                 -> "Please choose an occasion"
        (f.guestCount.toIntOrNull() ?: 0) < 1 -> "Enter the number of guests"
        (f.guestCount.toIntOrNull() ?: 0) > 100 -> "We host gatherings up to 100 guests"
        f.preferredDate.isBlank()             -> "Please pick a date"
        f.preferredTime.isBlank()             -> "Please pick a time"
        else -> null
    }

    fun submit() {
        val f = _uiState.value.form
        validate(f)?.let { error ->
            _uiState.update { it.copy(submitError = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(submitting = true, submitError = null) }
            val request = CreatePartyHallBookingRequest(
                customerName    = f.customerName.trim(),
                customerPhone   = f.customerPhone.trim(),
                customerEmail   = f.customerEmail.trim().ifBlank { null },
                eventType       = f.eventType,
                guestCount      = f.guestCount.toInt(),
                preferredDate   = f.preferredDate,
                preferredTime   = f.preferredTime,
                packageType     = f.packageType,
                specialRequests = f.specialRequests.trim().ifBlank { null },
            )
            when (val r = repository.book(request)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            submitting = false,
                            justBooked = r.data,
                            form       = PartyHallForm(),
                        )
                    }
                    loadBookings()
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(submitting = false, submitError = r.message)
                }
                else -> {}
            }
        }
    }

    fun dismissConfirmation() = _uiState.update { it.copy(justBooked = null) }

    /* ── My bookings ── */
    fun loadBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(bookingsLoading = true) }
            when (val r = repository.myBookings()) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(bookingsLoading = false, bookings = r.data)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(bookingsLoading = false)
                }
                else -> {}
            }
        }
    }

    fun cancelBooking(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cancellingId = id) }
            when (repository.cancel(id)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(cancellingId = null) }
                    loadBookings()
                }
                else -> _uiState.update { it.copy(cancellingId = null) }
            }
        }
    }
}
