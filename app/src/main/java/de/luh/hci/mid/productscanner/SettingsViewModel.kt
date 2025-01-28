package de.luh.hci.mid.productscanner

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // State für die ausgewählte Stimme
    private val _selectedVoice = MutableStateFlow(loadSelectedVoice())
    val selectedVoice: StateFlow<String> get() = _selectedVoice

    // State für die Lautstärke
    private val _volumeLevel = MutableStateFlow(loadVolumeLevel())
    val volumeLevel: StateFlow<Float> get() = _volumeLevel

    // Funktion zum Laden der gespeicherten Stimme
    private fun loadSelectedVoice(): String {
        return sharedPreferences.getString("selected_voice", "Alloy") ?: "Alloy"
    }

    // Funktion zum Laden des gespeicherten Lautstärkepegels
    private fun loadVolumeLevel(): Float {
        return sharedPreferences.getFloat("volume_level", 50f)
    }

    // Funktion zum Aktualisieren der Stimme
    fun updateSelectedVoice(newVoice: String) {
        viewModelScope.launch {
            _selectedVoice.emit(newVoice)
            saveSelectedVoice(newVoice)
        }
    }

    // Funktion zum Speichern der Stimme
    private fun saveSelectedVoice(newVoice: String) {
        sharedPreferences.edit().putString("selected_voice", newVoice).apply()
    }

    // Funktion zum Aktualisieren der Lautstärke
    fun updateVolumeLevel(newLevel: Float) {
        viewModelScope.launch {
            _volumeLevel.emit(newLevel)
            saveVolumeLevel(newLevel)
        }
    }

    // Funktion zum Speichern der Lautstärke
    private fun saveVolumeLevel(newLevel: Float) {
        sharedPreferences.edit().putFloat("volume_level", newLevel).apply()
    }
}
