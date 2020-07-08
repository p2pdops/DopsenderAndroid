package p2pdops.dopsender.local_connection

import android.os.Handler
import android.util.Log
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class ClientSocketThread(
    val handler: Handler,
    val groupOwnerAddress: InetAddress
) : Thread() {


    companion object {
        private const val TAG = "ClientSocketManager"
        private var socket: Socket? = null
    }


    override fun run() {
        socket = Socket()

        try {
            socket!!.connect(
                InetSocketAddress(groupOwnerAddress.hostAddress, WConfiguration.GROUP_OWNER_PORT),
                WConfiguration.CLIENT_PORT
            )

            Log.d(TAG, "Socket Connected : ${socket?.isBound}")

            Thread(MessagesRunnable(socket!!, handler)).start()

        } catch (e: IOException) {
            Log.e(TAG, "run: error:", e)
            try {
                socket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "run: error closing socket:", e)
            }
        }
    }

    fun closeSocketAndKillThisThread() {
        if (socket != null && !socket!!.isClosed) {
            try {
                socket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "closeSocketAndKillThisThread: error closing socket:", e)
            }
        }

        if (!isInterrupted) {
            Log.d(TAG, "closeSocketAndKillThisThread: closing ClientSocketManager instance")
            interrupt()
        }
    }
}