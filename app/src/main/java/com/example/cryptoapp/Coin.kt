package com.example.cryptoapp

data class Coin(val coinName: String) {
    val name = coinName
    val image = name.lowercase()
}