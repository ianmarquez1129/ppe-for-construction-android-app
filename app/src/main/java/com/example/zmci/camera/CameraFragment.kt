package com.example.zmci.camera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zmci.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zmci.adapters.UsersRecyclerAdapter
import com.example.zmci.mqtt.MQTT_CLIENT_ID_KEY
import com.example.zmci.mqtt.MQTT_PWD_KEY
import com.example.zmci.mqtt.MQTT_SERVER_URI_KEY
import com.example.zmci.mqtt.MQTT_USERNAME_KEY
import kotlinx.android.synthetic.main.fragment_camera.*

class CameraFragment: Fragment() {
    private lateinit var addsBtn: FloatingActionButton
    private lateinit var recv: RecyclerView
    private lateinit var cameraList: ArrayList<CameraData>
    private lateinit var cameraAdapter: CameraAdapter

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
        cameraList = ArrayList()
        //set find Id
        recv = view.findViewById(R.id.mRecycler)
        // set adapter
        cameraAdapter = CameraAdapter(requireContext(),cameraList)
        //set recycler view adapter
        recv.layoutManager = LinearLayoutManager(this.context)

        //MQTT
        val serverURI   = arguments?.getString(MQTT_SERVER_URI_KEY)
        val clientId    = arguments?.getString(MQTT_CLIENT_ID_KEY)
        val username    = arguments?.getString(MQTT_USERNAME_KEY)
        val pwd         = arguments?.getString(MQTT_PWD_KEY)

//        val serverURIFromEditText   = view.findViewById<EditText>(R.id.edittext_server_uri).text.toString()
//        val clientIDFromEditText    = view.findViewById<EditText>(R.id.edittext_client_id).text.toString()
//        val usernameFromEditText    = view.findViewById<EditText>(R.id.edittext_username).text.toString()
//        val pwdFromEditText         = view.findViewById<EditText>(R.id.edittext_password).text.toString()

        val mqttCredentialsBundle = bundleOf(
            MQTT_SERVER_URI_KEY    to serverURI,
            MQTT_CLIENT_ID_KEY     to clientId,
            MQTT_USERNAME_KEY      to username,
            MQTT_PWD_KEY           to pwd)
        //MQTT end

        var adapter = cameraAdapter
        recv.adapter = adapter
        adapter.setOnItemClickListener(object: CameraAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                findNavController().navigate(R.id.action_CameraFragment_to_ClientFragment, mqttCredentialsBundle)
            }

        })
        //set dialog
//        addsBtn.setOnClickListener{ addInfo()}
        addingBtn.setOnClickListener{addInfo()}



    }

    private fun addInfo(){
        val inflater = LayoutInflater.from(this.context)
        val v = inflater.inflate(R.layout.fragment_add_camera,null)
        //set view
        val cameraName = v.findViewById<EditText>(R.id.cameraName)
        val notification = v.findViewById<SwitchCompat>(R.id.swNotification)
        val identifiers = v.findViewById<SwitchCompat>(R.id.swIdentifiers)
        val helmet = v.findViewById<SwitchCompat>(R.id.swHelmet)
        val glasses = v.findViewById<SwitchCompat>(R.id.swGlasses)
        val vest = v.findViewById<SwitchCompat>(R.id.swVest)
        val gloves = v.findViewById<SwitchCompat>(R.id.swGloves)
        val boots = v.findViewById<SwitchCompat>(R.id.swBoots)
        //camera status variable
//        val cameraStatus
        val addDialog = AlertDialog.Builder(this.requireContext())

        addDialog.setView(v)
        addDialog.setPositiveButton("Ok"){
                dialog,_->
            val names = cameraName.text.toString()
            val notification = notification.isChecked
            val identifiers = identifiers.isChecked
            val helmet = helmet.isChecked
            val glasses = glasses.isChecked
            val vest = vest.isChecked
            val gloves = gloves.isChecked
            val boots = boots.isChecked
            cameraList.add(CameraData("Name: $names","Status: $names",notification,identifiers,helmet,glasses,vest,gloves,boots))
            cameraAdapter.notifyDataSetChanged()
            Toast.makeText(this.context,"Adding Camera Information Success",Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        addDialog.setNegativeButton("Cancel"){
                dialog,_->
            dialog.dismiss()
            Toast.makeText(this.context,"Cancel",Toast.LENGTH_SHORT).show()
        }
        addDialog.create()
        addDialog.show()
    }

}