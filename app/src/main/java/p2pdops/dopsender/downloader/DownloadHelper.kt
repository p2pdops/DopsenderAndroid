package p2pdops.dopsender.downloader

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.*
import kotlin.math.roundToInt

sealed class DownloadResult {

    class Success : DownloadResult() {
        override fun equals(other: Any?): Boolean {
            return this === other
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }
    }

    class Started : DownloadResult() {
        override fun equals(other: Any?): Boolean {
            return this === other
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }
    }

    class DownloadStarted : DownloadResult() {
        override fun equals(other: Any?): Boolean {
            return this === other
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }
    }

    data class Error(val message: String, val cause: Exception? = null) : DownloadResult()

    data class Progress(val percentage: Int, val eta: Long) : DownloadResult()
}

const val TAG = "Downloader"
suspend fun downloadFile(file: File, url: String): Flow<DownloadResult> {
    return flow {
        Log.d(TAG, "downloadFile: ")
        val client = OkHttpClient()
        val call = client.newCall(Request.Builder().url(url).get().build())
        try {
            withContext(Dispatchers.IO) {
                val response: Response = call.execute()
                if (response.code == 200 || response.code == 201) {
                    emit(DownloadResult.Started())
                    val responseHeaders: Headers = response.headers
                    for (i in 0 until responseHeaders.size) {
                        Log.d(
                            "downloadFile",
                            responseHeaders.name(i) + ": " + responseHeaders.value(i)
                        )
                    }
                    var inputStream: InputStream? = null
                    try {
                        inputStream = response.body!!.byteStream()
                        val buff = ByteArray(1024 * 8)
                        var bytesDownloaded: Long = 0
                        val timeStarted = System.currentTimeMillis()
                        var tookTime: Long
                        var networkSpeed: Long
                        var eta: Long
                        var percentage: Int
                        var updateInterval = 1
                        val target: Long = response.body!!.contentLength()

                        val output: OutputStream = FileOutputStream(file)

                        var bytesRead: Int

                        var sentStarted = false

                        while (inputStream.read(buff).also { bytesRead = it } != -1) {
                            output.write(buff, 0, bytesRead)

                            bytesDownloaded += bytesRead.toLong()

                            val bytesToDownload: Long = target - bytesDownloaded

                            tookTime = System.currentTimeMillis() - timeStarted

                            if (tookTime <= 0) tookTime = 1

                            networkSpeed = bytesDownloaded / tookTime

                            eta = bytesToDownload / networkSpeed

                            if (tookTime > 1250 * updateInterval) {
                                if (!sentStarted) emit(DownloadResult.DownloadStarted()).also {
                                    sentStarted = true
                                }

                                updateInterval++
                                percentage = (bytesDownloaded / (1.0f * target) * 100).roundToInt()
                                emit(DownloadResult.Progress(percentage, eta))
                            }

                        }
                        output.flush()
                        output.close()
                    } catch (e: IOException) {
                        Log.e(TAG, "downloadFile: error", e)
                    } finally {
                        Log.d(TAG, "downloadFile: done")
                        inputStream?.close()
                        emit(DownloadResult.Success())
                    }
                } else {
                    Log.d(TAG, "downloadFile: Some error")
                    emit(DownloadResult.Error("Error ${response.code}"))
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "downloadFile: error", e)
        }
    }
}