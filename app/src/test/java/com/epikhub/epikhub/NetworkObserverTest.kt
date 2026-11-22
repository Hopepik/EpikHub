package com.epikhub.epikhub

import com.epikhub.epikhub.util.NetworkObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeNetworkObserver(
    initialState: Boolean = true
) : NetworkObserver {
    private val _isOnline = MutableStateFlow(initialState)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }
}

class NetworkObserverTest {

    @Test
    fun testNetworkObserverInitialState() {
        val observer = FakeNetworkObserver(initialState = true)
        assertTrue(observer.isOnline.value)
    }

    @Test
    fun testNetworkObserverStateTransition() {
        val observer = FakeNetworkObserver(initialState = true)
        assertTrue(observer.isOnline.value)

        observer.setOnline(false)
        assertFalse(observer.isOnline.value)

        observer.setOnline(true)
        assertTrue(observer.isOnline.value)
    }
}
