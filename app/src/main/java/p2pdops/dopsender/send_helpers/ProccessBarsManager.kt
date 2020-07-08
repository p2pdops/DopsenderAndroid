package p2pdops.dopsender.send_helpers

import kotlinx.android.synthetic.main.ac_sender_connected_lay.*
import p2pdops.dopsender.SenderActivity
import p2pdops.dopsender.utils.*

fun SenderActivity.initBars() {
    hideReceiveProcessBar()
    hideSendProcessBar()
    hideInfoBar()

    sendProgress.setCharacterLists("9876543210")
    receiveProgress.setCharacterLists("9876543210")

    sendEta.setCharacterLists("9876543210")
    receiveEta.setCharacterLists("9876543210")
}

fun SenderActivity.hideInfoBar() {
    if (isSending || isReceiving)
        tap_select_bar.shrink()
}

fun SenderActivity.showInfoBar() {
    if (isSending || isReceiving) return
    tap_select_bar.bulge()
}

fun SenderActivity.showSendProcessBar() {
    if (isSending) {
        sendRippleBackground.startRippleAnimation()
        send_progress_bar.bulge()
        hideInfoBar()
    }
}

fun SenderActivity.showReceiveProcessBar() {
    receiveRippleBackground.startRippleAnimation()
    receive_progress_bar.bulge()
    hideInfoBar()
}

fun SenderActivity.hideSendProcessBar() {
    sendRippleBackground.stopRippleAnimation()
    send_progress_bar.shrink()
    showInfoBar()
}

fun SenderActivity.hideReceiveProcessBar() {
    receiveRippleBackground.stopRippleAnimation()
    receive_progress_bar.shrink()
    showInfoBar()
}

fun SenderActivity.setReceivingFileName(fileName: String) {
    receivingFileName.text = "Receiving: $fileName%"
}

fun SenderActivity.setSendingFileName(fileName: String) {
    sendingFileName.text = "Sending: $fileName%"
}

fun SenderActivity.updateReceiveFileStatus(timeLeft: Long, percentage: Int) {
    receiveProgress.text = "$percentage% done,"
    receiveEta.text = "${humanizeTime(timeLeft)} left"
}

fun SenderActivity.updateSendFileStatus(timeLeft: Long, percentage: Int) {
    sendProgress.text = "$percentage% done,"
    sendEta.text = "${humanizeTime(timeLeft)} left"
}















