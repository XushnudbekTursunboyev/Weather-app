package com.example.weather.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.MenuItem.OnMenuItemClickListener
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.weather.DialogManager
import com.example.weather.MainViewModel
import com.example.weather.R
import com.example.weather.adapters.ViewPagerAdapter
import com.example.weather.databinding.FragmentMainBinding
import com.example.weather.model.WeatherModel
import com.example.weather.ui.days.DaysFragment
import com.example.weather.ui.hours.HoursFragment
import com.example.weather.utils.extensions.isPermissionGranted
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONObject


const val API_KEY = "cac864b716ec4702a1f174256220107"

class MainFragment : Fragment(), OnRefreshListener {
    private lateinit var fLocationClient: FusedLocationProviderClient

    private val fList = listOf(HoursFragment(), DaysFragment())
    private val tList = listOf("Hours", "Days")

    private val navController: NavController by lazy(LazyThreadSafetyMode.NONE) { NavHostFragment.findNavController(this) }
    private val model: MainViewModel by activityViewModels()
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var _bn: FragmentMainBinding
    private val bn get() = _bn


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bn = FragmentMainBinding.inflate(inflater, container, false)
        bn.constraint.setOnRefreshListener(this)
        setHasOptionsMenu(true)
        bn.toolbar.inflateMenu(R.menu.main_menu)
        bn.toolbar.title =" Wheather App"
        bn.toolbar.setOnMenuItemClickListener(object : OnMenuItemClickListener,
            Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(p0: MenuItem?): Boolean {
                when(p0?.itemId){
                    R.id.daysFragment-> {
                        navController.navigate(R.id.daysFragment)
                    }
                    R.id.hoursFragment2 ->{
                        navController.navigate(R.id.hoursFragment2)
                    }
                }

                return true
            }

        })

        return bn.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()

    }

    private fun setUpUI() {
        setHasOptionsMenu(true)
        checkPermission()
        updateCurrentCard()
        init()
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun init() = with(bn) {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = ViewPagerAdapter(activity as FragmentActivity, fList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp) { t, p ->
            t.text = tList[p]
        }.attach()
        imSync.setOnClickListener {
            tabLayout.selectTab(tabLayout.getTabAt(0))
            checkLocation()
        }
        imSearch.setOnClickListener {
            DialogManager.searchByNameDialog(requireContext(), object : DialogManager.Listener {
                override fun onClick(name: String?) {
                    Log.d("MyLog", "Name: $name")
                    name?.let { requestWeatherData(it) }
                }
            })
        }
    }

    private fun getLocation() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) checkPermission()
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener {
                requestWeatherData("${it.result.latitude},${it.result.longitude}")
            }

    }

    private fun checkLocation() {
        if (isLocationEnabled()) {
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener {
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }

    }

    private fun isLocationEnabled(): Boolean {
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun updateCurrentCard() = with(bn) {
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            val maxMin = "${it.minTemp}°C/${it.maxTemp}°C"
            tvCity.text = it.city
            tvDate.text = it.time
            tvCondition.text = it.condition
            tvCurrentTemp.text = it.currentTemp.ifEmpty { maxMin }
            tvMaxMin.text = if (it.currentTemp.isEmpty()) "" else maxMin
            Glide.with(requireActivity())
                .load("https:" + it.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(imWeather)
        }
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Toast.makeText(requireActivity(), "Permission is $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestWeatherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "&q=" +
                city +
                "&days=" +
                "10" +
                "&aqi=no&alerts=no\n"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url, { result ->
                parseWeatherData(result)
            },
            { error ->
                Log.d("MyLog", "Error: $error")
            }
        )
        queue.add(request)
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])
    }

    private fun parseDays(mainObject: JSONObject): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        model.liveDataList.value = list
        return list
    }

    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
        val item = WeatherModel(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("location").getString("localtime"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),
            weatherItem.hours
        )

        model.liveDataCurrent.value = item
    }

    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onRefresh() {
        bn.tabLayout.selectTab(bn.tabLayout.getTabAt(0))
        checkLocation()
        bn.constraint.isRefreshing = false
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
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
