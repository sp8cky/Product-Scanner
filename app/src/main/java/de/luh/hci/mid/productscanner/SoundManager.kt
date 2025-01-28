package de.luh.hci.mid.productscanner

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

object SoundManager {
    private lateinit var soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()

    // Initialisierung
    fun initialize(context: Context) {
        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Maximal 5 gleichzeitige Sounds
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        // Sounds laden
        soundMap["delete"] = soundPool.load(context, R.raw.delete, 1)
        soundMap["error"] = soundPool.load(context, R.raw.error, 1)
        soundMap["success"] = soundPool.load(context, R.raw.success, 1)
        soundMap["tap"] = soundPool.load(context, R.raw.tap, 1)
        soundMap["ping"] = soundPool.load(context, R.raw.ping, 1)
    }

    // Sound abspielen
    fun playSound(soundKey: String) {
        val soundId = soundMap[soundKey] ?: return // Wenn der Sound nicht existiert, nichts tun
        soundPool.play(soundId, 0.2f, 0.2f, 0, 0, 1f) // linkVolume, rightVolume, priority, loop, rate
    }

    // Ressourcen freigeben
    fun release() {
        soundPool.release()
    }
}
