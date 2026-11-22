package com.epikhub.epikhub

import com.epikhub.epikhub.data.model.RequestStatus
import com.epikhub.epikhub.data.model.UserRole
import com.epikhub.epikhub.data.repository.RequestRepository
import org.junit.Assert.*
import org.junit.Test

class RequestStatusTest {

    @Test
    fun testRequestStatusParsing() {
        assertEquals(RequestStatus.PENDING, RequestStatus.fromString("PENDING"))
        assertEquals(RequestStatus.ACCEPTED, RequestStatus.fromString("accepted"))
        assertEquals(RequestStatus.IN_PROGRESS, RequestStatus.fromString("IN_PROGRESS"))
        assertEquals(RequestStatus.COMPLETED, RequestStatus.fromString("COMPLETED"))
        assertEquals(RequestStatus.CANCELLED, RequestStatus.fromString("cancelled"))
        assertEquals(RequestStatus.REJECTED, RequestStatus.fromString("rejected"))
        assertEquals(RequestStatus.PENDING, RequestStatus.fromString(null))
        assertEquals(RequestStatus.PENDING, RequestStatus.fromString("unknown_status"))
    }

    @Test
    fun testUserRoleParsing() {
        assertEquals(UserRole.CUSTOMER, UserRole.fromString("customer"))
        assertEquals(UserRole.COMPANY_ADMIN, UserRole.fromString("company_admin"))
        assertEquals(UserRole.COMPANY_ADMIN, UserRole.fromString("ADMIN"))
        assertEquals(UserRole.CUSTOMER, UserRole.fromString(null))
    }

    @Test
    fun testRequestMapStructure() {
        val map = RequestRepository.buildRequestMap(
            id = "req_123",
            customerId = "cust_456",
            companyId = "epik_compu_tech",
            serviceId = "epik_service_1",
            serviceName = "Computer Repair & Maintenance",
            servicePrice = 30000.0,
            customerName = "John Customer",
            customerPhone = "+265888000111",
            description = "Fix screen",
            createdAt = 1700000000000,
            updatedAt = 1700000000000
        )

        assertEquals(12, map.size)
        assertTrue(map.containsKey("id"))
        assertTrue(map.containsKey("customerId"))
        assertTrue(map.containsKey("companyId"))
        assertTrue(map.containsKey("serviceId"))
        assertTrue(map.containsKey("serviceName"))
        assertTrue(map.containsKey("servicePrice"))
        assertTrue(map.containsKey("customerName"))
        assertTrue(map.containsKey("customerPhone"))
        assertTrue(map.containsKey("description"))
        assertTrue(map.containsKey("status"))
        assertTrue(map.containsKey("createdAt"))
        assertTrue(map.containsKey("updatedAt"))

        assertFalse("Map must NOT contain requestStatus", map.containsKey("requestStatus"))
        assertEquals("PENDING", map["status"])
        assertEquals("req_123", map["id"])
        assertEquals("cust_456", map["customerId"])
        assertEquals("epik_compu_tech", map["companyId"])
        assertEquals("epik_service_1", map["serviceId"])
        assertEquals(30000.0, map["servicePrice"])
    }
}
