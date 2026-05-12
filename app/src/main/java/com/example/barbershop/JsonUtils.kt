package com.example.barbershop

import android.content.Context
import com.google.gson.Gson
import java.io.IOException

class JsonUtils {
    fun loadShopData(context: Context, fileName: String): Shop? {
        return try {
            // 1. Open the file from assets
            val jsonString = context.assets.open("simulation_input.json").bufferedReader().use { it.readText() }

            // 2. Convert JSON string to your Shop data class
            Gson().fromJson(jsonString, Shop::class.java)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            null
        }
    }


}
