package p2pdops.dopsender.local_connection

import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket


class MessagesRunnable(private val socket: Socket, private val handler: Handler) : Runnable {

    companion object {
        private const val TAG = "MessagesRunnable"
        private var disabled = false
        private var inputStream: InputStream? = null
        private var outputStream: OutputStream? = null
    }

    override fun run() {
        Log.d(TAG, "run: started")
        try {
            inputStream = socket.getInputStream()
            outputStream = socket.getOutputStream()

            handler.obtainMessage(WConfiguration.SOCKETS_CONNECTED, this).sendToTarget()

            val buffer = ByteArray(1024)
            var bufferLength: Int

            while (!disabled) {

                if (inputStream != null) {
                    try {

                        bufferLength = inputStream!!.read(buffer)

                        if (bufferLength == -1) break

                        //HANDLE_OBTAINED_MESSAGE
                        handler.obtainMessage(
                            WConfiguration.HANDLE_RECEIVED_MESSAGE,
                            bufferLength,
                            -1,
                            buffer
                        ).sendToTarget()

                    } catch (e: IOException) {
                        Log.e(TAG, "run: error in while:", e)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "run: error", e)
        } finally {
            try {
                inputStream?.close()
                socket.close()
                Log.d(TAG, "run: socket closed")
            } catch (e: IOException) {
                Log.e(TAG, "error Closing inputStream/socket", e)
            }
        }
    }

    fun write(buffer: ByteArray) {
        val thread = Thread(Runnable {
            if (!socket.isClosed)
                try {
                    Thread.sleep(200)
                    outputStream!!.write(buffer)
                } catch (e: IOException) {
                    Log.e(TAG, "Error in write: ", e)
//                handler.obtainMessage(WConfiguration.HANDLE_TRY_RESTART_SOCKET).sendToTarget()
                }
        })

        thread.start()
    }
}