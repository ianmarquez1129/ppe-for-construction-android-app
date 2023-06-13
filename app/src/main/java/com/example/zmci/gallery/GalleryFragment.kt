package com.example.zmci.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.zmci.databinding.FragmentGalleryBinding
import kotlinx.android.synthetic.main.fragment_gallery.btnRefresh
import kotlinx.android.synthetic.main.fragment_gallery.btnStream
import kotlinx.android.synthetic.main.fragment_gallery.etStream
import kotlinx.android.synthetic.main.fragment_gallery.text_gallery
import kotlinx.android.synthetic.main.fragment_gallery.webStreamVideo


class GalleryFragment : Fragment() {

    private var etStreamString = ""
    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        galleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = "Configure"
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Viewing of any video url

//        val videoView: VideoView = streamVideo
//        val mediaController = MediaController(activity)
//        mediaController.setAnchorView(videoView)
//        videoView.setMediaController(mediaController)
//        //sample http audio only = https://stream-14.zeno.fm/mdrkm4npms8uv?zs=HnPdhsx2TWObn3mAZrskBQ
//        videoView.setVideoURI(Uri.parse("https://stream-14.zeno.fm/mdrkm4npms8uv?zs=HnPdhsx2TWObn3mAZrskBQ"))
//        videoView.start()

        val btnStream = btnStream as Button
        val webview = webStreamVideo as WebView
        webview.webViewClient = WebViewClient()
        webview.settings.javaScriptEnabled = true
        webview.settings.javaScriptCanOpenWindowsAutomatically = true
        webview.settings.pluginState = WebSettings.PluginState.ON
        webview.settings.mediaPlaybackRequiresUserGesture = false
        webview.settings.loadWithOverviewMode = true
        webview.settings.useWideViewPort = true
        webview.webChromeClient = WebChromeClient()
        btnStream.setOnClickListener {
            webview.loadUrl(""+etStream.text.toString())
            etStreamString = ""+etStream.text.toString()
            text_gallery.text = "Streaming "+etStream.text.toString()
            etStream.setText("")
        }
        btnRefresh.setOnClickListener {
            webview.loadUrl(""+etStreamString)
        }

        //Checkpoint commit

    }
}