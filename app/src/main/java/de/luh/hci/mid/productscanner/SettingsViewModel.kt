package de.luh.hci.mid.productscanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    // State für die ausgewählte Stimme
    private val _selectedVoice = MutableStateFlow("Alloy")
    val selectedVoice: StateFlow<String> get() = _selectedVoice

    // State für die Lautstärke
    private val _volumeLevel = MutableStateFlow(50f)
    val volumeLevel: StateFlow<Float> get() = _volumeLevel

    // Funktion zum Aktualisieren der Stimme
    fun updateSelectedVoice(newVoice: String) {
        viewModelScope.launch {
            _selectedVoice.emit(newVoice)
        }
    }

    // Funktion zum Aktualisieren der Lautstärke
    fun updateVolumeLevel(newLevel: Float) {
        viewModelScope.launch {
            _volumeLevel.emit(newLevel)
        }
    }
}
