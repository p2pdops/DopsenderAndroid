package p2pdops.dopsender.local_connection

import android.os.Handler
import android.util.Log
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class GroupOwnerSocketThread(private val handler: Handler) : Thread() {
    private var serverSocket: ServerSocket? = null

    var ipAddress: InetAddress? = null

    fun getPeerIpAddress(): InetAddress? {
        return ipAddress
    }

    companion object {
        private const val TAG = "GroupOwnerSocketHandler"
    }

    private val pool =
        ThreadPoolExecutor(
            WConfiguration.MAX_THREAD_COUNT,
            WConfiguration.MAX_THREAD_COUNT,
            WConfiguration.MAX_THREAD_POOL_EXECUTOR_KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            LinkedBlockingQueue()
        )

    init {
        try {
            serverSocket = ServerSocket(WConfiguration.GROUP_OWNER_PORT)
            Log.d(TAG, "init:")
        } catch (e: IOException) {
            Log.e(TAG, "IOException during open ServerSockets with port 4545", e)
            pool.shutdownNow()
            throw e
        }
    }

    override fun run() {
        Log.d(TAG, "run: called")
        while (true) {
            try {
                // Blocking operation : Initiate a MessagesRunnable instance when there is a new connection
                if (serverSocket != null && !serverSocket!!.isClosed) {
                    val clientSocket = serverSocket!!.accept()
                    pool.execute(MessagesRunnable(clientSocket, handler))
                    ipAddress = clientSocket.inetAddress
                    Log.d(TAG, "run: $ipAddress - clientSocket connected")
                }
            } catch (e: IOException) {
                try {
                    if (serverSocket != null && !serverSocket!!.isClosed) {
                        serverSocket!!.close()
                    }
                } catch (ioe: IOException) {
                    Log.e(TAG, "Error closing Socket", ioe)
                }
                pool.shutdownNow()
                break
            }
        }
    }

    fun closeSocketAndKillThisThread() {
        if (serverSocket != null && !serverSocket!!.isClosed) {
            try {
                serverSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "closeSocketAndKillThisThread: close socket error:", e)
            }
            pool.shutdown()
        }
    }


}
