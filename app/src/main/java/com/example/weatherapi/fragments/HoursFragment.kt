package com.example.weatherapi.fragments

import android.os.Binder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapi.MainViewModel
import com.example.weatherapi.R
import com.example.weatherapi.adapters.WeatherAdapter
import com.example.weatherapi.adapters.WeatherModel
import com.example.weatherapi.databinding.FragmentHoursBinding
import com.example.weatherapi.helpers.*
import org.json.JSONArray
import org.json.JSONObject

class HoursFragment : Fragment() {
    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WeatherAdapter
    private val model: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        updateData()
    }

    private fun init() {
        adapterInit()
    }

    private fun adapterInit() {
        adapter = WeatherAdapter(null)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter

    }

    private fun updateData() {
        model.liveDataCurrent.observe(viewLifecycleOwner){
            adapter.submitList(getHoursList(it))
        }
    }

    //parsing hours: String to List<WeatherModel>
    private fun getHoursList(weatherModel: WeatherModel): List<WeatherModel>{
        val array = JSONArray(weatherModel.hours)
        val list = emptyList<WeatherModel>().toMutableList()
        for (i in 0 until array.length()) {
            val obj = array[i] as JSONObject
            val item = WeatherModel(
                weatherModel.city,
                obj.getString(TIME),
                obj.getJSONObject(CONDITION).getString(TEXT),
                obj.getString(TEMP_C),
                "",
                "",
                "https:"+obj.getJSONObject(CONDITION).getString(ICON),
                ""
            )
            list.add(item)
        }
        return list
    }

    companion object {
        @JvmStatic
        fun newInstance() = HoursFragment()
    }
}