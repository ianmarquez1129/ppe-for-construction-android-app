package com.example.zmci.mqtt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zmci.MainActivity
import com.example.zmci.R
import kotlinx.android.synthetic.main.fragment_client.*
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class ClientFragment : Fragment() {
    private lateinit var mqttClient: MQTTClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                if (mqttClient.isConnected()) {
//                    // Disconnect from MQTT Broker
//                    mqttClient.disconnect(object : IMqttActionListener {
//                        override fun onSuccess(asyncActionToken: IMqttToken?) {
//                            Log.d(this.javaClass.name, "Disconnected")
//
//                            Toast.makeText(
//                                context,
//                                "MQTT Disconnection success",
//                                Toast.LENGTH_SHORT
//                            ).show()
//
//                            // Disconnection success, come back to Connect Fragment
//                            findNavController().navigate(R.id.action_ClientFragment_to_ConnectFragment)
//                        }
//
//                        override fun onFailure(
//                            asyncActionToken: IMqttToken?,
//                            exception: Throwable?
//                        ) {
//                            Log.d(this.javaClass.name, "Failed to disconnect")
//                        }
//                    })
//                } else {
//                    Log.d(this.javaClass.name, "Impossible to disconnect, no server connected")
//                }
//            }
//        })
    }

    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME = "channelName"
    val NOTIFICATION_ID = 1
    override fun onDestroy() {
        super.onDestroy()
        createNotificationChannel()
        val intentNotify = Intent(context, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intentNotify)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setContentTitle("PPE notification")
            .setContentText("The application has been closed. Tap here to reopen.")
            .setSmallIcon(R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()
        val notificationManager = NotificationManagerCompat.from(requireContext())
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lightColor = Color.GREEN
                enableLights(true)
            }
            val manager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_client, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get arguments passed by ConnectFragment
        val serverURI = arguments?.getString(MQTT_SERVER_URI_KEY).toString()
        val clientId = arguments?.getString(MQTT_CLIENT_ID_KEY).toString()
        val username = arguments?.getString(MQTT_USERNAME_KEY).toString()
        val pwd = arguments?.getString(MQTT_PWD_KEY).toString()
        val topic = arguments?.getString(MQTT_TEST_TOPIC).toString()

        // Open MQTT Broker communication
        mqttClient = MQTTClient(context, serverURI, clientId)

        try {

            if (mqttClient.isConnected()) {
                Log.d(this.javaClass.name, "MQTT is already connected")
                mqttClient.disconnect(object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(this.javaClass.name, "Disconnected")

                        Toast.makeText(context, "MQTT Disconnection success", Toast.LENGTH_SHORT)
                            .show()

                        // Disconnection success, come back to Connect Fragment
                        findNavController().navigate(R.id.action_ClientFragment_to_CameraFragment)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(this.javaClass.name, "Failed to disconnect")
                    }
                })

            } else {
                // Connect and login to MQTT Broker
                mqttClient.connect(username,
                    pwd,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d(this.javaClass.name, "Connection success")

//                            Toast.makeText(context, "MQTT Connection success", Toast.LENGTH_SHORT)
//                                .show()
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

                            // Come back to Connect Fragment
                            findNavController().navigate(R.id.action_ClientFragment_to_CameraFragment)
                        }
                    },
                    object : MqttCallback {
                        override fun messageArrived(topic: String?, message: MqttMessage?) {
                            val msg = "Receive message: ${message.toString()} from topic: $topic"
                            Log.d(this.javaClass.name, msg)

//                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                            try {
                                val jsonData = "[ ${message.toString()} ]"
                                val obj = JSONArray(jsonData)
                                val imageObj: JSONObject = obj.getJSONObject(0)
                                val imageData = imageObj.getString("image") // get image
                                val violatorsData = imageObj.getString("violators")
//                            val violatorsArray = JSONArray(violatorsData)

                                //clear text after every loop
                                textDetect.text = ""
                                tvTimestamp.text = ""

                                textDetect.append(violatorsData)
                                textDetect.append("\n")

                                val cameraData = imageObj.getString("camera")
//                            val cameraArray = JSONArray(cameraData)
                                textDetect.append(cameraData)

                                val timestampData = imageObj.getString("timestamp")
                                tvTimestamp.text = timestampData

                                //decode base64 to image
                                val decodedByte = Base64.decode(imageData, Base64.DEFAULT)
                                val bitmap =
                                    BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
                                imgDetect.setImageBitmap(bitmap)

                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }

//                        textDetect.text = msg
                        }

                        override fun connectionLost(cause: Throwable?) {
                            Log.d(this.javaClass.name, "Connection lost ${cause.toString()}")
                        }

                        override fun deliveryComplete(token: IMqttDeliveryToken?) {
                            Log.d(this.javaClass.name, "Delivery complete")
                        }
                    })
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }

//        view.findViewById<Button>(R.id.button_prefill_client).setOnClickListener {
//            // Set default values in edit texts
//            view.findViewById<EditText>(R.id.edittext_pubtopic).setText(MQTT_TEST_TOPIC)
//            view.findViewById<EditText>(R.id.edittext_pubmsg).setText(MQTT_TEST_MSG)
//            view.findViewById<EditText>(R.id.edittext_subtopic).setText(MQTT_TEST_TOPIC)
//        }
//
//        view.findViewById<Button>(R.id.button_clean_client).setOnClickListener {
//            // Clean values in edit texts
//            view.findViewById<EditText>(R.id.edittext_pubtopic).setText("")
//            view.findViewById<EditText>(R.id.edittext_pubmsg).setText("")
//            view.findViewById<EditText>(R.id.edittext_subtopic).setText("")
//        }

        view.findViewById<Button>(R.id.button_disconnect).setOnClickListener {
            if (mqttClient.isConnected()) {
                // Disconnect from MQTT Broker
                mqttClient.disconnect(object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(this.javaClass.name, "Disconnected")

                        Toast.makeText(context, "MQTT Disconnection success", Toast.LENGTH_SHORT)
                            .show()

                        // Disconnection success, come back to Connect Fragment
                        findNavController().navigate(R.id.action_ClientFragment_to_CameraFragment)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(this.javaClass.name, "Failed to disconnect")
                    }
                })
            } else {
                Log.d(this.javaClass.name, "Impossible to disconnect, no server connected")
            }
        }

//        view.findViewById<Button>(R.id.button_publish).setOnClickListener {
//            val topic = view.findViewById<EditText>(R.id.edittext_pubtopic).text.toString()
//            val message = view.findViewById<EditText>(R.id.edittext_pubmsg).text.toString()
//
//            if (mqttClient.isConnected()) {
//                mqttClient.publish(topic,
//                    message,
//                    1,
//                    false,
//                    object : IMqttActionListener {
//                        override fun onSuccess(asyncActionToken: IMqttToken?) {
//                            val msg = "Publish message: $message to topic: $topic"
//                            Log.d(this.javaClass.name, msg)
//
//                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
//                        }
//
//                        override fun onFailure(
//                            asyncActionToken: IMqttToken?,
//                            exception: Throwable?
//                        ) {
//                            Log.d(this.javaClass.name, "Failed to publish message to topic")
//                        }
//                    })
//            } else {
//                Log.d(this.javaClass.name, "Impossible to publish, no server connected")
//            }
//        }

        view.findViewById<Button>(R.id.button_subscribe).setOnClickListener {

            if (mqttClient.isConnected()) {
                mqttClient.subscribe(topic,
                    1,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            val msg = "Subscribed to: $topic"
                            Log.d(this.javaClass.name, msg)

                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {
                            Log.d(this.javaClass.name, "Failed to subscribe: $topic")
                        }
                    })
            } else {
                Log.d(this.javaClass.name, "Impossible to subscribe, no server connected")
            }
        }

        view.findViewById<Button>(R.id.button_unsubscribe).setOnClickListener {

            if (mqttClient.isConnected()) {
                mqttClient.unsubscribe(topic,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            val msg = "Unsubscribed to: $topic"
                            Log.d(this.javaClass.name, msg)

                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {
                            Log.d(this.javaClass.name, "Failed to unsubscribe: $topic")
                        }
                    })
            } else {
                Log.d(this.javaClass.name, "Impossible to unsubscribe, no server connected")
            }
        }
    }
}