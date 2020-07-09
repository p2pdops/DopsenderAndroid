package p2pdops.dopsender.send_helpers

import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import p2pdops.dopsender.SenderActivity
import p2pdops.dopsender.modals.FileData
import java.io.*

class ReceiveFileDownloadTask(
    private val position: Int,
    private val c: SenderActivity,
    private val fileData: FileData
) :
    AsyncTask<Long, Long, Long>() {

    private var fileSaveAddress: String = ""

    companion object {
        private const val TAG = "ReceiveFileDownloadTask"
    }

    override fun onPreExecute() {
        super.onPreExecute()
        c.updateItemToReceiving(position)
        c.setReceivingFileName(fileData.fileName)
        c.isReceiving = true
    }

    override fun doInBackground(vararg params: Long?): Long? {

        val dopsenderFolder =
            File(Environment.getExternalStorageDirectory().canonicalPath + "/Dopsender/")

        if (!(dopsenderFolder.exists()))
            if (dopsenderFolder.mkdirs())
                Log.d(
                    TAG,
                    "doInBackground: created dopsender folder ${dopsenderFolder.canonicalPath}"
                )


        val fileAddress = "http://${c.peerAddress!!.hostAddress}:9090${fileData.filePath}"

        this.fileSaveAddress =
            dopsenderFolder.canonicalPath + '/' + fileData.type + '/' + fileAddress.substring(
                fileAddress.lastIndexOf('/') + 1
            )


        val f = File(fileSaveAddress)


        Log.d(TAG, "handleMessage: $f")


        val downloadFolder = File(f.parent!!)

        if ((downloadFolder.exists() || downloadFolder.mkdirs()) && f.createNewFile())
            Log.d(SenderActivity.TAG, "writing file : $f")

        val client = OkHttpClient()
        val call = client.newCall(Request.Builder().url(fileAddress).get().build())
        try {
            val response: Response = call.execute()
            if (response.code == 200 || response.code == 201) {
                val responseHeaders: Headers = response.headers
                for (i in 0 until responseHeaders.size) {
                    Log.d(
                        SenderActivity.TAG,
                        responseHeaders.name(i).toString() + ": " + responseHeaders.value(i)
                    )
                }
                var inputStream: InputStream? = null
                try {
                    inputStream = response.body!!.byteStream()
                    val buff = ByteArray(1024 * 8)
                    var bytesDownloaded: Long = 0
                    val timeStarted = System.currentTimeMillis()
                    var tookTime = 1L
                    var networkSpeed = 0L
                    var eta = 0L
                    var dlTimeQuotiant = 1
                    var target: Long = response.body!!.contentLength()

                    val output: OutputStream = FileOutputStream(f)

                    publishProgress(-1L, target)
                    while (true) {
                        val readed: Int = inputStream.read(buff)
                        if (readed == -1) break
                        output.write(buff, 0, readed)

                        bytesDownloaded += readed.toLong()
                        //publishProgress(bytesDownloaded, target)

                        val bytesToDownload: Long = target - bytesDownloaded
                        tookTime = System.currentTimeMillis() - timeStarted
                        if (tookTime <= 0) target = 1
                        networkSpeed = bytesDownloaded / tookTime
                        eta = bytesToDownload / networkSpeed
                        if (tookTime > 1000 * dlTimeQuotiant) {
                            dlTimeQuotiant++
                            publishProgress((bytesDownloaded / (1.0f * target) * 100).toLong(), eta)
                        }

                        if (isCancelled) {
                            Log.d(SenderActivity.TAG, "doInBackground: cancelled")
                        }
                    }
                    output.flush()
                    output.close()
                    bytesDownloaded == target
                } catch (ignore: IOException) {
                    Log.e(SenderActivity.TAG, "doInBackground: error", ignore)
                } finally {
                    Log.d(SenderActivity.TAG, "doInBackground: done")
                    inputStream?.close()
                }
            } else {
                Log.d(SenderActivity.TAG, "doInBackground: Some error")
            }
        } catch (e: IOException) {
            Log.e(SenderActivity.TAG, "doInBackground: error", e)
        }

        return 0L;
    }

    override fun onProgressUpdate(vararg values: Long?) {
        super.onProgressUpdate(*values)
        if (values[0] == -1L) {
            c.showReceiveProcessBar()
        } else {
            values[1]?.let {
                c.updateReceiveFileStatus(it, values[0]!!.toInt())
            }
        }
    }

    override fun onPostExecute(result: Long?) {
        super.onPostExecute(result)
        Log.d(SenderActivity.TAG, "onPostExecute: done")
        c.isReceiving = false
        c.updateItemToReceived(position, fileSaveAddress).also {
            c.hideReceiveProcessBar()
            c.messagePeer("REQ_NEXT_FILE", "", "")
        }
    }

    override fun onCancelled() {
        super.onCancelled()
        c.isReceiving = false
        c.hideReceiveProcessBar()
        File(fileSaveAddress).delete()
    }

    override fun onCancelled(result: Long?) {
        super.onCancelled(result)
        c.isReceiving = true
        c.hideReceiveProcessBar()
        File(fileSaveAddress).delete()
    }
}