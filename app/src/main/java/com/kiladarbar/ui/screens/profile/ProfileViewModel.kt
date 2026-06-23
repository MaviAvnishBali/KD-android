package com.kiladarbar.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiladarbar.data.local.SessionManager
import com.kiladarbar.data.remote.dto.*
import com.kiladarbar.data.repository.NetworkResult
import com.kiladarbar.data.repository.OrderRepository
import com.kiladarbar.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading:          Boolean         = true,
    val isGuest:            Boolean         = false,
    val profile:            UserProfileDto? = null,
    val addresses:          List<AddressDto> = emptyList(),
    val totalOrders:        Int             = 0,
    val totalSpent:         Double          = 0.0,
    val loyaltyPoints:      Int             = 0,
    val loyaltyTier:        String          = "Silver",

    // Edit profile sheet
    val showEditProfile:    Boolean         = false,
    val editName:           String          = "",
    val editEmail:          String          = "",
    val editGender:         String          = "",
    val editDob:            String          = "",

    // Address add/edit sheet
    val showAddressSheet:   Boolean         = false,
    val editingAddress:     AddressDto?     = null,
    val addrLabel:          String          = "",
    val addrLine1:          String          = "",
    val addrLine2:          String          = "",
    val addrLandmark:       String          = "",
    val addrCity:           String          = "",
    val addrState:          String          = "",
    val addrPincode:        String          = "",

    val isSaving:           Boolean         = false,
    val successMessage:     String?         = null,
    val error:              String?         = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository:  UserRepository,
    private val orderRepository: OrderRepository,
    private val sessionManager:  SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val isGuest = sessionManager.isGuest()

            val profileJob   = async { userRepository.getProfile() }
            val addressesJob = async { userRepository.getAddresses() }
            val ordersJob    = async { orderRepository.getOrders(0) }

            val profile   = profileJob.await()
            val addresses = addressesJob.await()
            val orders    = ordersJob.await()

            val profileData  = (profile   as? NetworkResult.Success)?.data
            val addressData  = (addresses as? NetworkResult.Success)?.data ?: emptyList()
            val ordersPage   = (orders    as? NetworkResult.Success)?.data

            _state.update {
                it.copy(
                    isLoading     = false,
                    isGuest       = isGuest,
                    profile       = profileData,
                    addresses     = addressData,
                    totalOrders   = ordersPage?.totalElements?.toInt() ?: 0,
                    totalSpent    = ordersPage?.content?.sumOf { o -> o.totalAmount } ?: 0.0,
                    editName      = profileData?.name ?: "",
                    editEmail     = profileData?.email ?: "",
                    editGender    = profileData?.gender ?: "",
                    editDob       = profileData?.dateOfBirth ?: "",
                    error         = if (profile is NetworkResult.Error) profile.message else null,
                )
            }
        }
    }

    /* ── Edit profile ── */

    fun openEditProfile() = _state.update { s ->
        s.copy(
            showEditProfile = true,
            editName        = s.profile?.name ?: "",
            editEmail       = s.profile?.email ?: "",
            editGender      = s.profile?.gender ?: "",
            editDob         = s.profile?.dateOfBirth ?: "",
        )
    }
    fun closeEditProfile()            = _state.update { it.copy(showEditProfile = false) }
    fun onEditName(v: String)         = _state.update { it.copy(editName = v) }
    fun onEditEmail(v: String)        = _state.update { it.copy(editEmail = v) }
    fun onEditGender(v: String)       = _state.update { it.copy(editGender = v) }
    fun onEditDob(v: String)          = _state.update { it.copy(editDob = v) }

    fun saveProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val s = _state.value
            val req = UpdateProfileRequest(
                name        = s.editName.ifBlank { null },
                email       = s.editEmail.ifBlank { null },
                gender      = s.editGender.ifBlank { null },
                dateOfBirth = s.editDob.ifBlank { null },
            )
            when (val r = userRepository.updateProfile(req)) {
                is NetworkResult.Success -> _state.update {
                    it.copy(isSaving = false, showEditProfile = false, profile = r.data, successMessage = "Profile updated!")
                }
                is NetworkResult.Error -> _state.update {
                    it.copy(isSaving = false, error = r.message)
                }
                else -> _state.update { it.copy(isSaving = false) }
            }
        }
    }

    /* ── Address CRUD ── */

    fun openAddAddress() = _state.update {
        it.copy(showAddressSheet = true, editingAddress = null, addrLabel = "", addrLine1 = "", addrLine2 = "", addrLandmark = "", addrCity = "", addrState = "", addrPincode = "")
    }

    fun openEditAddress(addr: AddressDto) = _state.update {
        it.copy(showAddressSheet = true, editingAddress = addr, addrLabel = addr.label ?: "", addrLine1 = addr.addressLine1, addrLine2 = addr.addressLine2 ?: "", addrLandmark = addr.landmark ?: "", addrCity = addr.city, addrState = addr.state, addrPincode = addr.pincode)
    }

    fun closeAddressSheet() = _state.update { it.copy(showAddressSheet = false, editingAddress = null) }

    fun onAddrLabel(v: String)    = _state.update { it.copy(addrLabel = v) }
    fun onAddrLine1(v: String)    = _state.update { it.copy(addrLine1 = v) }
    fun onAddrLine2(v: String)    = _state.update { it.copy(addrLine2 = v) }
    fun onAddrLandmark(v: String) = _state.update { it.copy(addrLandmark = v) }
    fun onAddrCity(v: String)     = _state.update { it.copy(addrCity = v) }
    fun onAddrState(v: String)    = _state.update { it.copy(addrState = v) }
    fun onAddrPincode(v: String)  = _state.update { it.copy(addrPincode = v) }

    fun saveAddress() {
        val s = _state.value
        if (s.addrLine1.isBlank() || s.addrCity.isBlank() || s.addrState.isBlank() || s.addrPincode.length != 6) {
            _state.update { it.copy(error = "Please fill in all required fields (address, city, state, 6-digit pincode)") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val req = CreateAddressRequest(
                label        = s.addrLabel.ifBlank { null },
                addressLine1 = s.addrLine1,
                addressLine2 = s.addrLine2.ifBlank { null },
                landmark     = s.addrLandmark.ifBlank { null },
                city         = s.addrCity,
                state        = s.addrState,
                pincode      = s.addrPincode,
            )
            val result = if (s.editingAddress?.id != null)
                userRepository.updateAddress(s.editingAddress.id, req)
            else
                userRepository.addAddress(req)

            when (result) {
                is NetworkResult.Success -> {
                    val updated = userRepository.getAddresses()
                    _state.update { it.copy(
                        isSaving        = false,
                        showAddressSheet = false,
                        addresses       = (updated as? NetworkResult.Success)?.data ?: it.addresses,
                        successMessage  = if (s.editingAddress != null) "Address updated!" else "Address added!",
                    )}
                }
                is NetworkResult.Error -> _state.update { it.copy(isSaving = false, error = result.message) }
                else -> _state.update { it.copy(isSaving = false) }
            }
        }
    }

    fun deleteAddress(id: String) {
        viewModelScope.launch {
            userRepository.deleteAddress(id)
            val updated = userRepository.getAddresses()
            _state.update { it.copy(addresses = (updated as? NetworkResult.Success)?.data ?: it.addresses) }
        }
    }

    fun setDefaultAddress(id: String) {
        viewModelScope.launch {
            userRepository.setDefaultAddress(id)
            _state.update { s ->
                s.copy(addresses = s.addresses.map { it.copy(isDefault = it.id == id) })
            }
        }
    }

    /* ── Session ── */

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearSession()
            onDone()
        }
    }

    fun clearMessage() = _state.update { it.copy(successMessage = null, error = null) }
}
