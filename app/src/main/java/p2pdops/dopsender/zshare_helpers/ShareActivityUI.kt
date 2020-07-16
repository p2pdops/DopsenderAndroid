package p2pdops.dopsender.zshare_helpers

import kotlinx.android.synthetic.main.ac_sender_connected_lay.*
import p2pdops.dopsender.ShareActivity
import p2pdops.dopsender.utils.*

fun ShareActivity.initBars() {
    hideReceiveProcessBar()
    hideSendProcessBar()
}

fun ShareActivity.showInfoBar() {
    if (isSending || isReceiving) return
    tap_select_bar.bulge()
}

fun ShareActivity.showSendProcessBar() {
    if (isSending) {
        sendRippleBackground.startRippleAnimation()
        send_progress_bar.slideUp()
    }
}

fun ShareActivity.showReceiveProcessBar() {
    receiveRippleBackground.startRippleAnimation()
    receive_progress_bar.slideUp()
}

fun ShareActivity.hideSendProcessBar() {
    sendRippleBackground.stopRippleAnimation()
    send_progress_bar.slideDown()
    showInfoBar()
}

fun ShareActivity.hideReceiveProcessBar() {
    receiveRippleBackground.stopRippleAnimation()
    receive_progress_bar.slideDown()
    showInfoBar()
}

fun ShareActivity.setReceivingFileName(fileName: String) {
    receivingFileName.text = "Receiving: $fileName%"
}

fun ShareActivity.setSendingFilePath(fileName: String) {
    sendingFileName.text = "Sending: $fileName%"
}

fun ShareActivity.updateReceiveFileStatus(percentage: Int, timeLeft: Long) {
    receiveProgress.text = "$percentage% done,"
    receiveEta.text = "${humanizeTime(timeLeft)} left"
}

fun ShareActivity.updateSendFileStatus(timeLeft: Long, percentage: Int) {
    sendProgress.text = "$percentage% done,"
    sendEta.text = "${humanizeTime(timeLeft)} left"
}









