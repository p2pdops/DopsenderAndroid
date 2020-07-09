package p2pdops.dopsender.local_connection

import android.net.wifi.p2p.WifiP2pDevice
import java.util.*

data class P2pDestinationDevice(val p2pDevice: WifiP2pDevice?)

data class WConnMessage(
    val type: String,
    val intData: Int?,
    val longData: Long?,
    val stringData: String?
)

class WaitingToSendListElement {

    val waitingToSendList: List<String>

    init {
        waitingToSendList = ArrayList()
    }
}


class WaitingToSendQueue private constructor() {
    private val waitingToSend: MutableList<WaitingToSendListElement?>

    /**
     * Method to get an element from the list using the tab number.
     * Contains the logic to retrieve the correct
     * WaitingToSendListElement ArrayList.
     * This method returns always an element, because if the element isn't in the list, this method adds
     * a new WaitingToSendListElement
     * at the specified tabNumber-1 position.
     * WaitingToSendListElement
     * @param tabNumber int that represents the tabNumber used to retrieve the ArrayList.
     * @return List element of the waitingToSend list.
     */

    fun getWaitingToSendItemsList(tabNumber: Int): List<String> {

        //to remap the tabNumber index to the waitingToSend list's index, this method
        //uses only "(tabNumber - 1)".

        //if tabNumber index in between 0 and size-1
        if (tabNumber - 1 >= 0 && tabNumber - 1 <= waitingToSend.size - 1) {

            //if this element is null i set the WaitingToSendListElement() at tabNumber-1
            if (waitingToSend[tabNumber - 1] == null) {
                waitingToSend[tabNumber - 1] = WaitingToSendListElement()
            }

            //if is !=null, do nothing, because i have the list ready and probably with elements
            //and i can't lost this elements
        } else {

            //if the tabNumber index is not available, i add a new WaitingToSendListElement ad the end of the waitingToSend List.
            waitingToSend.add(tabNumber - 1, WaitingToSendListElement())
        }
        return waitingToSend[tabNumber - 1]!!.waitingToSendList
    }

    companion object {
        /**
         * Method to get the instance of this class.
         * @return instance of this class.
         */
        val instance = WaitingToSendQueue()

    }

    /**
     * Private constructor, because is a singleton class.
     */
    init {
        waitingToSend = ArrayList()
    }
}

