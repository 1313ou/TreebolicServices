/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.clients

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException
import android.os.ResultReceiver
import android.util.Log
import android.widget.Toast
import org.treebolic.ParcelableModel
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.clients.iface.ITreebolicClient
import org.treebolic.services.iface.ITreebolicAIDLService
import org.treebolic.services.iface.ITreebolicService
import treebolic.model.Model

/**
 * Treebolic bound client
 **
 * @property context            context
 * @property connectionListener connectionListener
 * @property modelListener      modelListener
 * @param serviceFullName       service full name (pkg/class)

 * @author Bernard Bou
 */
open class TreebolicAIDLBoundClient(
    private val context: Context, serviceFullName: String,
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
    private var binder: ITreebolicAIDLService? = null

    /**
     * Result receiver
     */
    private val receiver: ResultReceiver

    /**
     * Constructor
     */
    init {
        val serviceNameComponents = serviceFullName.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        servicePackage = serviceNameComponents[0]
        serviceName = serviceNameComponents[1]
        receiver = object : ResultReceiver(
            Handler(Looper.getMainLooper())
        ) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                resultData.classLoader = ParcelableModel::class.java.classLoader

                // scheme
                val urlScheme = resultData.getString(ITreebolicService.RESULT_URLSCHEME)

                // model
                val isSerialized = resultData.getBoolean(ITreebolicService.RESULT_SERIALIZED)
                var model: Model? = null
                if (isSerialized) {
                    @Suppress("DEPRECATION")
                    model = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        resultData.getSerializable(ITreebolicService.RESULT_MODEL, Model::class.java) else
                        resultData.getSerializable(ITreebolicService.RESULT_MODEL) as Model?
                } else {
                    @Suppress("DEPRECATION")
                    var parcelable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        resultData.getParcelable(ITreebolicService.RESULT_MODEL, Parcelable::class.java) else
                        resultData.getParcelable(ITreebolicService.RESULT_MODEL)
                    if (parcelable != null) {
                        if (ParcelableModel::class.java != parcelable.javaClass) {
                            Log.d(TAG, "Parcel/Unparcel from source classloader " + parcelable.javaClass.classLoader + " to target classloader " + ParcelableModel::class.java.classLoader)

                            // obtain parcel
                            val parcel = Parcel.obtain()

                            // write parcel
                            parcel.setDataPosition(0)
                            parcelable.writeToParcel(parcel, 0)

                            // read parcel
                            parcel.setDataPosition(0)
                            parcelable = ParcelableModel(parcel)

                            // recycle
                            parcel.recycle()
                        }
                        val parcelModel = parcelable as ParcelableModel
                        model = parcelModel.model
                    }
                }
                modelListener.onModel(if (resultCode == 0) model else null, urlScheme)
            }
        }
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
                this@TreebolicAIDLBoundClient.isBound = true
                this@TreebolicAIDLBoundClient.binder = ITreebolicAIDLService.Stub.asInterface(binder0)

                // signal connected
                connectionListener.onConnected(true)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                this@TreebolicAIDLBoundClient.binder = null

                // signal disconnected
                connectionListener.onConnected(false)
            }
        }

        val intent = Intent()
        //intent.setAction("org.treebolic.service.action.MAKE_MODEL");
        intent.setComponent(ComponentName(servicePackage, serviceName))
        if (!context.bindService(intent, connection!!, Context.BIND_AUTO_CREATE)) {
            Log.e(TAG, "Service failed to bind $servicePackage/$serviceName")
            Toast.makeText(context, R.string.fail_bind, Toast.LENGTH_LONG).show()
        }
    }

    override fun requestModel(source: String, base: String?, imageBase: String?, settings: String?, forward: Intent?) {
        if (binder != null) {
            if (forward == null) {
                try {
                    binder!!.makeModel(source, base, imageBase, settings, receiver)
                } catch (e: RemoteException) {
                    Log.e(TAG, "Service request failed", e)
                }
            } else {
                try {
                    binder!!.makeAndForwardModel(source, base, imageBase, settings, forward)
                } catch (e: RemoteException) {
                    Log.e(TAG, "Service request failed", e)
                }
            }
        }
    }

    companion object {

        private const val TAG = "AidlBoundC"
    }
}
