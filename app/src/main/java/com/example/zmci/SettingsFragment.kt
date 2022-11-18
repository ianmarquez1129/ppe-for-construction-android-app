package com.example.zmci

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.zmci.mqtt.*
import com.google.gson.Gson
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {

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
        Toast.makeText(context, "Preferences", Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings()
    }


    private fun loadSettings() {

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

        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val helmet = sp.getString("helmet", "both_helmet").toString()
        val glasses = sp.getString("glasses", "both_glasses").toString()
        val vest = sp.getString("vest", "both_vest").toString()
        val gloves = sp.getString("gloves", "both_gloves").toString()
        val boots = sp.getString("boots", "both_boots").toString()

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

        val gson = Gson()
        val newPreferences = "{\"ppe_preferences\":" + JSONObject(gson.toJson(ppePreferences)).toString() + "}"

        val serverUri = sp.getString("publish", "tcp://192.168.1.2:1883").toString()
        val topic = "rpi/set"

        Thread {
            val clientID = java.util.UUID.randomUUID().toString()
            val brokerUsername = MQTT_USERNAME
            val brokerPassword = MQTT_PWD

            // Open MQTT Broker communication
            mqttClient = MQTTClient(context, serverUri, clientID)

            if (mqttClient.isConnected()) {
                Log.d(this.javaClass.name, "MQTT is already connected")
            } else {
                // Connect and login to MQTT Broker
                mqttClient.connect(brokerUsername,
                    brokerPassword,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d(this.javaClass.name, "Connection success")

                            Toast.makeText(context, "MQTT Connection success", Toast.LENGTH_SHORT)
                                .show()
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {
                            Log.d(
                                this.javaClass.name,
                                "Connection failure: ${exception.toString()}"
                            )

                            Toast.makeText(
                                context,
                                "MQTT Connection fails: ${exception.toString()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }

            if (mqttClient.isConnected()) {
                Log.d(this.javaClass.name, "MQTT is Connected")
            } else {
                Log.d(this.javaClass.name, "MQTT failed to connect")
            }
        }.start()
        Thread {
            Thread.sleep(10000)
            if (mqttClient.isConnected()) {
                mqttClient.publish(topic,
                    newPreferences,
                    1,
                    false,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            val msg = "Publish message: $newPreferences to topic: $topic"
                            Log.d(this.javaClass.name, msg)

                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {
                            Log.d(this.javaClass.name, "Failed to publish message to topic")
                        }
                    })
            } else {
                Log.d(this.javaClass.name, "Impossible to publish, no server connected")
            }
        }.start()
    }
}