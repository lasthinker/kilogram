package net.kilogram.messenger

import android.graphics.Color
import org.telegram.tgnet.ConnectionsManager

object DataCenter {

    @JvmStatic
    fun applyOfficalDataCanter(account: Int) {

        if (ConnectionsManager.native_isTestBackend(account) != 0) {
            ConnectionsManager.getInstance(account).switchBackend(false)
        }

        ConnectionsManager.native_cleanUp(account, true)

    }

    @JvmStatic
    fun applyTestDataCenter(account: Int) {

        if (ConnectionsManager.native_isTestBackend(account) == 0) {
            ConnectionsManager.getInstance(account).switchBackend(false)
        }

    }

}