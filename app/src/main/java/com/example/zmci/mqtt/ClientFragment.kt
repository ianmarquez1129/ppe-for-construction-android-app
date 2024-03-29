package com.example.zmci.mqtt

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
import javax.net.ssl.SSLSocketFactory


class ClientFragment : Fragment() {
    private lateinit var mqttClient: MQTTClient

    companion object {
        lateinit var databaseHelper: DatabaseHelper
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the databaseHelper
        databaseHelper = DatabaseHelper(this.requireContext())
    }

    val CHANNEL_ID = "channelID"
    val CHANNEL_ID2= "channelID2"
    val CHANNEL_NAME = "channelName"
    val CHANNEL_NAME2= "channelName2"
    val NOTIFICATION_ID = 1
    val NOTIFICATION_ID2= 2
    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        createNotificationChannel()
        val intentNotify = Intent(context, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intentNotify)
            getPendingIntent(0, PendingIntent.FLAG_MUTABLE)
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
        val cameraName = arguments?.getString(CAMERA_NAME_KEY).toString()
        val serverURI = arguments?.getString(MQTT_SERVER_URI_KEY).toString()
        val clientId = arguments?.getString(MQTT_CLIENT_ID_KEY).toString()
        val username = arguments?.getString(MQTT_USERNAME_KEY).toString()
        val pwd = arguments?.getString(MQTT_PWD_KEY).toString()
        val topic = arguments?.getString(MQTT_TOPIC_KEY).toString()

        // Open MQTT Broker communication
        mqttClient = MQTTClient(requireContext(), serverURI, clientId)

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
                val options = MqttConnectOptions()
                val sslSocketFactory: SSLSocketFactory? = mqttClient.getSocketFactory(resources.openRawResource(R.raw.amazonrootca1),resources.openRawResource(R.raw.certificate_pem),resources.openRawResource(R.raw.private_pem),"")
                options.socketFactory = sslSocketFactory
                mqttClient.connect(options,
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

                            Toast.makeText(
                                context,
                                "MQTT Connection fails: ${exception.toString()}",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Go back to Connect Fragment
                            findNavController().navigate(R.id.action_ClientFragment_to_CameraFragment)
                        }
                    },
                    object : MqttCallback {
                        @SuppressLint("SetTextI18n", "MissingPermission")
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun messageArrived(topic: String?, message: MqttMessage?) {
                            val msg = "Receive message: ${message.toString()} from topic: $topic"
                            Log.d(this.javaClass.name, msg)

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
                                //val timestampData = imageObj.getString("timestamp")
                                //total violations
                                val totalViolations = imageObj.getString("total_violations")
                                //total violators
                                val totalViolators = imageObj.getString("total_violators")

                                //violatorsData parse
                                val violatorsObject = JSONArray(violatorsData)
                                //cameraData parse
                                val cameraDataObject = JSONArray("[ $cameraData ]")

                                // Process the received data time
                                val curTime = LocalDateTime.now()
                                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                val ts = curTime.format(formatter)
                                try {

                                    // Clear LinearLayout views for the new incoming data
                                    llCameraDetails.removeAllViews()
                                    llDetails.removeAllViews()
                                    llDetails2.removeAllViews()

                                    // Clear TextView after every loop and
                                    // setup a new one for new incoming data
                                    tvTimestamp.text = "Timestamp: $ts"
                                    textCameraData.text = "Camera details:"
                                    textTotalViolators.text = "Person: $totalViolators"
                                    textTotalViolations.text = "Detected PPE: $totalViolations"
                                    textDetect.text = "Details:"

                                    //cameraData extract
                                    for (cd in 0 until cameraDataObject.length()) {
                                        val itemCD = cameraDataObject.getJSONObject(cd)
                                        val camIP = itemCD.getString("ip_address")
                                        // Create a TextView for CameraName and IP address
                                        val tvCameraDetails = TextView(context)
                                        tvCameraDetails.layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                        tvCameraDetails.textSize = 20f
                                        tvCameraDetails.typeface = Typeface.DEFAULT_BOLD
                                        tvCameraDetails.text =
                                            "Camera Name: $cameraName\n" +
                                                    "IP: $camIP"
                                        // Display the TextView in LinearLayout view
                                        llCameraDetails.addView(tvCameraDetails)
                                    }

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

                                    //violators extract
                                    for (i in 0 until violatorsObject.length()) {
                                        val item = violatorsObject.getJSONObject(i)
                                        val itemPersonInfo = item.getString("person_info")
                                        // extract itemPersonInfo
                                        val itemPersonInfoObject = JSONArray(itemPersonInfo)

                                        // If the current person has no 'face' and is not recognized,
                                        // it is considered "Unknown person".
                                        if (itemPersonInfoObject.length() == 0) {
                                            // Create TextView for "Unknown person"
                                            val tvPerson = TextView(context)
                                            tvPerson.layoutParams = ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT
                                            )
                                            tvPerson.textSize = 20f
                                            tvPerson.typeface = Typeface.DEFAULT_BOLD
                                            tvPerson.text = "Unknown person"
                                            // Display the TextView in LinearLayout view
                                            if (i % 2 == 0) {
                                                llDetails.addView(tvPerson)
                                            } else {
                                                llDetails2.addView(tvPerson)
                                            }
                                        }

                                        // If the current person has a detected 'face'...
                                        for (j in 0 until itemPersonInfoObject.length()) {
                                            val itemLength = itemPersonInfoObject.getJSONObject(j).length()
                                            // If the person has been recognized via face recognition
                                            if (itemLength > 1) {
                                                val itemPI = itemPersonInfoObject.getJSONObject(j)
                                                val personId = itemPI.getString("person_id")
                                                val firstName = itemPI.getString("first_name")
                                                val middleName = itemPI.getString("middle_name")
                                                val lastName = itemPI.getString("last_name")
                                                val jobTitle = itemPI.getString("job_title")
                                                val overlaps = itemPI.getString("overlaps")
                                                val tvPersonInfo = TextView(context)
                                                tvPersonInfo.layoutParams = ViewGroup.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                                )
                                                tvPersonInfo.textSize = 20f
                                                tvPersonInfo.typeface = Typeface.DEFAULT_BOLD
                                                tvPersonInfo.text =
                                                    "ID: $personId\n" +
                                                            "Name: $firstName $middleName $lastName\n" +
                                                            "Job Title: $jobTitle\n" +
                                                            "Overlaps: $overlaps"
                                                if (i % 2 == 0) {
                                                    llDetails.addView(tvPersonInfo)
                                                } else {
                                                    llDetails2.addView(tvPersonInfo)
                                                }

                                            }
                                            // If the person is not recognized via 'face recognition'
                                            // the person is considered "Unknown"
                                            else {
                                                val itemPI = itemPersonInfoObject.getJSONObject(j)
                                                val overlaps = itemPI.getString("overlaps")
                                                // Create TextView for "Unknown person"
                                                val tvPersonUnknown = TextView(context)
                                                tvPersonUnknown.layoutParams =
                                                    ViewGroup.LayoutParams(
                                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                                    )
                                                tvPersonUnknown.textSize = 20f
                                                tvPersonUnknown.typeface = Typeface.DEFAULT_BOLD
                                                tvPersonUnknown.text =
                                                    "Unknown person\n" +
                                                            "Overlaps: $overlaps"
                                                // Display the TextView in LinearLayout view
                                                if (i % 2 == 0) {
                                                    llDetails.addView(tvPersonUnknown)
                                                } else {
                                                    llDetails2.addView(tvPersonUnknown)
                                                }

                                            }

                                        }

                                        // Set the person's ID, and detections
                                        val personUniqueID = item.getString("id")
                                        val itemViolations = item.getString("violations")
                                        val itemViolationsObject = JSONArray(itemViolations)
                                        val tvPersonID = TextView(context)
                                        // Create a TextView for person's ID and detections
                                        tvPersonID.layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                        tvPersonID.textSize = 20f
                                        tvPersonID.typeface = Typeface.DEFAULT_BOLD
                                        tvPersonID.text = "\nPerson ID: $personUniqueID \n\nDetections:"
                                        // Display the TextView in LinearLayout view
                                        if (i % 2 == 0) {
                                            llDetails.addView(tvPersonID)
                                        } else {
                                            llDetails2.addView(tvPersonID)
                                        }

                                        try {
                                            // Processing of detected PPE with their corresponding color coding
                                            for (k in 0 until itemViolationsObject.length()) {
                                                // Obtain the detections from JSON
                                                val itemV = itemViolationsObject.getJSONObject(k)
                                                val className = itemV.getString("class_name")
                                                // Create TextView for each detections
                                                val tvPPE = TextView(context)
                                                tvPPE.layoutParams = ViewGroup.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                                )
                                                tvPPE.textSize = 20f
                                                tvPPE.typeface = Typeface.DEFAULT_BOLD
                                                tvPPE.text = className
                                                // Setting the color coding of each PPE detections
                                                try {
                                                    when (className) {
                                                        "helmet" -> {
                                                            tvPPE.setTextColor(
                                                                ContextCompat.getColor(
                                                                    requireContext(),
                                                                    R.color.helmet
                                                                )
                                                            )
                                                        }

                                                        "no helmet" -> {
                                                            tvPPE.setTextColor(
                                                                ContextCompat.getColor(
                                                                    requireContext(),
                                                                    R.color.no_helmet
                                                                )
                                                            )
                                                        }

                                                        "glasses" -> {
                                                            tvPPE.setTextColor(
                                                                ContextCompat.getColor(
                                                                    requireContext(),
                                                                    R.color.glasses
                                                                )
                                                            )
                                                        }

                                                        "no glasses" -> {
                                                            tvPPE.setTextColor(
                                                                ContextCompat.getColor(
                                                                    requireContext(),
                                                                    R.color.no_glasses
                                                                )
                                                            )
                                                        }

                                                        "vest" -> {
                                                            tvPPE.setTextColor(
                                                                ContextCompat.getColor(
                                                                    requireContext(),
                                                                    R.color.vest
                                                                )
                                                            )
                                                        }

                                                        "no vest" -> {
                                                            tvPPE.setTextColor(
                                                                ContextCompat.getColor(
                                                                    requireContext(),
                                                                    R.color.no_vest
                                                                )
                                                            )
                                                        }

                                                        "gloves" -> {
                                                            tvPPE.setTextColor(
                                                                ContextCompat.getColor(
                                                                    requireContext(),
                                                                    R.color.gloves
                                                                )
                                                            )
                                                        }

                                                        "no gloves" -> {
                                                            tvPPE.setTextColor(
                                                                ContextCompat.getColor(
                                                                    requireContext(),
                                                                    R.color.no_gloves
                                                                )
                                                            )
                                                        }

                                                        "boots" -> {
                                                            tvPPE.setTextColor(
                                                                ContextCompat.getColor(
                                                                    requireContext(),
                                                                    R.color.boots
                                                                )
                                                            )
                                                        }

                                                        "no boots" -> {
                                                            tvPPE.setTextColor(
                                                                ContextCompat.getColor(
                                                                    requireContext(),
                                                                    R.color.no_boots
                                                                )
                                                            )
                                                        }
                                                    }
                                                    // Display the TextView in LinearLayout view
                                                    if (i % 2 == 0) {
                                                        llDetails.addView(tvPPE)
                                                    } else {
                                                        llDetails2.addView(tvPPE)
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        // Horizontal line separator
                                        val tvLine = TextView(context)
                                        tvLine.layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            10
                                        )
                                        tvLine.setBackgroundColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.black
                                            ))
                                        // Display of Horizontal Line in LinearLayout view
                                        if (i % 2 == 0) {
                                            llDetails.addView(tvLine)
                                        } else {
                                            llDetails2.addView(tvLine)
                                        }
                                    }
                                } catch (e : Exception) {
                                    e.printStackTrace()
                                }
                                //Save data to database
                                val detectionDB = Detection()
                                detectionDB.image = imageData
                                detectionDB.cameraName = cameraName
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
                                        getPendingIntent(0, PendingIntent.FLAG_MUTABLE)
                                    }
                                    val notification =
                                        NotificationCompat.Builder(requireContext(), CHANNEL_ID2)
                                            .setContentTitle("Detection alert")
                                            .setContentText("Review logs for details")
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