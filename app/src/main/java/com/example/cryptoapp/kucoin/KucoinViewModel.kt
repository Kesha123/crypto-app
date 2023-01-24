package com.example.cryptoapp.kucoin

import androidx.lifecycle.ViewModel

class KucoinViewModel : ViewModel() {
    var registrationData = RegistrationData("", "", 0, 0)
}

data class RegistrationData(var token: String, var endpoint: String, var pingInterval: Long, var pingTimeout: Long)