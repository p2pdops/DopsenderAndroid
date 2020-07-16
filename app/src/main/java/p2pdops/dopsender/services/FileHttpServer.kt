package p2pdops.dopsender.services

import android.util.Log

import p2pdops.dopsender.interfaces.FileTransferStatusListener
import java.io.File
import java.io.IOException

class FileHttpServer(
    host_name: String?,
    port: Int,
    private val fileTransferListener: FileTransferStatusListener
) :
    NanoHttpD(host_name, port) {


    companion object {
        private const val TAG = "FileHttpServer"
        private const val MIME_FORCE_DOWNLOAD = "application/force-download"
    }


    override fun serve(session: IHTTPSession): Response {
        var res: Response? = null
        try {
            val url = session.uri
            Log.d(TAG, "request uri: $url")

            res = createFileResponse(url, session.headers["http-client-ip"])

        } catch (e: Exception) {
            Log.e(TAG, "serve: file not found ", e)
            res = createErrorResponse(Response.Status.FORBIDDEN, e.message)
        } finally {
            if (null == res) res = createForbiddenResponse()

            res.addHeader("Accept-Ranges", "bytes")
            res.addHeader("Access-Control-Allow-Origin", "*")
            res.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
            return res
        }
    }

    private fun createErrorResponse(
        status: Response.Status,
        message: String?
    ): Response {
        Log.e(TAG, "Error: create response: $message")
        return Response(status, MIME_PLAINTEXT, message)
    }

    private fun createForbiddenResponse(): Response {
        return createErrorResponse(
            Response.Status.FORBIDDEN,
            "FORBIDDEN: Reading file failed."
        )
    }

    @Throws(IOException::class)
    private fun createFileResponse(
        fileUrl: String,
        clientIp: String?
    ): Response {
        val file = File(fileUrl)
        Log.d(TAG, "req: ${file.absolutePath},  file length: ${file.length()}")
        val res = Response(
            Response.Status.OK,
            MIME_FORCE_DOWNLOAD,
            clientIp,
            file,
            fileTransferListener
        )
        res.addHeader("Content-Length", file.length().toString())
        res.addHeader(
            "Content-Disposition",
            "attachment; filename='${file.name}'"
        )
        return res
    }

}