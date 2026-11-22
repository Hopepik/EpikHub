package com.epikhub.epikhub.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epikhub.epikhub.data.model.Company
import com.epikhub.epikhub.data.model.RequestStatus
import com.epikhub.epikhub.data.model.Service
import com.epikhub.epikhub.data.model.ServiceRequest
import com.epikhub.epikhub.data.repository.CompanyRepository
import com.epikhub.epikhub.data.repository.RequestRepository
import com.epikhub.epikhub.data.repository.ServiceRepository
import com.epikhub.epikhub.domain.model.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Admin Dashboard ViewModel
data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val company: Company? = null,
    val totalRequests: Int = 0,
    val pendingRequests: Int = 0,
    val inProgressRequests: Int = 0,
    val activeServicesCount: Int = 0,
    val errorMessage: String? = null
)

class AdminDashboardViewModel(
    private val companyRepository: CompanyRepository = CompanyRepository(),
    private val serviceRepository: ServiceRepository = ServiceRepository(),
    private val requestRepository: RequestRepository = RequestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState(isLoading = true))
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    fun loadDashboardData(companyId: String) {
        viewModelScope.launch {
            _uiState.value = AdminDashboardUiState(isLoading = true)

            val companyRes = companyRepository.getCompanyById(companyId)
            val company = if (companyRes is Resource.Success) companyRes.data else null

            combine(
                serviceRepository.getAllServicesForCompanyFlow(companyId),
                requestRepository.getCompanyRequestsFlow(companyId)
            ) { serviceRes, requestRes ->
                val services = if (serviceRes is Resource.Success) serviceRes.data else emptyList()
                val requests = if (requestRes is Resource.Success) requestRes.data else emptyList()

                AdminDashboardUiState(
                    isLoading = false,
                    company = company,
                    totalRequests = requests.size,
                    pendingRequests = requests.count { it.requestStatus == RequestStatus.PENDING },
                    inProgressRequests = requests.count { it.requestStatus == RequestStatus.IN_PROGRESS },
                    activeServicesCount = services.count { it.isActive }
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}

// Admin Services ViewModel
data class AdminServicesUiState(
    val isLoading: Boolean = false,
    val services: List<Service> = emptyList(),
    val errorMessage: String? = null
)

class AdminServicesViewModel(
    private val serviceRepository: ServiceRepository = ServiceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminServicesUiState(isLoading = true))
    val uiState: StateFlow<AdminServicesUiState> = _uiState.asStateFlow()

    fun loadServices(companyId: String) {
        viewModelScope.launch {
            serviceRepository.getAllServicesForCompanyFlow(companyId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = AdminServicesUiState(isLoading = true)
                    is Resource.Error -> _uiState.value = AdminServicesUiState(isLoading = false, errorMessage = resource.message)
                    is Resource.Success -> _uiState.value = AdminServicesUiState(isLoading = false, services = resource.data)
                }
            }
        }
    }

    fun toggleServiceActive(serviceId: String, currentActive: Boolean) {
        viewModelScope.launch {
            val result = serviceRepository.toggleServiceActive(serviceId, !currentActive)
            if (result is Resource.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }
}

// Add / Edit Service ViewModel
data class AddEditServiceUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val description: String = "",
    val category: String = "General",
    val price: String = "",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class AddEditServiceViewModel(
    private val serviceRepository: ServiceRepository = ServiceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditServiceUiState())
    val uiState: StateFlow<AddEditServiceUiState> = _uiState.asStateFlow()

    private var editingServiceId: String? = null

    fun loadExistingService(serviceId: String?) {
        if (serviceId.isNullOrEmpty()) return
        editingServiceId = serviceId

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = serviceRepository.getServiceById(serviceId)) {
                is Resource.Success -> {
                    val s = res.data
                    _uiState.value = AddEditServiceUiState(
                        isLoading = false,
                        name = s.name,
                        description = s.description,
                        category = s.category,
                        price = s.price.toString(),
                        imageUrl = s.imageUrl ?: "",
                        isActive = s.isActive
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun onDescChange(v: String) { _uiState.value = _uiState.value.copy(description = v) }
    fun onCategoryChange(v: String) { _uiState.value = _uiState.value.copy(category = v) }
    fun onPriceChange(v: String) { _uiState.value = _uiState.value.copy(price = v) }
    fun onImageUrlChange(v: String) { _uiState.value = _uiState.value.copy(imageUrl = v) }
    fun onActiveChange(v: Boolean) { _uiState.value = _uiState.value.copy(isActive = v) }

    fun saveService(companyId: String) {
        val state = _uiState.value
        val priceDouble = state.price.toDoubleOrNull()

        if (state.name.isBlank() || state.description.isBlank() || priceDouble == null) {
            _uiState.value = state.copy(errorMessage = "Please enter valid service details and numeric price")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val service = Service(
                id = editingServiceId ?: "",
                companyId = companyId,
                name = state.name.trim(),
                description = state.description.trim(),
                category = state.category.trim().ifEmpty { "General" },
                price = priceDouble,
                imageUrl = state.imageUrl.trim().ifEmpty { null },
                isActive = state.isActive,
                companyActive = true
            )

            val res = if (editingServiceId.isNullOrEmpty()) {
                serviceRepository.createService(service)
            } else {
                serviceRepository.updateService(service)
            }

            when (res) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.message)
                is Resource.Loading -> {}
            }
        }
    }
}

// Admin Requests ViewModel
data class AdminRequestsUiState(
    val isLoading: Boolean = false,
    val requests: List<ServiceRequest> = emptyList(),
    val errorMessage: String? = null
)

class AdminRequestsViewModel(
    private val requestRepository: RequestRepository = RequestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminRequestsUiState(isLoading = true))
    val uiState: StateFlow<AdminRequestsUiState> = _uiState.asStateFlow()

    fun loadCompanyRequests(companyId: String) {
        viewModelScope.launch {
            requestRepository.getCompanyRequestsFlow(companyId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = AdminRequestsUiState(isLoading = true)
                    is Resource.Error -> _uiState.value = AdminRequestsUiState(isLoading = false, errorMessage = resource.message)
                    is Resource.Success -> _uiState.value = AdminRequestsUiState(isLoading = false, requests = resource.data)
                }
            }
        }
    }

    fun updateStatus(requestId: String, newStatus: RequestStatus) {
        viewModelScope.launch {
            val result = requestRepository.updateRequestStatus(requestId, newStatus)
            if (result is Resource.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }
}

// Admin Request Detail ViewModel
data class AdminRequestDetailUiState(
    val isLoading: Boolean = false,
    val request: ServiceRequest? = null,
    val errorMessage: String? = null
)

class AdminRequestDetailViewModel(
    private val requestRepository: RequestRepository = RequestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminRequestDetailUiState(isLoading = true))
    val uiState: StateFlow<AdminRequestDetailUiState> = _uiState.asStateFlow()

    fun loadRequestDetails(requestId: String) {
        if (requestId.isBlank()) {
            _uiState.value = AdminRequestDetailUiState(isLoading = false, errorMessage = "Invalid request ID")
            return
        }

        viewModelScope.launch {
            _uiState.value = AdminRequestDetailUiState(isLoading = true)
            when (val res = requestRepository.getRequestById(requestId)) {
                is Resource.Success -> {
                    _uiState.value = AdminRequestDetailUiState(isLoading = false, request = res.data)
                }
                is Resource.Error -> {
                    _uiState.value = AdminRequestDetailUiState(isLoading = false, errorMessage = res.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateStatus(requestId: String, newStatus: RequestStatus) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = requestRepository.updateRequestStatus(requestId, newStatus)
            when (result) {
                is Resource.Success -> {
                    loadRequestDetails(requestId)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }
}

// Company Profile Edit ViewModel
data class CompanyProfileEditUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val description: String = "",
    val phone: String = "",
    val email: String = "",
    val location: String = "",
    val logoUrl: String = "",
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class CompanyProfileEditViewModel(
    private val companyRepository: CompanyRepository = CompanyRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyProfileEditUiState())
    val uiState: StateFlow<CompanyProfileEditUiState> = _uiState.asStateFlow()

    fun loadCompany(companyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = companyRepository.getCompanyById(companyId)) {
                is Resource.Success -> {
                    val c = res.data
                    _uiState.value = CompanyProfileEditUiState(
                        isLoading = false,
                        name = c.name,
                        description = c.description,
                        phone = c.phone,
                        email = c.email,
                        location = c.location,
                        logoUrl = c.logoUrl ?: ""
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun onDescChange(v: String) { _uiState.value = _uiState.value.copy(description = v) }
    fun onPhoneChange(v: String) { _uiState.value = _uiState.value.copy(phone = v) }
    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v) }
    fun onLocationChange(v: String) { _uiState.value = _uiState.value.copy(location = v) }
    fun onLogoUrlChange(v: String) { _uiState.value = _uiState.value.copy(logoUrl = v) }

    fun saveCompany(companyId: String) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val res = companyRepository.updateCompanyProfile(
                companyId = companyId,
                name = state.name,
                description = state.description,
                phone = state.phone,
                email = state.email,
                location = state.location,
                logoUrl = state.logoUrl.ifEmpty { null }
            )
            when (res) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.message)
                is Resource.Loading -> {}
            }
        }
    }
}
