package com.example.formniganidemo.attendee

data class EventDetailData(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val datetime: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val organizer: String = "",
    val price: String = "",
    val category: String = "",
    val imageUri: String = ""
)