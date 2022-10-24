package com.example.weather.ui.hours

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.weather.MainViewModel
import com.example.weather.databinding.FragmentHoursBinding
import com.example.weather.adapters.WeatherAdapter
import com.example.weather.model.WeatherModel
import com.google.gson.JsonArray
import org.json.JSONArray
import org.json.JSONObject


class HoursFragment : Fragment() {

    private lateinit var _bn: FragmentHoursBinding
    private val bn get() = _bn
    private val model: MainViewModel by activityViewModels()

    private lateinit var adapter: WeatherAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        model.liveDataCurrent.observe(viewLifecycleOwner){
            adapter.submitList(getHoursList(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bn = FragmentHoursBinding.inflate(inflater, container, false)
        return bn.root
    }


    private fun initRcView() = with(bn) {
        adapter = WeatherAdapter(null)
        rcView.adapter = adapter

    }

    private fun getHoursList(wItem: WeatherModel):List<WeatherModel>{
        val hoursArray = JSONArray(wItem.hours)
        val list = ArrayList<WeatherModel>()
        for (i in 0 until hoursArray.length()){
            val item =  WeatherModel(
                wItem.city,
                (hoursArray[i] as JSONObject).getString("time"),
                (hoursArray[i] as JSONObject).getJSONObject("condition").getString("text"),
                (hoursArray[i] as JSONObject).getString("temp_c"),
                "",
                "",
                (hoursArray[i] as JSONObject).getJSONObject("condition").getString("icon"),
                ""
                )
            list.add(item)
        }
        return list
    }

}