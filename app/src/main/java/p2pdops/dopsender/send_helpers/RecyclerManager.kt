package p2pdops.dopsender.send_helpers

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.ac_sender_connected_lay.*
import p2pdops.dopsender.SenderActivity
import p2pdops.dopsender.modals.*
import p2pdops.dopsender.utils.slideDown
import p2pdops.dopsender.utils.slideUp

fun SenderActivity.setupMessagesRecycler() {
    connMessagesRecycler.setHasFixedSize(true)
    val layoutManager = LinearLayoutManager(this)
    layoutManager.stackFromEnd = true
    connMessagesRecycler.layoutManager = layoutManager
    connMessagesAdapter = ConnectionMessageAdapter(connMessagesList, this)
    connMessagesRecycler.adapter = connMessagesAdapter
}

fun SenderActivity.addInitMessageItem(item: ConnectionItem) {

    connMessagesList.add(item)
    connMessagesAdapter!!.notifyItemInserted(connMessagesList.lastIndex)

}

fun SenderActivity.addEndMessageItem(item: ConnectionItem) {

    connMessagesList.add(item)
    connMessagesAdapter!!.notifyItemInserted(connMessagesList.lastIndex)

}

fun SenderActivity.addSendMessageItem(item: ConnSendFileItem) {
    if (tapAnimation.isVisible) tapAnimation.slideDown()
    if (waiting.isVisible) waiting.slideDown()

    connMessagesList.add(item)
    connMessagesAdapter!!.notifyItemInserted(connMessagesList.lastIndex)
    connMessagesRecycler.smoothScrollToPosition(connMessagesAdapter!!.itemCount - 1);
}

fun SenderActivity.addReceiveMessageItem(item: ConnReceiveFileItem): Int {
    if (tapAnimation.isVisible) tapAnimation.slideDown()
    if (waiting.isVisible) waiting.slideDown()
    connMessagesList.add(item)
    connMessagesAdapter!!.notifyItemInserted(connMessagesList.lastIndex)
    connMessagesRecycler.smoothScrollToPosition(connMessagesAdapter!!.itemCount - 1)
    return connMessagesList.lastIndex
}

fun SenderActivity.updateItemToSending(position: Int) {
    val item = connMessagesList[position]
    (item as ConnSendFileItem).status = ConnFileStatusTypes.LOADING
    connMessagesList[position] = item
    connMessagesAdapter!!.notifyItemChanged(position)
}

fun SenderActivity.updateItemToSent(
    position: Int
) {
    val item = connMessagesList[position]
    (item as ConnSendFileItem).status = ConnFileStatusTypes.LOADED
    connMessagesList[position] = item
    connMessagesAdapter!!.notifyItemChanged(position)
}

fun SenderActivity.updateItemToReceiving(position: Int) {
    val item = connMessagesList[position]
    (item as ConnReceiveFileItem).status = ConnFileStatusTypes.LOADING
    connMessagesList[position] = item
    connMessagesAdapter!!.notifyItemChanged(position)
}

fun SenderActivity.updateItemToReceived(
    position: Int,
    fileSaveAddress: String?
) {
    val item = connMessagesList[position]
    fileSaveAddress?.let { (item as ConnReceiveFileItem).filePath = fileSaveAddress }
    (item as ConnReceiveFileItem).status = ConnFileStatusTypes.LOADED
    connMessagesList[position] = item
    connMessagesAdapter!!.notifyItemChanged(position)
}
