package com.example.weather.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weather.databinding.ActivityMainBinding
import org.json.JSONObject

const val API_KEY = "cac864b716ec4702a1f174256220107"

class MainActivity : AppCompatActivity() {

    private lateinit var bn: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bn = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bn.root)

        bn.apply {

        }

    }

    private fun getResult(name: String) {
        val url = "https://api.weatherapi.com/v1/current.json" +
                "?key=$API_KEY&q=$name&aqi=no"
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET,
            url,
            { response ->
                val obj = JSONObject(response)
                val temp = obj.getJSONObject("current")
                Log.d("MyLog", "Volley error : ${temp.getString("temp_c")}")
            },
            {
                Log.d("MyLog", "Volley error : $it")
            }
        )
        queue.add(stringRequest)

    }
}