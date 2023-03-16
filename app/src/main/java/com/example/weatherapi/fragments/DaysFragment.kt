package com.example.weatherapi.fragments

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
import com.example.weatherapi.databinding.FragmentDaysBinding

class DaysFragment : Fragment(), WeatherAdapter.Listener {
    private lateinit var binding: FragmentDaysBinding
    private lateinit var adapter: WeatherAdapter
    private val model: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        getData()
    }

    private fun init() {
        adapterInit()
    }
    private fun getData(){
        model.liveDataList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
    private fun adapterInit() = with(binding) {
        adapter = WeatherAdapter(this@DaysFragment)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
    }
    companion object {
        @JvmStatic
        fun newInstance() = DaysFragment()
    }

    override fun onClick(weatherModel: WeatherModel) {
        model.liveDataCurrent.value = weatherModel
    }
}