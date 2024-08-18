/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.clients

import android.util.Log
import android.widget.Toast
import org.treebolic.AppCompatCommonActivity
import org.treebolic.TreebolicIface
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.clients.iface.ITreebolicClient

/**
 * Treebolic server. Produces Treebolic model from data. Acts as client to service.
 *
 * @author Bernard Bou
 */
abstract class TreebolicClientActivityStub : AppCompatCommonActivity(), IConnectionListener, IModelListener {

    /**
     * Client
     */
    @JvmField
    protected var client: ITreebolicClient? = null

    /**
     * Client status true=up
     */
    @JvmField
    protected var clientStatus: Boolean = false

    /**
     * Url scheme
     */
    @JvmField
    protected var urlScheme: String? = null

    // L I F E C Y C L E

    override fun onResume() {
        stop()
        start()
        super.onResume()
    }

    override fun onPause() {
        stop()
        super.onPause()
    }

    // C L I E N T M A N A G E M E N T

    protected abstract fun makeClient(): ITreebolicClient?

    /**
     * Start client
     */
    protected fun start() {
        client = makeClient()
        if (client != null) {
            Log.d(TAG, "Connecting client-service")
            client!!.connect()
        }
    }

    /**
     * Stop client
     */
    protected fun stop() {
        if (client != null) {
            Log.d(TAG, "Disconnecting client-service")
            client!!.disconnect()
            client = null
        }
    }

    /**
     * Request model
     */
    private fun request() {
        // get query from activity intent
        val intent = intent
        if (TreebolicIface.ACTION_MAKEMODEL == intent.action) {
            val query = intent.getStringExtra(TreebolicIface.ARG_SOURCE)
            val base = intent.getStringExtra(TreebolicIface.ARG_BASE)
            val imageBase = intent.getStringExtra(TreebolicIface.ARG_IMAGEBASE)
            val settings = intent.getStringExtra(TreebolicIface.ARG_SETTINGS)

            if (query.isNullOrEmpty()) {
                Toast.makeText(this, R.string.fail_nullquery, Toast.LENGTH_SHORT).show()
                return
            } else if (client == null) {
                Toast.makeText(this, R.string.fail_nullclient, Toast.LENGTH_SHORT).show()
                return
            }

            // request model from query
            client!!.requestModel(query, base, imageBase, settings, null)
        }
    }

    // C O N N E C T I O N L I S T E N E R

    override fun onConnected(flag: Boolean) {
        clientStatus = flag
        request()
    }

    companion object {

        private const val TAG = "AClientA"
    }
}
