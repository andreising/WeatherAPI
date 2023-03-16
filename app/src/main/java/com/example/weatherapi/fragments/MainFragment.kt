package com.example.weatherapi.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapi.MainViewModel
import com.example.weatherapi.adapters.ViewPager2Adapter
import com.example.weatherapi.adapters.WeatherModel
import com.example.weatherapi.databinding.FragmentMainBinding
import com.example.weatherapi.helpers.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject


class MainFragment : Fragment() {
    private lateinit var fLocationClient: FusedLocationProviderClient
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ViewPager2Adapter
    private val model: MainViewModel by activityViewModels()
    private val listFragment = listOf(HoursFragment.newInstance(), DaysFragment.newInstance())
    private val listTitles = listOf("Hours", "Days")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        checkPermission()
        updateCurrentCard()
        getLocation()
    }


    // get data from api by city name or coordinates and parsing it
    private fun requestWeatherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=$BASE_URL+" +
                "&q=$city&days=3&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET, url, { response ->
                parseData(JSONObject(response))
            }, { _ ->
                Toast.makeText(
                    activity as AppCompatActivity,
                    "I couldn't find it =(",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
        queue.add(request)
    }

    // takes array with next days weather and current day weather and put it in ViewModel
    private fun parseData(obj: JSONObject) {
        val nextDays = parseDaysArrays(obj)
        model.liveDataList.value = nextDays
        val currentDay = parseCurrentData(obj, nextDays[0])
        model.liveDataCurrent.value = currentDay
    }

    //transform JSON to our WeatherModel
    private fun parseCurrentData(obj: JSONObject, weatherModel: WeatherModel): WeatherModel {
        val current = obj.getJSONObject(CURRENT)
        val condition = current.getJSONObject(CONDITION)
        return WeatherModel(
            weatherModel.city,
            current.getString(LAST_UPDATED),
            condition.getString(TEXT),
            current.getString(TEMP_C),
            weatherModel.maxTemp,
            weatherModel.minTemp,
            "https:" + condition.getString(ICON),
            weatherModel.hours
        )
    }

    //transform JSON to ArrayList<WeatherModel>
    private fun parseDaysArrays(obj: JSONObject): ArrayList<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val array = obj.getJSONObject(FORECAST).getJSONArray(FORECASTDAY)
        val cityName = obj.getJSONObject(LOCATION).getString(NAME)
        for (i in 0 until array.length()) {
            val item = array[i] as JSONObject
            val day = item.getJSONObject(DAY)
            val condition = day.getJSONObject(CONDITION)
            val weatherModel = WeatherModel(
                cityName,
                item.getString(DATE),
                condition.getString(TEXT),
                "",
                day.getString(MAXTEMP_C).toFloat().toInt().toString(),
                day.getString(MINTEMP_C).toFloat().toInt().toString(),
                "https:" + condition.getString(ICON),
                item.getJSONArray(HOUR).toString()
            )
            list.add(weatherModel)
        }
        return list
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun init() = with(binding) {
        tabLayoutInit()
        clickerInit()
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

    }

    private fun tabLayoutInit() = with(binding) {
        adapter = ViewPager2Adapter(activity as AppCompatActivity, listFragment)
        vp2.adapter = adapter
        TabLayoutMediator(tabLayout, vp2) { tab, position ->
            tab.text = listTitles[position]
        }.attach()
    }

    private fun clickerInit() = with(binding) {
        imgBtnRefresh.setOnClickListener {
            tabLayout.selectTab(tabLayout.getTabAt(0))
            getLocation()
        }
        imgBtnSearch.setOnClickListener {
            DialogManager.searchByName(requireContext(), object : DialogManager.Listener {
                override fun onClick(name: String?) {
                    name?.let { it1 -> requestWeatherData(it1) }
                }

            })
        }
    }

    //check location access and ,if success, use it to take a weather by coordinates
    private fun getLocation() {
        val cancellationToken = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
            .addOnCompleteListener {
                requestWeatherData("${it.result.latitude}, ${it.result.longitude}")
            }
    }

    //check if location is enabled - get a data by it, else start DialogManager
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

    //check location enable
    private fun isLocationEnabled(): Boolean {
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    //start
    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
        }
    }

    //binding main card
    private fun updateCurrentCard() = with(binding) {
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            val maxMinTemp = "${it.maxTemp}°C/${it.minTemp}°C"
            tvData.text = it.time
            tvCity.text = it.city
            tvCurrentTemp.text = it.currentTemp.ifEmpty { maxMinTemp }
            tvWeather.text = it.condition
            tvTemp.text = if (it.currentTemp.isEmpty()) "" else maxMinTemp
            Picasso.get().load(it.imageUrl).into(imgWeather)
        }
    }

    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment
    }

}




