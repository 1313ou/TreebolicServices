/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.clients

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.clients.iface.ITreebolicClient
import org.treebolic.services.iface.ITreebolicServiceBinder

/**
 * Treebolic bound client
 *
 * @property context            context
 * @property connectionListener connection listener
 * @property modelListener      model listener
 * @param serviceFullName       service full name (pkg/class)
 *
 * @author Bernard Bou
 */
open class TreebolicBoundClient(
    private val context: Context,
    serviceFullName: String,
    private val connectionListener: IConnectionListener,
    private val modelListener: IModelListener
) : ITreebolicClient {

    /**
     * Service package
     */
    private val servicePackage: String

    /**
     * Service name
     */
    private val serviceName: String

    /**
     * Connection
     */
    private var connection: ServiceConnection? = null

    /**
     * Bind state
     */
    private var isBound = false

    /**
     * Binder
     */
    private var binder: ITreebolicServiceBinder? = null

    /**
     * Constructor
     */
    init {
        val serviceNameComponents = serviceFullName.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        servicePackage = serviceNameComponents[0]
        serviceName = serviceNameComponents[1]
    }

    override fun connect() {
        bind()
    }

    override fun disconnect() {
        if (isBound) {
            Log.d(TAG, "Service disconnected")

            // detach our existing connection.
            checkNotNull(connection)
            context.unbindService(connection!!)
            isBound = false
        }
    }

    /**
     * Bind client to service
     */
    private fun bind() {
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder0: IBinder) {
                Log.d(TAG, "Service connected")
                this@TreebolicBoundClient.isBound = true
                this@TreebolicBoundClient.binder = binder0 as ITreebolicServiceBinder

                // signal connected
                connectionListener.onConnected(true)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                this@TreebolicBoundClient.binder = null

                // signal disconnected
                connectionListener.onConnected(false)
            }
        }

        val intent = Intent()
        intent.setComponent(ComponentName(servicePackage, serviceName))
        if (!context.bindService(intent, connection!!, Context.BIND_AUTO_CREATE)) {
            Log.e(TAG, "Service failed to bind")
            Toast.makeText(context, R.string.fail_bind, Toast.LENGTH_LONG).show()
        }
    }

    override fun requestModel(source: String, base: String?, imageBase: String?, settings: String?, forward: Intent?) {
        if (binder != null) {
            if (forward == null) {
                binder!!.makeModel(source, base, imageBase, settings, modelListener)
            } else {
                binder!!.makeModel(source, base, imageBase, settings, forward)
            }
        }
    }

    companion object {

        private const val TAG = "BoundC"
    }
}
