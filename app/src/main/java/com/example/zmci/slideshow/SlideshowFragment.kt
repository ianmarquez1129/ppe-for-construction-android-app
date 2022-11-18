package com.example.zmci.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.zmci.databinding.FragmentSlideshowBinding
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import android.net.Uri
import androidx.navigation.fragment.findNavController
import com.example.zmci.R
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.fragment_slideshow.*

class SlideshowFragment : Fragment() {

    private var url: String = "rtsp://192.168.1.2:8554/"
//"http://192.168.55.103:4747/video"

    private lateinit var libVlc: LibVLC
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        libVlc = LibVLC(activity)
        mediaPlayer = MediaPlayer(libVlc)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_slideshow, container, false)

    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val videoLayout1 : VLCVideoLayout = videoLayout
//
//        btnStreamIP.setOnClickListener {
//            mediaPlayer.attachViews(videoLayout1, null, false, false)
//            val url = ""+streamIP.text.toString()
//            val media = Media(libVlc, Uri.parse(url))
//            media.setHWDecoderEnabled(true, false)
//            media.addOption(":network-caching=600")
//
//            mediaPlayer.media = media
//            media.release()
//            mediaPlayer.play()
//        }
//    }

    override fun onStart() {
        super.onStart()
        val videoLayout1 : VLCVideoLayout = videoLayout

        mediaPlayer.attachViews(videoLayout1, null, false, false)
        val url = "" + streamIP.text.toString()
        val media = Media(libVlc, Uri.parse(url))
        media.setHWDecoderEnabled(true, false)
        media.addOption(":network-caching=600")

        mediaPlayer.media = media
        media.release()
        mediaPlayer.play()
    }


    override fun onStop() {
        super.onStop()

        mediaPlayer.stop()
        mediaPlayer.detachViews()
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer.release()
        libVlc.release()
    }

}