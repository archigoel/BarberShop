package com.example.barbershop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BarberViewModel ( private val shop: Shop) : ViewModel() {
    private val _uiState = MutableStateFlow(ShopState())
    val uiState = _uiState.asStateFlow()

   // var timeScale by mutableStateOf(10000f)
    var timeScale by mutableStateOf(60f)
    private var job: Job? = null
    fun togglePlayPause() {

        if (job == null) {
            // First time starting
            _uiState.update { it.copy(isPaused = false) }
            startSimulation()
        } else {
            // Toggling during the run
            _uiState.update { currentState ->
                currentState.copy(isPaused = !currentState.isPaused)
            }
        }
    }

    fun restartSimulation() {
        job?.cancel() // Stop current loop
        job = null
        _uiState.update { state ->
            state.copy(
                currentTime = "09:00",
                activeBarbers = emptyList(),
                waitingRoom = emptyList(),
                isPaused = false
            )
        }
        startSimulation()
    }

    fun startSimulation() {
        job = viewModelScope.launch(Dispatchers.Default) {
            val engine = BarberShopEngine(shop)
            for (minute in 540..1020) {
                while (_uiState.value.isPaused) { delay(100) } // Pause logic

                engine.tick(minute)
                _uiState.update { state ->
                    state.copy(
                        currentTime = engine.formatTime(minute),
                        waitingRoom = engine.waitingRoom.toList(),
                        activeBarbers = engine.activeBarbers.map { it.copy() },
                        isClosed = minute >= 1020,
                        isPaused = minute >= 1020
                    )
                }

                val delayTime = (60000 / timeScale).toLong()
                delay(delayTime)
            }
        }
    }
}
data class ShopState(
    val currentTime: String = "09:00",
    val waitingRoom: List<Customer> = emptyList(),
    val activeBarbers: List<Barber> = emptyList(),
    val isClosed: Boolean = false,
    val isPaused: Boolean = true,
    val logs: List<String> = emptyList() // For that text-file style feed
)
class BarberViewModelFactory(private val shopData: Shop) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BarberViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BarberViewModel(shopData) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}