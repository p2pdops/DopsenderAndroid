package p2pdops.dopsender.interfaces

interface FileTransferStatusListener {
    fun onBytesTransferProgress(
        ip: String?,
        filePath: String?,
        totalSize: Long,
        timeLeft: Long,
        speed: String?,
        currentSize: Long,
        percentageUploaded: Int
    )

    fun onBytesTransferCompleted(ip: String?, filePath: String?)
    fun onBytesTransferStarted(ip: String?, filePath: String?)

    fun onBytesTransferCancelled(
        ip: String?,
        error: String?,
        filePath: String?
    )
}