package com.linku.im.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkConnectivityObserver(
    context: Context
) : ConnectivityObserver {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<ConnectivityObserver.State> = channelFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(ConnectivityObserver.State.Available)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                trySend(ConnectivityObserver.State.Losing)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(ConnectivityObserver.State.Lost)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                trySend(ConnectivityObserver.State.Unavailable)
            }
        }
        connectivityManager.registerNetworkCallback(
            RequestFactory.wifiAndCellularRequest(),
            callback
        )
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}