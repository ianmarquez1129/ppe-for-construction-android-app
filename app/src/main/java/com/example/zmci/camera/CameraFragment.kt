package com.example.zmci.camera

import android.os.AsyncTask
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zmci.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.zmci.adapters.UsersRecyclerAdapter
import com.example.zmci.mqtt.MQTT_CLIENT_ID_KEY
import com.example.zmci.mqtt.MQTT_PWD_KEY
import com.example.zmci.mqtt.MQTT_SERVER_URI_KEY
import com.example.zmci.mqtt.MQTT_USERNAME_KEY
import kotlinx.android.synthetic.main.fragment_camera.*
import com.example.zmci.database.DatabaseHelper
import kotlinx.android.synthetic.main.fragment_add_camera.*

class CameraFragment : Fragment() {

    companion object {
        lateinit var databaseHelper: DatabaseHelper
    }
    private lateinit var recv: RecyclerView
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
        val cameraAdapter = CameraAdapter(requireContext(), cameraList)
        //set find Id
        val recv: RecyclerView = mRecycler
        //set recycler view adapter
        recv.layoutManager = LinearLayoutManager(this.context)
        recv.adapter = cameraAdapter
//        viewCamera()


        //MQTT
        val serverURI = arguments?.getString(MQTT_SERVER_URI_KEY)
        val clientId = arguments?.getString(MQTT_CLIENT_ID_KEY)
        val username = arguments?.getString(MQTT_USERNAME_KEY)
        val pwd = arguments?.getString(MQTT_PWD_KEY)

//        val serverURIFromEditText   = view.findViewById<EditText>(R.id.edittext_server_uri).text.toString()
//        val clientIDFromEditText    = view.findViewById<EditText>(R.id.edittext_client_id).text.toString()
//        val usernameFromEditText    = view.findViewById<EditText>(R.id.edittext_username).text.toString()
//        val pwdFromEditText         = view.findViewById<EditText>(R.id.edittext_password).text.toString()

        val mqttCredentialsBundle = bundleOf(
            MQTT_SERVER_URI_KEY to serverURI,
            MQTT_CLIENT_ID_KEY to clientId,
            MQTT_USERNAME_KEY to username,
            MQTT_PWD_KEY to pwd
        )
        //MQTT end

        var adapter = cameraAdapter
        recv.adapter = adapter
        adapter.setOnItemClickListener(object : CameraAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                findNavController().navigate(
                    R.id.action_CameraFragment_to_ClientFragment,
                    mqttCredentialsBundle
                )
            }
        })
        //set dialog
        addingBtn.setOnClickListener { addInfo() }


    }

//    private fun viewCamera() {
//        //set List
//        val cameraList = databaseHelper.getAllCamera(requireContext())
//        // set adapter
//        val cameraAdapter = CameraAdapter(requireContext(), cameraList)
//        //set find Id
//        val recv: RecyclerView = mRecycler
//        //set recycler view adapter
//        recv.layoutManager = LinearLayoutManager(this.context)
//        recv.adapter = cameraAdapter
//
//        var getDataFromSQLite = GetDataFromSQLite()
//        getDataFromSQLite.execute()
//    }

    private fun addInfo() {
        val inflater = LayoutInflater.from(this.context)
        val v = inflater.inflate(R.layout.fragment_add_camera, null)
        //set view
        val cameraName = v.findViewById<EditText>(R.id.cameraName)
        val addDialog = AlertDialog.Builder(this.requireContext())

        addDialog.setView(v)
        addDialog.setPositiveButton("Ok") { dialog, _ ->
            if (cameraName.text.isEmpty()) {
                Toast.makeText(this.context,"Enter Camera Name", Toast.LENGTH_SHORT).show()
                cameraName.requestFocus()
            }
            else {
                val camera = CameraData()
                camera.cameraName = cameraName.text.toString()
                databaseHelper.addCamera(this.requireContext(), camera)
            }
//            val names = cameraName.text.toString()
//            cameraList.add(CameraData(id, names))
//            cameraAdapter.notifyDataSetChanged()
//            Toast.makeText(this.context, "Adding Camera Information Success", Toast.LENGTH_SHORT)
//                .show()
            //set List
            dialog.dismiss()
        }
        addDialog.setNegativeButton("Cancel") { dialog, _ ->
            Toast.makeText(this.context, "Cancel", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        addDialog.create()
        addDialog.show()
    }

//    override fun onResume() {
//        viewCamera()
//        super.onResume()
//    }



//    inner class GetDataFromSQLite : AsyncTask<Void, Void, List<CameraData>>() {
//
//        override fun doInBackground(vararg p0: Void?): List<CameraData> {
//            return databaseHelper.getAllCamera()
//        }
//
//        override fun onPostExecute(result: List<CameraData>?) {
//            super.onPostExecute(result)
//            cameraList.clear()
//            cameraList.addAll(result!!)
//        }
//
//    }

}