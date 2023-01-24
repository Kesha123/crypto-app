package com.example.cryptoapp

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.cryptoapp.coin.CoinDataList
import com.example.cryptoapp.kucoin.KucoinViewModel
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class CoinListFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var coinAdapter: CoinAdapter
    lateinit var coinList: ArrayList<Coin>
    lateinit var kucoinViewModel: KucoinViewModel
    lateinit var viewOfLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)

        kucoinViewModel = ViewModelProvider(this)[KucoinViewModel::class.java]
        register(kucoinViewModel)
        loadCoins(kucoinViewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout = inflater.inflate(R.layout.fragment_coin_list, container, false)
        return viewOfLayout
    }

    private fun register(viewModel: KucoinViewModel) {
        val queue = Volley.newRequestQueue(context)
        val url = "https://api.kucoin.com/api/v1/bullet-public"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, JSONObject(),
            { response ->
                val data = response.getJSONObject("data")
                val intsances = data.getJSONArray("instanceServers")[0] as JSONObject

                viewModel.registrationData.token = data.get("token") as String
                viewModel.registrationData.endpoint = intsances.get("endpoint") as String
                viewModel.registrationData.pingInterval = (intsances.get("pingInterval") as Int).toLong()
                viewModel.registrationData.pingTimeout = (intsances.get("pingTimeout") as Int).toLong()
            },
            { error ->
                Log.e("KucoinApi", error.toString())
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun loadCoins(viewModel: KucoinViewModel) {

        val queue = Volley.newRequestQueue(context)
        val url = "https://api.kucoin.com/api/v1/symbols"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val data = response.getJSONArray("data")
                coinList = ArrayList()

                for (i in 0 until data.length()) {
                    val coin = data.getJSONObject(i)
                    val coinName = coin.getString("symbol").split("-")[0]
                    if (!coinList.contains(Coin(coinName)) && CoinDataList().list.contains(coinName.toLowerCase())) coinList.add(Coin(coinName))
                }

                coinAdapter = CoinAdapter(coinList, viewModel)

                recyclerView = viewOfLayout.findViewById(R.id.currency_recycler_view)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = coinAdapter
            },
            { error ->
                Log.e("CoinLoad", error.message.toString())
            }
        )
        queue.add(jsonObjectRequest)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu.findItem(R.id.action_search_coin)

        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText?.isNotEmpty() == true) filter(newText)
                    else loadCoins(kucoinViewModel)
                    return false
                }
            }
        )

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun filter(text: String?) {
        val filteredList: ArrayList<Coin> = ArrayList()
        for (item in coinList) {
            if (text?.lowercase(Locale.ROOT)?.let { item.name.lowercase(Locale.ROOT).contains(it) } == true) {
                filteredList.add(item)
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(context, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            coinAdapter.filterList(filteredList)
        }
    }

}