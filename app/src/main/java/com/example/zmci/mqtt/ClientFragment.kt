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
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zmci.MainActivity
import com.example.zmci.R
import com.example.zmci.database.DatabaseHelper
import com.example.zmci.mqtt.model.Detection
import kotlinx.android.synthetic.main.fragment_client.*
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class ClientFragment : Fragment() {
    private lateinit var mqttClient: MQTTClient

    companion object {
        lateinit var databaseHelper: DatabaseHelper
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseHelper = DatabaseHelper(this.requireContext())
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
    val CHANNEL_ID2= "channelID2"
    val CHANNEL_NAME = "channelName"
    val CHANNEL_NAME2= "channelName2"
    val NOTIFICATION_ID = 1
    val NOTIFICATION_ID2= 2
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
    private fun createNotificationChannel2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID2, CHANNEL_NAME2,
                NotificationManager.IMPORTANCE_HIGH
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
        val topic = arguments?.getString(MQTT_TOPIC_KEY).toString()

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
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun messageArrived(topic: String?, message: MqttMessage?) {
                            val msg = "Receive message: ${message.toString()} from topic: $topic"
                            Log.d(this.javaClass.name, msg)

//                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                            try {
                                val jsonData = "[ ${message.toString()} ]"
                                val obj = JSONArray(jsonData)
                                val imageObj: JSONObject = obj.getJSONObject(0)

                                // get image in json
                                val imageData = imageObj.getString("image") // get image
                                // get violators in json
                                val violatorsData = imageObj.getString("violators")
                                //get camera in json
                                val cameraData = imageObj.getString("camera")
                                //get timestamp in json
                                val timestampData = imageObj.getString("timestamp")
                                //total violations
                                val totalViolations = imageObj.getString("total_violations")
                                //total violators
                                val totalViolators = imageObj.getString("total_violators")

                                //violatorsData parse
                                val violatorsObject = JSONArray(violatorsData)
                                //cameraData parse
                                val cameraDataObject = JSONArray("[ $cameraData ]")

                                val curTime = LocalDateTime.now()

                                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                val ts = curTime.format(formatter)
                                try {

                                    //clear text after every loop
                                    tvTimestamp.text = "Timestamp: "
                                    textCameraData.text = "Camera Data: \n"
                                    textTotalViolators.text = "Total Violators: "
                                    textTotalViolations.text = "Total Violations: "
                                    textDetect.text = "Details: \n"

                                    //violators extract
                                    for (i in 0 until violatorsObject.length()) {
                                        val item = violatorsObject.getJSONObject(i)
//                                        val itemId = item.getString("id")
//                                        textDetect.append("ID: $itemId\n")
                                        /**********/
                                        val itemPersonInfo = item.getString("person_info")
                                        // extract itemPersonInfo
                                        val itemPersonInfoObject = JSONArray(itemPersonInfo)
                                        textDetect.append("Person in frame: \n")

                                        for (j in 0 until itemPersonInfoObject.length()) {
                                            val itemPI = itemPersonInfoObject.getJSONObject(j)
                                            val personId = itemPI.getString("person_id")
                                            val firstName = itemPI.getString("first_name")
                                            val middleName = itemPI.getString("middle_name")
                                            val lastName = itemPI.getString("last_name")
                                            val jobTitle = itemPI.getString("job_title")
                                            val overlaps = itemPI.getString("overlaps")
                                            textDetect.append(
                                                "ID: $personId\n" +
                                                        "Name: $firstName $middleName $lastName\n" +
                                                        "Job Title: $jobTitle\n" +
                                                        "Overlaps: $overlaps\n" +
                                                        "-----\n"
                                            )
                                        }

                                        /**********/
                                        val itemViolations = item.getString("violations")
                                        val itemViolationsObject = JSONArray(itemViolations)
                                        textDetect.append("\nViolations: \n")

                                        for (k in 0 until itemViolationsObject.length()) {
                                            val itemV = itemViolationsObject.getJSONObject(k)
                                            val className = itemV.getString("class_name")
                                            textDetect.append("$className\n")
                                        }
                                        textDetect.append("-----\n")
                                        /**********/

                                    }
                                    //cameraData extract
                                    for (cd in 0 until cameraDataObject.length()) {
                                        val itemCD = cameraDataObject.getJSONObject(cd)
                                        val camName = itemCD.getString("name")
                                        val camDesc = itemCD.getString("description")
                                        val camIP = itemCD.getString("ip_address")
                                        textCameraData.append("Camera Name: $camName\n" +
                                                    "$camDesc\n" +
                                                    "IP: $camIP")
                                    }

                                    //set violators in textview
//                                    textDetect.append(violatorsData)
                                    //set camera details in textview
//                                    textCameraData.append(cameraData)
                                    //set timestamp in textview
                                    tvTimestamp.append(ts)
                                    //set total violators in textview
                                    textTotalViolators.append(totalViolators)
                                    //set total violations in textview
                                    textTotalViolations.append(totalViolations)

                                    //decode base64 to image
                                    val decodedByte = Base64.decode(imageData, Base64.DEFAULT)
                                    val bitmap =
                                        BitmapFactory.decodeByteArray(
                                            decodedByte,
                                            0,
                                            decodedByte.size
                                        )
                                    //set image in imageview
                                    imgDetect.setImageBitmap(bitmap)

                                } catch (e : Exception) {
                                    e.printStackTrace()
                                }

                                //Save data to database
                                val detectionDB = Detection()
                                detectionDB.image = imageData
                                detectionDB.camera = cameraData
                                detectionDB.timestamp = ts
                                detectionDB.violators = violatorsData
                                detectionDB.total_violations = totalViolations
                                detectionDB.total_violators = totalViolators
                                databaseHelper.addDetection(context, detectionDB)

                            } catch (e : Exception) {
                                e.printStackTrace()
                            }

                            //notification
                            Thread {
                                try {
                                    createNotificationChannel2()
                                    val intentNotify = Intent(context, MainActivity::class.java)
                                    val pendingIntent = TaskStackBuilder.create(context).run {
                                        addNextIntentWithParentStack(intentNotify)
                                        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                                    }
                                    val notification =
                                        NotificationCompat.Builder(requireContext(), CHANNEL_ID2)
                                            .setContentTitle("Detection alert")
                                            .setContentText("Review logs for violation details")
                                            .setSmallIcon(R.drawable.ic_detect)
                                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                                            .setContentIntent(pendingIntent)
                                            .build()
                                    val notificationManager =
                                        NotificationManagerCompat.from(requireContext())
                                    notificationManager.notify(NOTIFICATION_ID2, notification)
                                } catch (e: Exception){
                                    e.printStackTrace()
                                }
                            }.start()


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

    }
}