package com.example.rhythmspot

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NetworkMonitor(context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOffline = MutableStateFlow(!isNetworkAvailable(context))
    val isOffline: StateFlow<Boolean> = _isOffline

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d("NetworkMonitor", "Network Available")
            _isOffline.value = false
        }

        override fun onLost(network: Network) {
            Log.d("NetworkMonitor", "Network Lost")
            _isOffline.value = true
        }
    }

    fun startNetworkCallback() {
        val networkRequest = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        Log.d("NetworkMonitor", "Network callback registered")
    }

    fun stopNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        Log.d("NetworkMonitor", "Network callback unregistered")
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}