package com.example.cryptoapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cryptoapp.kucoin.KucoinViewModel


class CoinAdapter(private var coinsList: MutableList<Coin>, private val viewModel: KucoinViewModel) : RecyclerView.Adapter<CoinAdapter.ViewHolder>(){

    class ViewHolder(view: View, val context: Context) : RecyclerView.ViewHolder(view) {
        val currencyImage = view.findViewById<ImageView>(R.id.currency_image)
        val currencyName = view.findViewById<TextView>(R.id.currency_name)
        val container = view.findViewById<RelativeLayout>(R.id.coin_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.currency_item, parent,false)
        return ViewHolder(view, view.context)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val coin = coinsList[position]
        holder.currencyName.text = coin.name
        try {
            val uri = "@drawable/${coin.image}"
            val imageSource = holder.context.resources.getIdentifier(uri, null, holder.context.packageName)
            val res = holder.context.resources.getDrawable(imageSource)
            holder.currencyImage.setImageDrawable(res)
        }
        catch (e: Exception) {
            //holder.currencyImage.setImageResource(R.drawable.coin)
            coinsList.removeAt(position)
        }

        holder.container.setOnClickListener {
            val intent = Intent(holder.itemView.context, CoinActivity::class.java).apply {
                putExtra("coin", coin.name,)
                putExtra("token", viewModel.registrationData.token)
                putExtra("endpoint", viewModel.registrationData.endpoint)
                putExtra("pingInterval", viewModel.registrationData.pingInterval)
                putExtra("pingTimeout", viewModel.registrationData.pingTimeout)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return coinsList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterList: ArrayList<Coin>) {
        coinsList = filterList
        notifyDataSetChanged()
    }
}