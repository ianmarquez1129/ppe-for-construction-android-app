package com.example.zmci.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zmci.R
import com.example.zmci.database.DatabaseHelper
import com.example.zmci.mqtt.*
import kotlinx.android.synthetic.main.fragment_camera.*

class CameraFragment : Fragment() {

    companion object {
        lateinit var databaseHelper: DatabaseHelper
    }
    private lateinit var cameraAdapter: CameraAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseHelper = DatabaseHelper(this.requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //set List
        val cameraList = databaseHelper.getAllCamera(requireContext())
        // set adapter
        cameraAdapter = CameraAdapter(requireContext(), cameraList)
        //set find Id
        val recv: RecyclerView = mRecycler
        //set recycler view adapter
        recv.layoutManager = LinearLayoutManager(this.context)
        recv.adapter = cameraAdapter

        var adapter = cameraAdapter
        recv.adapter = adapter
        adapter.setOnItemClickListener(object : CameraAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                val cameraName  = cameraList[position].cameraName
                val serverURI   = cameraList[position].MQTT_SERVER_URI
                val clientID    = cameraList[position].MQTT_CLIENT_ID
                val username    = cameraList[position].MQTT_USERNAME
                val pwd         = cameraList[position].MQTT_PWD
                val topic       = cameraList[position].MQTT_TOPIC

                val mqttCredentialsBundle = bundleOf(
                    CAMERA_NAME_KEY        to cameraName,
                    MQTT_SERVER_URI_KEY    to serverURI,
                    MQTT_CLIENT_ID_KEY     to clientID,
                    MQTT_USERNAME_KEY      to username,
                    MQTT_PWD_KEY           to pwd,
                    MQTT_TOPIC_KEY         to topic)
                findNavController().navigate(
                    R.id.action_CameraFragment_to_ClientFragment, mqttCredentialsBundle)
            }
        })
        //set dialog
        addingBtn.setOnClickListener { addInfo() }


    }

    private fun addInfo() {
        val inflater = LayoutInflater.from(this.context)
        val v = inflater.inflate(R.layout.fragment_add_camera, null)
        //set view
        val cameraName = v.findViewById<EditText>(R.id.cameraName)
        val etServerUri = v.findViewById<EditText>(R.id.etServerUri)
        val etServerUsername = v.findViewById<EditText>(R.id.etServerUsername)
        val etServerPassword = v.findViewById<EditText>(R.id.etServerPassword)
        val etServerTopic = v.findViewById<EditText>(R.id.etServerTopic)
        val addDialog = AlertDialog.Builder(this.requireContext())

        addDialog.setView(v)
        addDialog.setPositiveButton("Ok") { dialog, _ ->
            if (cameraName.text.isEmpty() ||
                etServerUri.text.isEmpty() ||
                etServerUsername.text.isEmpty() ||
                etServerPassword.text.isEmpty() ||
                etServerTopic.text.isEmpty()) {
                Toast.makeText(this.context,"Complete all fields", Toast.LENGTH_SHORT).show()
                cameraName.requestFocus()
            }
            else {
                val camera = CameraData()
                camera.cameraName = cameraName.text.toString()
                camera.MQTT_SERVER_URI = etServerUri.text.toString()
                camera.MQTT_USERNAME = etServerUsername.text.toString()
                camera.MQTT_PWD = etServerPassword.text.toString()
                camera.MQTT_TOPIC = etServerTopic.text.toString()
                camera.MQTT_CLIENT_ID = java.util.UUID.randomUUID().toString()
                databaseHelper.addCamera(this.requireContext(), camera)
            }
            cameraAdapter.notifyDataSetChanged()

            dialog.dismiss()
        }
        addDialog.setNegativeButton("Cancel") { dialog, _ ->
            Toast.makeText(this.context, "Cancel", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        addDialog.create()
        addDialog.show()
    }

}