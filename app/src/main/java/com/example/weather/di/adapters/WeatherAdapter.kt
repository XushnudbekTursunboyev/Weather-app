package com.example.weather.di.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.R
import com.example.weather.databinding.ListItemBinding
import com.example.weather.di.model.WeatherModel

class WeatherAdapter(val listener: Listener?) : ListAdapter<WeatherModel, WeatherAdapter.Holder>(Comparator()) {

    class Holder(view: View, val listener: Listener?) : RecyclerView.ViewHolder(view) {
        val bn = ListItemBinding.bind(view)
        var itemTemp:WeatherModel? =null
        init {
                itemView.setOnClickListener {
                    listener?.let {
                        itemTemp?.let { it1 -> listener.onClick(it1) }
                    }
                }
        }

        fun onBind(weatherModel: WeatherModel) = with(bn) {
            itemTemp = weatherModel
            tvDate.text = weatherModel.time
            tvCondition.text = weatherModel.condition
            tvTemp.text = weatherModel.currentTemp.ifEmpty { "${weatherModel.maxTemp}°C / ${weatherModel.minTemp}°C" }

            Glide
                .with(itemView.context)
                .load("https:" + weatherModel.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(im)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return Holder(view, listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.onBind(getItem(position))
    }

    interface Listener{
        fun onClick(item:WeatherModel)
    }
}