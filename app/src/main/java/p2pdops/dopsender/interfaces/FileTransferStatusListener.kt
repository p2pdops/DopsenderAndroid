package p2pdops.dopsender.interfaces

import p2pdops.dopsender.downloader.DownloadResult

interface FileTransferStatusListener {
    fun onUploadProgress(result: DownloadResult)
}