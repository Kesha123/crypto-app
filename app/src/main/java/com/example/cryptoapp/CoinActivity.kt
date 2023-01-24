package com.example.cryptoapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cryptoapp.kucoin.RegistrationData
import com.example.cryptoapp.kucoin.Websocket


class CoinActivity : AppCompatActivity() {

    lateinit var webSocket: Websocket
    lateinit var bundle: Bundle
    lateinit var coinRate: TextView
    lateinit var arrowDown: ImageView
    lateinit var arrowUp: ImageView

    lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.currency_rate)
        bundle = intent.extras!!
        coinRate = findViewById(R.id.coin_rate_price)
        arrowDown = findViewById(R.id.arrow_down_rate)
        arrowUp = findViewById(R.id.arrow_up_rate)

        val token = bundle.getString("token") as String
        val endpoint = bundle.getString("endpoint") as String
        val pingInterval = bundle.getLong("pingInterval")
        val pingTimeout = bundle.getLong("pingTimeout")

        val registrationData = RegistrationData(token, endpoint, pingInterval, pingTimeout)

        loadCoin()

        webSocket = bundle.getString("coin")?.let { Websocket(it, coinRate, arrowDown, arrowUp, applicationContext) }!!
        webSocket.runListening(registrationData)
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket.unsubscribe()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadCoin() {
        val coin = bundle.getString("coin")
        val coinImage = findViewById<ImageView>(R.id.currency_image)
        try {
            val uri = "@drawable/${coin?.lowercase()}"
            val imageSource = resources.getIdentifier(uri, null, applicationContext.packageName)
            val res = resources.getDrawable(imageSource)
            coinImage.setImageDrawable(res)
        }
        catch (e: Exception) {
            coinImage.setImageResource(R.drawable.coin)
        }
    }

}