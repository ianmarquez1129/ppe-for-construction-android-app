package com.example.zmci.mqtt.model

data class Detection (
    var image : String?,
    var camera : Camera?,
    var violators : Violators?,
    var timestamp : String?

)

data class Camera (
    var name : String?,
    var description : String?,
    var ip_address : String?

    )

data class Violators (
    var persons : Persons?,
    var violations : List<String>?

    )

data class Persons (
    var id : Int?,
    var person_id : Int?,
    var first_name : String?,
    var middle_name : String?,
    var last_name : String?,
    var job_title : String?
    )