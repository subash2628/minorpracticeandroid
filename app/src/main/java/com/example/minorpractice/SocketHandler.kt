package com.example.minorpractice

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
//import java.net.Socket
import java.net.URISyntaxException

class SocketHandler {
    companion object {

        lateinit var mSocket: Socket

        @Synchronized
        fun setSocket() {
            try {
// "http://10.0.2.2:3000" is the network your Android emulator must use to join the localhost network on your computer
// "http://localhost:3000/" will not work
    // locally -> http://192.168.10.103:5000
// If you want to use your physical phone you could use the your ip address plus :3000
// This will allow your Android Emulator and physical device at your home to connect to the server
                mSocket = IO.socket("https://minorpractice.herokuapp.com")
                Log.d("socket : ","connection ok")
            } catch (e: URISyntaxException) {
                Log.d("socket : ","connection failed");
            }
        }

        @Synchronized
        fun getSocket(): Socket {
            return mSocket
        }

        @Synchronized
        fun establishConnection() {
            mSocket.connect()
        }

        @Synchronized
        fun closeConnection() {
            mSocket.disconnect()
        }
    }
}