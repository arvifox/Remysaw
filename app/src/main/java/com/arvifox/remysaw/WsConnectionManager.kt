package com.arvifox.remysaw

import jp.co.soramitsu.shared_utils.wsrpc.SocketService

class WsConnectionManager(
    private val socket: SocketService,
) {

    private lateinit var address: String

    fun setAddress(address: String) {
        this.address = address
    }

    fun start() {
        socket.start(address, false)
    }

    fun stop() {
        socket.stop()
    }

    val isStarted: Boolean
        get() = socket.started()
}
