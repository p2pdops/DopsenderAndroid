package p2pdops.dopsender.interfaces

interface FileTransferStatusListener {
    fun onBytesTransferProgress(
        ip: String?,
        fileName: String?,
        totalSize: Long,
        timeLeft: Long,
        speed: String?,
        currentSize: Long,
        percentageUploaded: Int
    )

    fun onBytesTransferCompleted(ip: String?, fileName: String?)
    fun onBytesTransferStarted(ip: String?, fileName: String?)
    fun onBytesTransferCancelled(
        ip: String?,
        error: String?,
        fileName: String?
    )
}