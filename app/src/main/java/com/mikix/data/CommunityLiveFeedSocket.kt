package com.mikix.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityLiveFeedSocket @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private var socket: WebSocket? = null
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val events: SharedFlow<String> = _events

    fun connect(accessToken: String) {
        socket?.close(1000, "reconnect")
        val request = Request.Builder()
            .url("wss://api.mikix.app/v1/community/live")
            .header("Authorization", "Bearer $accessToken")
            .build()
        socket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                _events.tryEmit(text)
            }
        })
    }

    fun disconnect() {
        socket?.close(1000, "done")
        socket = null
    }
}
