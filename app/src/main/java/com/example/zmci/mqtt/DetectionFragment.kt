package com.example.zmci.mqtt

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zmci.R
import com.example.zmci.database.DatabaseHelper
import com.example.zmci.mqtt.adapter.DetectionAdapter
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.android.synthetic.main.fragment_detection.*
import kotlinx.android.synthetic.main.fragment_home.*

class DetectionFragment : Fragment() {

    companion object {
        lateinit var databaseHelper: DatabaseHelper
    }
    private lateinit var detectionAdapter: DetectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseHelper = DatabaseHelper(this.requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //set List
        val detectionList = databaseHelper.getAllDetection(requireContext())
        // set adapter
        detectionList.sortByDescending { it.id }
        detectionAdapter = DetectionAdapter(requireContext(), detectionList)
        //set find Id
        val rvReports: RecyclerView = rvDetection
        //set recycler view adapter
        rvReports.layoutManager = LinearLayoutManager(this.context)
        rvReports.adapter = detectionAdapter

        var adapter = detectionAdapter
        rvReports.adapter = adapter
        adapter.setOnItemClickListener(object : DetectionAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                val image             = detectionList[position].image
                val cameraName        = detectionList[position].cameraName
                val camera            = detectionList[position].camera
                val timestamp         = detectionList[position].timestamp
                val violators         = detectionList[position].violators
                val total_violations  = detectionList[position].total_violations
                val total_violators   = detectionList[position].total_violators

                val detectionBundle = bundleOf(
                    DETECTION_IMAGE_KEY      to image,
                    DETECTION_CAMERA_NAME_KEY to cameraName,
                    DETECTION_CAMERA_KEY     to camera,
                    DETECTION_TIMESTAMP_KEY  to timestamp,
                    DETECTION_VIOLATORS_KEY  to violators,
                    TOTAL_VIOLATIONS_KEY to total_violations,
                    TOTAL_VIOLATORS_KEY to total_violators)
                findNavController().navigate(
                    R.id.action_DetectionFragment_to_DetectionReportFragment, detectionBundle)
            }
        })
        //set dialog
        btnDeleteAll.setOnClickListener { deleteAllDetection() }
    }

    private fun deleteAllDetection() {
        val deleteDialog = AlertDialog.Builder(this.requireContext())

        deleteDialog.setTitle("Warning")
        deleteDialog.setMessage("Are you sure you want to permanently delete all records?")
        deleteDialog.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, _ ->
            if (databaseHelper.deleteAllDetection()) {
                detectionAdapter.notifyDataSetChanged()
                Toast.makeText(this.requireContext(), "All Records are Deleted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this.requireContext(), "Error Deleting", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        })
        deleteDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        deleteDialog.create()
        deleteDialog.show()
    }

}