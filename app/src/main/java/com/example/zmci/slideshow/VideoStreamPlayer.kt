package com.example.zmci.slideshow

import android.content.Context
import android.net.Uri
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

class VideoStreamPlayer(context: Context, private var videoLayout: VLCVideoLayout)
{
    private var libVlc: LibVLC = LibVLC(context)
    private var mediaPlayer: MediaPlayer = MediaPlayer(libVlc)

    fun start(url: String)
    {
        mediaPlayer.attachViews(videoLayout, null, false, false)

        val media = Media(libVlc, Uri.parse(url))
        media.setHWDecoderEnabled(true, false)
        media.addOption(":network-caching=600")

        mediaPlayer.media = media
        media.release()
        mediaPlayer.play()
    }

    fun stop()
    {
        mediaPlayer.stop()
        mediaPlayer.detachViews()
    }

    fun release()
    {
        mediaPlayer.release()
        libVlc.release()
    }
}