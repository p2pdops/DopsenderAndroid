package p2pdops.dopsender.interfaces

import p2pdops.dopsender.modals.ConnReceiveFileItem
import p2pdops.dopsender.modals.ConnSendFileItem

interface ShareActivityImpl {

    fun onSocketsConnected()

    fun onAddedSenderItem(item: ConnSendFileItem)
    fun onAddedReceiverItem(downloadUrl: String, receiveItem: ConnReceiveFileItem)


    fun onSendingItemStart(item: ConnSendFileItem)

    fun onSendingItemProgress(percentage: Int, eta: Long)
    fun onSendingItemSuccess(item: ConnSendFileItem)

    fun handlePeerDisconnected()
    fun onInitError()

}