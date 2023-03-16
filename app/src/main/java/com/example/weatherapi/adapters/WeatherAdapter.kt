package com.example.weatherapi.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapi.R
import com.example.weatherapi.databinding.ListItemBinding
import com.squareup.picasso.Picasso

class WeatherAdapter(private val listener: Listener?) :
    ListAdapter<WeatherModel, WeatherAdapter.WeatherHolder>(Comparator()) {

    class WeatherHolder(item: View, private val listener: Listener?) :
        RecyclerView.ViewHolder(item) {
        private val binding = ListItemBinding.bind(item)
        var weatherModel: WeatherModel? = null

        init {
            itemView.setOnClickListener {
                weatherModel?.let { it1 -> listener?.onClick(it1) }
            }
        }

        fun bind(item: WeatherModel) = with(binding) {
            weatherModel = item.apply {
                tvDate.text = time
                tvCurrentTemp.text = currentTemp.ifEmpty {
                    "${item.maxTemp}°C/${item.minTemp}°C"
                }
                tvCondition.text = condition
                Picasso.get().load(imageUrl).into(imgWeather)
            }
        }
    }

    class Comparator : DiffUtil.ItemCallback<WeatherModel>() {
        override fun areItemsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return WeatherHolder(view, listener)
    }

    override fun onBindViewHolder(holder: WeatherHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface Listener {
        fun onClick(weatherModel: WeatherModel)
    }
}

