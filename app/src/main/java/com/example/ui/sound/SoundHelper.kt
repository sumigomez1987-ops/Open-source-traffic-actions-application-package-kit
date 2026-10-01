package com.example.ui.sound

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.SoundEffectConstants

object SoundHelper {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (_: Exception) {}
    }

    fun playButtonClick(context: Context? = null) {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
        } catch (_: Exception) {}
        try {
            context?.let { ctx ->
                val audio = ctx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audio?.playSoundEffect(SoundEffectConstants.CLICK, 1.0f)
            }
        } catch (_: Exception) {}
    }

    fun playSuccessTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 80)
        } catch (_: Exception) {}
    }

    fun playErrorTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 100)
        } catch (_: Exception) {}
    }
}
