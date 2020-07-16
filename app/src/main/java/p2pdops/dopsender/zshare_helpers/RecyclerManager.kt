package p2pdops.dopsender.zshare_helpers


import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.ac_sender_connected_lay.*

import p2pdops.dopsender.ShareActivity
import p2pdops.dopsender.modals.*
import p2pdops.dopsender.utils.slideDown

fun ShareActivity.setupMessagesRecycler() {
    connMessagesRecycler.setHasFixedSize(true)
    val layoutManager = LinearLayoutManager(this)
    layoutManager.stackFromEnd = true
    connMessagesRecycler.layoutManager = layoutManager
    connMessagesAdapter = ConnectionMessageAdapter(allConnectionMessages, this)
    connMessagesRecycler.adapter = connMessagesAdapter
}

fun ShareActivity.addSendMessageItem(item: ConnSendFileItem) {
    if (waiting.isVisible) waiting.slideDown()

    allConnectionMessages.add(item)
    connMessagesAdapter!!.notifyItemInserted(allConnectionMessages.lastIndex)
    connMessagesRecycler.smoothScrollToPosition(connMessagesAdapter!!.itemCount - 1)
}

fun ShareActivity.addReceiveMessageItem(item: ConnReceiveFileItem): Int {
    if (waiting.isVisible) waiting.slideDown()
    allConnectionMessages.add(item)
    connMessagesAdapter!!.notifyItemInserted(allConnectionMessages.lastIndex)
    connMessagesRecycler.smoothScrollToPosition(connMessagesAdapter!!.itemCount - 1)
    return allConnectionMessages.lastIndex
}

fun ShareActivity.updateItemToSending(position: Int) {
    val item = allConnectionMessages[position]
    (item as ConnSendFileItem).status = ConnFileStatusTypes.LOADING
    allConnectionMessages[position] = item
    connMessagesAdapter!!.notifyItemChanged(position)
}

fun ShareActivity.updateItemToSent(
    position: Int
) {
    val item = allConnectionMessages[position]
    (item as ConnSendFileItem).status = ConnFileStatusTypes.LOADED
    allConnectionMessages[position] = item
    connMessagesAdapter!!.notifyItemChanged(position)
}

fun ShareActivity.updateItemToReceiving(position: Int) {
    val item = allConnectionMessages[position]
    (item as ConnReceiveFileItem).status = ConnFileStatusTypes.LOADING
    allConnectionMessages[position] = item
    connMessagesAdapter!!.notifyItemChanged(position)
}

fun ShareActivity.updateItemToReceived(
    position: Int,
    fileSaveAddress: String?
) {
    val item = allConnectionMessages[position]
    fileSaveAddress?.let { (item as ConnReceiveFileItem).filePath = fileSaveAddress }
    (item as ConnReceiveFileItem).status = ConnFileStatusTypes.LOADED
    allConnectionMessages[position] = item
    connMessagesAdapter!!.notifyItemChanged(position)
}
