package com.example.zmci

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.zmci.mqtt.MQTTClient
import com.example.zmci.mqtt.MQTT_CLIENT_ID
import com.example.zmci.mqtt.MQTT_PWD
import com.example.zmci.mqtt.MQTT_USERNAME
import com.google.gson.Gson
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {

    /*
        SettingsActivity Methods:
            - onCreate        (savedInstanceState: Bundle?)
            - onDestroyView   ()
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var mqttClient: MQTTClient

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings()
    }


    private fun loadSettings() {
        Log.d(this.javaClass.name, "Started MAIN loadSettings thread")

        //initialize PreferenceManager
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        
        //initialize Gson for parsing JSON

        val gson = Gson()

        val clientID = MQTT_CLIENT_ID
        val brokerUsername = MQTT_USERNAME
        val brokerPassword = MQTT_PWD

        Thread {
            Log.d(this.javaClass.name, "Started Thread 1")
            //Interval before connecting to Server URI
            Thread.sleep(18000)
            try{
                // Get server URI from user input
                val serverUri = sp.getString("server_uri", "tcp://10.42.0.1:1883").toString()
                // Open MQTT Broker communication
                mqttClient = MQTTClient(context, serverUri, clientID)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                Thread.sleep(1000)
                // Check if MQTT is connected
                if (mqttClient.isConnected()) {
                    Log.d(this.javaClass.name, "MQTT is already connected")
                } else {
                    // Connect and login to MQTT Broker
                    try {
                        mqttClient.connect(brokerUsername,
                            brokerPassword,
                            object : IMqttActionListener {
                                override fun onSuccess(asyncActionToken: IMqttToken?) {
                                    Log.d(this.javaClass.name, "Connection success")
                                }
                                override fun onFailure(
                                    asyncActionToken: IMqttToken?,
                                    exception: Throwable?
                                ) {
                                    Log.d(
                                        this.javaClass.name,
                                        "Connection failure: ${exception.toString()}"
                                    )
                                }
                            })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e:Exception){
                e.printStackTrace()
            }
            Log.d(this.javaClass.name, "Finished thread 1")
        }.start()
        Thread {
            Log.d(this.javaClass.name, "Started thread 2")
            // Interval before executing Publish
            Thread.sleep(25000)
            try {
                if (mqttClient.isConnected()) {
                    try {
                        while (mqttClient.isConnected()) {

                            // Initialize preferences values and its defaults
                            val ppePreferences = HashMap<String, Boolean>()
                            ppePreferences["helmet"] = true
                            ppePreferences["no_helmet"] = true
                            ppePreferences["glasses"] = true
                            ppePreferences["no_glasses"] = true
                            ppePreferences["vest"] = true
                            ppePreferences["no_vest"] = true
                            ppePreferences["gloves"] = true
                            ppePreferences["no_gloves"] = true
                            ppePreferences["boots"] = true
                            ppePreferences["no_boots"] = true

                            // Get user input for PPE
                            val helmet = sp.getString("helmet", "both_helmet").toString()
                            val glasses = sp.getString("glasses", "both_glasses").toString()
                            val vest = sp.getString("vest", "both_vest").toString()
                            val gloves = sp.getString("gloves", "both_gloves").toString()
                            val boots = sp.getString("boots", "both_boots").toString()

                            // Check when the user change PPE preference
                            when (helmet) {
                                "with_helmet" -> {
                                    ppePreferences["helmet"] = true
                                    ppePreferences["no_helmet"] = false
                                }
                                "without_helmet" -> {
                                    ppePreferences["helmet"] = false
                                    ppePreferences["no_helmet"] = true
                                }
                                "both_helmet" -> {
                                    ppePreferences["helmet"] = true
                                    ppePreferences["no_helmet"] = true
                                }
                                "no_helmet" -> {
                                    ppePreferences["helmet"] = false
                                    ppePreferences["no_helmet"] = false
                                }
                            }
                            when (glasses) {
                                "with_glasses" -> {
                                    ppePreferences["glasses"] = true
                                    ppePreferences["no_glasses"] = false
                                }
                                "without_glasses" -> {
                                    ppePreferences["glasses"] = false
                                    ppePreferences["no_glasses"] = true
                                }
                                "both_glasses" -> {
                                    ppePreferences["glasses"] = true
                                    ppePreferences["no_glasses"] = true
                                }
                                "no_glasses" -> {
                                    ppePreferences["glasses"] = false
                                    ppePreferences["no_glasses"] = false
                                }
                            }
                            when (vest) {
                                "with_vest" -> {
                                    ppePreferences["vest"] = true
                                    ppePreferences["no_vest"] = false
                                }
                                "without_vest" -> {
                                    ppePreferences["vest"] = false
                                    ppePreferences["no_vest"] = true
                                }
                                "both_vest" -> {
                                    ppePreferences["vest"] = true
                                    ppePreferences["no_vest"] = true
                                }
                                "no_vest" -> {
                                    ppePreferences["vest"] = false
                                    ppePreferences["no_vest"] = false
                                }
                            }
                            when (gloves) {
                                "with_gloves" -> {
                                    ppePreferences["gloves"] = true
                                    ppePreferences["no_gloves"] = false
                                }
                                "without_gloves" -> {
                                    ppePreferences["gloves"] = false
                                    ppePreferences["no_gloves"] = true
                                }
                                "both_gloves" -> {
                                    ppePreferences["gloves"] = true
                                    ppePreferences["no_gloves"] = true
                                }
                                "no_gloves" -> {
                                    ppePreferences["gloves"] = false
                                    ppePreferences["no_gloves"] = false
                                }
                            }
                            when (boots) {
                                "with_boots" -> {
                                    ppePreferences["boots"] = true
                                    ppePreferences["no_boots"] = false
                                }
                                "without_boots" -> {
                                    ppePreferences["boots"] = false
                                    ppePreferences["no_boots"] = true
                                }
                                "both_boots" -> {
                                    ppePreferences["boots"] = true
                                    ppePreferences["no_boots"] = true
                                }
                                "no_boots" -> {
                                    ppePreferences["boots"] = false
                                    ppePreferences["no_boots"] = false
                                }
                            }

                            // Get MQTT topic from user input
                            val topic = sp.getString("topic", "rpi/set").toString()
                            // Generate finalized preferences for Publishing by converting to JSON
                            val newPreferences = "{\"ppe_preferences\":" + JSONObject(gson.toJson(ppePreferences)).toString() + "}"
                            // Publish the Preferences
                            mqttClient.publish(topic,
                                newPreferences,
                                1,
                                false,
                                object : IMqttActionListener {
                                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                                        val msg =
                                            "Publish message: $newPreferences to topic: $topic"
                                        Log.d(this.javaClass.name, msg)
                                    }

                                    override fun onFailure(
                                        asyncActionToken: IMqttToken?,
                                        exception: Throwable?
                                    ) {
                                        Log.d(
                                            this.javaClass.name,
                                            "Failed to publish message to topic"
                                        )
                                    }
                                })
                            // Loop interval for PPE preference publishing
                            Thread.sleep(3000)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Log.d(this.javaClass.name, "Impossible to publish, no server connected")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.d(this.javaClass.name, "Finished thread 2")
        }.start()
        Log.d(this.javaClass.name, "Finished MAIN loadSettings thread")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        
        if (mqttClient.isConnected()) {
            // Disconnect from MQTT Broker
            mqttClient.disconnect(object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(this.javaClass.name, "[OnDestroyView] Disconnected")
                }
                override fun onFailure(
                    asyncActionToken: IMqttToken?,
                    exception: Throwable?
                ) {
                    Log.d(this.javaClass.name, "[OnDestroyView] Failed to disconnect")
                }
            })
        } else {
            Log.d(this.javaClass.name, "[OnDestroyView] Impossible to disconnect, no server connected")
        }
        
    }
}