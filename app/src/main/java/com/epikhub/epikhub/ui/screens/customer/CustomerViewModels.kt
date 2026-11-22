package com.epikhub.epikhub.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epikhub.epikhub.data.model.Company
import com.epikhub.epikhub.data.model.RequestStatus
import com.epikhub.epikhub.data.model.Service
import com.epikhub.epikhub.data.model.ServiceRequest
import com.epikhub.epikhub.data.repository.CompanyRepository
import com.epikhub.epikhub.data.repository.CustomerRequestItemUiState
import com.epikhub.epikhub.data.repository.RequestRepository
import com.epikhub.epikhub.data.repository.ServiceRepository
import com.epikhub.epikhub.domain.model.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

// Home ViewModel
data class HomeUiState(
    val isLoading: Boolean = false,
    val companies: List<Company> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class HomeViewModel(
    private val companyRepository: CompanyRepository = CompanyRepository()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = combine(
        companyRepository.getActiveCompaniesFlow(),
        _searchQuery
    ) { resource, query ->
        when (resource) {
            is Resource.Loading -> HomeUiState(isLoading = true, searchQuery = query)
            is Resource.Error -> HomeUiState(isLoading = false, errorMessage = resource.message, searchQuery = query)
            is Resource.Success -> {
                val filtered = if (query.isBlank()) {
                    resource.data
                } else {
                    resource.data.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.description.contains(query, ignoreCase = true) ||
                                it.location.contains(query, ignoreCase = true)
                    }
                }
                HomeUiState(isLoading = false, companies = filtered, searchQuery = query)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState(isLoading = true))

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}

// Company Detail ViewModel
data class CompanyDetailUiState(
    val isLoading: Boolean = false,
    val company: Company? = null,
    val services: List<Service> = emptyList(),
    val errorMessage: String? = null
)

class CompanyDetailViewModel(
    private val companyRepository: CompanyRepository = CompanyRepository(),
    private val serviceRepository: ServiceRepository = ServiceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyDetailUiState(isLoading = true))
    val uiState: StateFlow<CompanyDetailUiState> = _uiState.asStateFlow()

    fun loadCompanyDetails(companyId: String) {
        viewModelScope.launch {
            _uiState.value = CompanyDetailUiState(isLoading = true)
            when (val result = companyRepository.getCompanyById(companyId)) {
                is Resource.Error -> {
                    _uiState.value = CompanyDetailUiState(isLoading = false, errorMessage = result.message)
                }
                is Resource.Success -> {
                    val company = result.data
                    // Observe active services
                    serviceRepository.getActiveServicesForCompanyFlow(companyId).collect { serviceResource ->
                        when (serviceResource) {
                            is Resource.Loading -> {
                                _uiState.value = CompanyDetailUiState(isLoading = true, company = company)
                            }
                            is Resource.Error -> {
                                _uiState.value = CompanyDetailUiState(isLoading = false, company = company, errorMessage = serviceResource.message)
                            }
                            is Resource.Success -> {
                                _uiState.value = CompanyDetailUiState(isLoading = false, company = company, services = serviceResource.data)
                            }
                        }
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }
}

// Service Detail ViewModel
data class ServiceDetailUiState(
    val isLoading: Boolean = false,
    val service: Service? = null,
    val company: Company? = null,
    val errorMessage: String? = null
)

class ServiceDetailViewModel(
    private val serviceRepository: ServiceRepository = ServiceRepository(),
    private val companyRepository: CompanyRepository = CompanyRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceDetailUiState(isLoading = true))
    val uiState: StateFlow<ServiceDetailUiState> = _uiState.asStateFlow()

    fun loadServiceDetails(serviceId: String) {
        viewModelScope.launch {
            _uiState.value = ServiceDetailUiState(isLoading = true)
            when (val serviceResult = serviceRepository.getServiceById(serviceId)) {
                is Resource.Error -> {
                    _uiState.value = ServiceDetailUiState(isLoading = false, errorMessage = serviceResult.message)
                }
                is Resource.Success -> {
                    val service = serviceResult.data
                    val companyResult = companyRepository.getCompanyById(service.companyId)
                    val company = if (companyResult is Resource.Success) companyResult.data else null
                    _uiState.value = ServiceDetailUiState(isLoading = false, service = service, company = company)
                }
                is Resource.Loading -> {}
            }
        }
    }
}

// Submit Request ViewModel
data class SubmitRequestUiState(
    val isLoading: Boolean = false,
    val service: Service? = null,
    val company: Company? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val description: String = "",
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
)

class SubmitRequestViewModel(
    private val serviceRepository: ServiceRepository = ServiceRepository(),
    private val companyRepository: CompanyRepository = CompanyRepository(),
    private val requestRepository: RequestRepository = RequestRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubmitRequestUiState())
    val uiState: StateFlow<SubmitRequestUiState> = _uiState.asStateFlow()

    // Stable submission ID for form session idempotency
    private var sessionSubmissionId: String? = null

    fun loadService(serviceId: String, initialName: String, initialPhone: String) {
        if (sessionSubmissionId == null) {
            sessionSubmissionId = "req_" + UUID.randomUUID().toString().replace("-", "")
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                customerName = initialName,
                customerPhone = initialPhone
            )
            when (val serviceResult = serviceRepository.getServiceById(serviceId)) {
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = serviceResult.message)
                }
                is Resource.Success -> {
                    val service = serviceResult.data
                    val companyResult = companyRepository.getCompanyById(service.companyId)
                    val company = if (companyResult is Resource.Success) companyResult.data else null
                    _uiState.value = _uiState.value.copy(isLoading = false, service = service, company = company)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(customerName = name)
    }

    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(customerPhone = phone)
    }

    fun onDescriptionChange(desc: String) {
        _uiState.value = _uiState.value.copy(description = desc)
    }

    fun submitRequest(customerIdParam: String? = null) {
        val state = _uiState.value
        val service = state.service ?: return

        val activeUid = if (!customerIdParam.isNullOrBlank()) customerIdParam else auth.currentUser?.uid
        if (activeUid.isNullOrBlank()) {
            _uiState.value = state.copy(errorMessage = "User not authenticated. Please log in.")
            return
        }

        if (state.customerName.isBlank() || state.customerPhone.isBlank() || state.description.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please fill in all details")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = requestRepository.submitServiceRequest(
                customerId = activeUid,
                companyId = service.companyId,
                serviceId = service.id,
                serviceName = service.name,
                servicePrice = service.price,
                customerName = state.customerName,
                customerPhone = state.customerPhone,
                description = state.description,
                customRequestId = sessionSubmissionId
            )
            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSubmitted = true)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

// Customer Requests ViewModel
data class CustomerRequestsUiState(
    val isLoading: Boolean = false,
    val requests: List<CustomerRequestItemUiState> = emptyList(),
    val errorMessage: String? = null
)

class CustomerRequestsViewModel(
    private val requestRepository: RequestRepository = RequestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerRequestsUiState(isLoading = true))
    val uiState: StateFlow<CustomerRequestsUiState> = _uiState.asStateFlow()

    fun loadRequests(customerId: String) {
        viewModelScope.launch {
            requestRepository.getCustomerRequestsFlow(customerId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = CustomerRequestsUiState(isLoading = true)
                    is Resource.Error -> _uiState.value = CustomerRequestsUiState(isLoading = false, errorMessage = resource.message)
                    is Resource.Success -> _uiState.value = CustomerRequestsUiState(isLoading = false, requests = resource.data)
                }
            }
        }
    }

    fun cancelRequest(requestId: String) {
        viewModelScope.launch {
            val result = requestRepository.updateRequestStatus(requestId, RequestStatus.CANCELLED)
            if (result is Resource.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }
}
