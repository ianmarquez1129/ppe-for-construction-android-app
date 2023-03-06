package com.example.zmci.mqtt

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zmci.R
import kotlinx.android.synthetic.main.fragment_detection_report.*
import org.json.JSONArray

class DetectionReportFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detection_report, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val image = arguments?.getString(DETECTION_IMAGE_KEY).toString()
        val cameraName = arguments?.getString(DETECTION_CAMERA_NAME_KEY).toString()
        val camera = arguments?.getString(DETECTION_CAMERA_KEY).toString()
        val timestamp = arguments?.getString(DETECTION_TIMESTAMP_KEY).toString()
        val violators = arguments?.getString(DETECTION_VIOLATORS_KEY).toString()
        val total_violations = arguments?.getString(TOTAL_VIOLATIONS_KEY).toString()
        val total_violators = arguments?.getString(TOTAL_VIOLATORS_KEY).toString()

        //decode base64 to image
        val decodedByte = Base64.decode(image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
        imageReport.setImageBitmap(bitmap)

        cameraReport.text = "Camera details: \n"
        timestampReport.text = "Timestamp: $timestamp"
        violatorsReport.text = "Details: \n"
        totalViolationsReport.text = "Detected PPE: $total_violations"
        totalViolatorsReport.text = "Person: $total_violators"

        try {
            val cameraObject = JSONArray("[ $camera ]")
            for (i in 0 until cameraObject.length()) {
                val itemCamera = cameraObject.getJSONObject(i)
//                val camName = itemCamera.getString("name")
//                val camDesc = itemCamera.getString("description")
                val camIP = itemCamera.getString("ip_address")
                cameraReport.append(
                    "Camera name: $cameraName\n" +
                            "IP: $camIP"
                )
            }
            val violatorsObject = JSONArray(violators)
            try {
                for (j in 0 until violatorsObject.length()) {
                    val itemViolators = violatorsObject.getJSONObject(j)
//                    val personId = itemViolators.getString("id")
//                    violatorsReport.append("ID: $personId\n")
                    val personInfo = itemViolators.getString("person_info")
                    val personInfoObject = JSONArray(personInfo)

                    for (k in 0 until personInfoObject.length()) {
                        val itemPI = personInfoObject.getJSONObject(k)
                        val personID = itemPI.getString("person_id")
                        val firstName = itemPI.getString("first_name")
                        val middleName = itemPI.getString("middle_name")
                        val lastName = itemPI.getString("last_name")
                        val jobTitle = itemPI.getString("job_title")
                        val overlaps = itemPI.getString("overlaps")
                        violatorsReport.append(
                            "ID: $personID\n" +
                                    "Name: $firstName $middleName $lastName\n" +
                                    "Job Title: $jobTitle\n" +
                                    "Overlaps: $overlaps\n" +
                                    "-----\n"
                        )
                    }

                    val personUniqueID = itemViolators.getString("id")
                    val violations = itemViolators.getString("violations")
                    val violationsObject = JSONArray(violations)
                    violatorsReport.append("\nPerson ID: $personUniqueID \n")
                    violatorsReport.append("Detections: \n")
                    try {
                        for (l in 0 until violationsObject.length()) {
                            val itemV = violationsObject.getJSONObject(l)
                            val className = itemV.getString("class_name")
                            violatorsReport.append("$className\n")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    violatorsReport.append("-----\n")
                }
            } catch (e:Exception) {
                e.printStackTrace()
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
}