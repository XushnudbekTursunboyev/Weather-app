package com.example.weather.ui.days

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.weather.MainViewModel
import com.example.weather.databinding.FragmentDaysBinding
import com.example.weather.adapters.WeatherAdapter
import com.example.weather.model.WeatherModel


class DaysFragment : Fragment() , WeatherAdapter.Listener{

    private lateinit var adapter: WeatherAdapter
    private lateinit var _bn: FragmentDaysBinding
    private val bn get() = _bn
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bn = FragmentDaysBinding.inflate(inflater, container, false)
        init()
        model.liveDataList.observe(viewLifecycleOwner) {
            adapter.submitList(it.subList(1, it.size))
        }
        return bn.root
    }

    private fun init() = with(bn) {
        adapter = WeatherAdapter(this@DaysFragment)
        rvDays.adapter = adapter
    }

    override fun onClick(item: WeatherModel) {
        model.liveDataCurrent.value = item
    }

}