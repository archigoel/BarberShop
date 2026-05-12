package com.example.barbershop

class Utils {

    fun timeToMinutes(timeStr: String): Int {
        val parts = timeStr.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        return (hours * 60) + minutes
    }
}