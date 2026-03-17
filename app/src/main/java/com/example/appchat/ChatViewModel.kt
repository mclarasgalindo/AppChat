package com.example.appchat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class ChatViewModel : ViewModel() {
    private var socket: Socket = IO.socket("http://10.111.9.8:3000") // IP do emulador para o localhost

    // Lista de mensagens observável pelo Compose
    var messages = mutableStateListOf<Pair<String, String>>()
        private set

    init {
        socket.connect()
        socket.on("receive_message") { args ->
            val data = args[0] as JSONObject
            // Atualiza a UI na Thread Principal
            messages.add(data.getString("author") to data.getString("message"))
        }
    }

    fun sendMessage(text: String) {
        val json = JSONObject().apply {
            put("author", "Eu")
            put("message", text)
        }
        socket.emit("send_message", json)
        //messages.add("Eu" to text) // Adiciona a própria mensagem na lista
    }

    override fun onCleared() {
        socket.disconnect()
        super.onCleared()
    }
}