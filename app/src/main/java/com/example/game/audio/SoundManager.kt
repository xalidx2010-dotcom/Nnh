package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

class SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var focusTrackJob: Job? = null
    private var isMuted = false
    private var volume = 0.5f

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
    }

    fun setMute(mute: Boolean) {
        isMuted = mute
    }

    fun playFocusTrack(trackType: String) {
        stopFocusTrack()
        if (isMuted || trackType == "None") return

        focusTrackJob = scope.launch {
            val sampleRate = 22050
            val durationSec = 4.0f
            val numSamples = (durationSec * sampleRate).toInt()
            val buffer = ShortArray(numSamples)
            val random = Random()

            // Synthesize the focus soundscape
            for (i in 0 until numSamples) {
                val t = i.toFloat() / sampleRate
                var value = 0f

                when (trackType) {
                    "Rain" -> {
                        // Soft soothing rainfall (Low-passed brown/pink-like noise with droplet accents)
                        var noise = (random.nextFloat() * 2f - 1f)
                        // Simple 1st-order low-pass filter approximation on buffer
                        if (i > 0) {
                            noise = 0.15f * noise + 0.85f * (buffer[i - 1].toFloat() / Short.MAX_VALUE)
                        }
                        
                        // Droplet triggers
                        val droplet = if (random.nextFloat() > 0.9995f) {
                            val freq = 400f + random.nextFloat() * 300f
                            sin(2f * PI * freq * t).toFloat() * 0.3f
                        } else 0f

                        value = (noise * 0.7f + droplet * 0.3f) * 0.6f
                    }
                    "Zen Waves" -> {
                        // Deep celestial binaural waves (theta frequency)
                        // Left Ear: 100Hz, Right Ear: 104Hz -> creates 4Hz theta beat (stereo simulator)
                        val carrier = sin(2f * PI * 100f * t).toFloat()
                        val shift = sin(2f * PI * 104f * t).toFloat()
                        
                        // Slow sweep LFO
                        val lfo = sin(2f * PI * 0.1f * t).toFloat() * 0.15f + 0.85f
                        value = ((carrier + shift) * 0.5f) * lfo * 0.5f
                    }
                    "White Noise" -> {
                        // Pure comforting white noise (high-frequency balanced)
                        value = (random.nextFloat() * 2f - 1f) * 0.25f
                    }
                    "Forest Wind" -> {
                        // Whispering wind and forest foliage (slow LFO modulating low-frequency bands)
                        val lfo1 = sin(2f * PI * 0.2f * t).toFloat() // 5-second swell
                        val lfo2 = sin(2f * PI * 0.05f * t).toFloat() // 20-second sweep
                        
                        var noise = (random.nextFloat() * 2f - 1f)
                        if (i > 0) {
                            noise = 0.05f * noise + 0.95f * (buffer[i - 1].toFloat() / Short.MAX_VALUE)
                        }

                        val windFreq = 120f + lfo1 * 40f
                        val windBase = sin(2f * PI * windFreq * t).toFloat()

                        value = (windBase * 0.4f + noise * (0.3f + lfo2 * 0.1f)) * 0.7f
                    }
                    else -> {
                        value = 0f
                    }
                }

                // Apply master volume and write to short array
                buffer[i] = (value * volume * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            var audioTrack: AudioTrack? = null
            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.setLoopPoints(0, buffer.size, -1) // Infinite looping
                audioTrack.play()

                // Keep coroutine alive for continuous play
                while (true) {
                    delay(2000)
                    // Dynamic volume updates can be implemented if needed, otherwise loop
                }
            } catch (e: Exception) {
                Log.e("SoundManager", "Error playing focus soundscape: ${e.message}")
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (ignored: Exception) {}
            }
        }
    }

    fun stopFocusTrack() {
        focusTrackJob?.cancel()
        focusTrackJob = null
    }
}
