package com.example.weather.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weather.R
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.ui.main.MainFragment
import org.json.JSONObject

const val API_KEY = "cac864b716ec4702a1f174256220107"

class MainActivity : AppCompatActivity() {

    private lateinit var bn: ActivityMainBinding
    private val navController: NavController by lazy(LazyThreadSafetyMode.NONE) { NavHostFragment.findNavController(MainFragment()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bn = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bn.root)


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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.daysFragment-> {
                navController.navigate(R.id.daysFragment)
            }
            R.id.hoursFragment2 ->{
                navController.navigate(R.id.hoursFragment2)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}