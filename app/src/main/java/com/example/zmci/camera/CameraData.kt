package com.example.zmci.camera

data class CameraData(
    var id: Int = 0,
    var cameraName: String = "",
    var MQTT_SERVER_URI: String = "",
    var MQTT_USERNAME:String = "",
    var MQTT_PWD:String = "",
    var MQTT_TOPIC:String = "",
    var MQTT_CLIENT_ID:String = "",
)