package com.example.supabasetesting

import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import io.github.jan.supabase.postgrest.from
import android.util.Log

class MainActivity : ComponentActivity() {
    private lateinit var resultTextView: TextView
    private lateinit var submitButton: Button
    private lateinit var inputField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get references to views
        submitButton = findViewById(R.id.submitButton)
        inputField = findViewById(R.id.inputField)
        resultTextView = findViewById(R.id.resultTextView)

        // Button click listener to send data to Supabase
        submitButton.setOnClickListener {
            val userInput = inputField.text.toString()
            val deviceId = getCustomDeviceId()
            sendDataToSupabase(userInput, deviceId)
        }
    }

    private fun getCustomDeviceId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun sendDataToSupabase(userInput: String, deviceId: String) {
        val mysupabaseClient = createSupabaseClient(
            supabaseUrl = "https://fkxnuszsjogobyqmqqdr.supabase.co/",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZreG51c3pzam9nb2J5cW1xcWRyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjYzOTYwMTAsImV4cCI6MjA0MTk3MjAxMH0.oiuARIE_fpD8pNMTCLKSgb_8f7QL67KcP0DIgd8kUuc"
        ){}
        val data = mapOf("roll_number" to userInput, "Device_ID" to deviceId)

        lifecycleScope.launch {
            try {
                val result = mysupabaseClient.from("prillTest").insert(data)
                Log.d("Supabase", "Insert successful: $result")
            } catch (e: Exception) {
                Log.e("Supabase", "Insert failed: ${e.message}")
            }
        }
    }
}
