package com.example.zmci.mqtt

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zmci.R
import kotlinx.android.synthetic.main.fragment_client.*
import kotlinx.android.synthetic.main.fragment_detection_report.*

class DetectionReportFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detection_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val image = arguments?.getString(DETECTION_IMAGE_KEY).toString()
        val camera = arguments?.getString(DETECTION_CAMERA_KEY).toString()
        val timestamp = arguments?.getString(DETECTION_TIMESTAMP_KEY).toString()
        val violators = arguments?.getString(DETECTION_VIOLATORS_KEY).toString()
        val total_violations = arguments?.getString(TOTAL_VIOLATIONS_KEY).toString()
        val total_violators = arguments?.getString(TOTAL_VIOLATORS_KEY).toString()

        //decode base64 to image
        val decodedByte = Base64.decode(image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
        imageReport.setImageBitmap(bitmap)

        cameraReport.text = "Camera details: $camera"
        timestampReport.text = "Timestamp: $timestamp"
        violatorsReport.text = "Details: $violators"
        totalViolationsReport.text = "Total violations: $total_violations"
        totalViolatorsReport.text = "Total violators: $total_violators"
    }
}