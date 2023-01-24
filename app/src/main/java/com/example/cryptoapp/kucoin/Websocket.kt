package com.example.cryptoapp.kucoin

import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.cryptoapp.R
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Runnable
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.random.Random

class Websocket(private val coinName: String, var coinRate: TextView, var arrowDown: ImageView, var arrowUp: ImageView, val context: Context) {

    lateinit var webSocket: WebSocket
    var connectId by Delegates.notNull<Int>()
    var lastPrice: Double = 0.0

    fun runListening(registrationData: RegistrationData) {

        connectId = Random.nextInt(1,999999)
        val token = registrationData.token
        val endpoint = registrationData.endpoint
        val url = "$endpoint?token=$token&connectId=$connectId".replace("\"", "")
        val pingInterval = registrationData.pingInterval

        val client = OkHttpClient.Builder()
            .pingInterval(pingInterval, TimeUnit.MILLISECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url(url)
            .build()
        val wsListener = Listener()
        webSocket = client.newWebSocket(request, wsListener)
    }

    inner class Listener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            println(response)
            webSocket.send("{\"id\": $connectId, \"type\": \"subscribe\", \"topic\": \"/market/ticker:${coinName}-USDT\", \"response\": \"true\"}")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (JsonParser().parse(text).asJsonObject.get("type").asString.equals("message")) {
                val currencyData = getCurrencyData(text)
                coinRate.post(Runnable() {
                    coinRate.text = currencyData.price.toString().plus("$")
                })
                if (currencyData.price > lastPrice) {
                    arrowUp.setColorFilter(ContextCompat.getColor(context, R.color.green))
                    arrowDown.setColorFilter(ContextCompat.getColor(context, R.color.grey))
                }
                else if (currencyData.price < lastPrice) {
                    arrowDown.setColorFilter(ContextCompat.getColor(context, R.color.red))
                    arrowUp.setColorFilter(ContextCompat.getColor(context, R.color.grey))
                }
                lastPrice = currencyData.price
            }

        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            output(bytes.hex())
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            output(t.message.toString())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null)
        }

        private fun output(message: String) {
            Log.d("CoinData", message)
        }
    }

    fun unsubscribe() {
        webSocket.send("{\"id\": $connectId, \"type\": \"unsubscribe\", \"topic\": \"/market/ticker:${coinName}-USDT\", \"response\": \"false\"}")
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
    }

    fun getCurrencyData(message: String) : CurrencyData {
        val data = JsonParser().parse(message).asJsonObject.get("data").asJsonObject
        return CurrencyData(data)
    }

    data class CurrencyData(val json: JsonObject) {
        val price = json.get("price").asDouble
        val time = json.get("time").asLong
    }

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }
}































