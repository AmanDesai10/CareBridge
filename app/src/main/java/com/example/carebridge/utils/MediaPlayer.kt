package com.example.carebridge.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.carebridge.R

var mediaPlayer: MediaPlayer? = null

/**
 * playSound plays the alert sound when a fall is detected.
 * @param context the context of the activity or service
 */
fun playSound(context: Context) {
    // If mediaPlayer is already initialized, release it
    mediaPlayer?.release()

    // Initialize a new MediaPlayer
    mediaPlayer = MediaPlayer.create(context, R.raw.alert)

    // Start playing the sound
    mediaPlayer?.start()
}
