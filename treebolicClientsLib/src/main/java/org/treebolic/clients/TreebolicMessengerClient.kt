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
import android.os.Message
import android.os.Messenger
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import org.treebolic.ParcelableModel
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.clients.iface.ITreebolicClient
import org.treebolic.services.iface.ITreebolicService
import treebolic.model.Model

/**
 * Treebolic messenger bound client
 *
 * @property context            context
 * @property connectionListener connection listener
 * @property modelListener      model listener
 * @param serviceFullName       service full name (pkg/class)

 * @author Bernard Bou
 */
open class TreebolicMessengerClient(
    private val context: Context, serviceFullName: String,
    private val connectionListener: IConnectionListener,
    private val modelListener: IModelListener

) : ITreebolicClient {

    /**
     * Handler of incoming messages (results) from service

     * @property client client
     */
    internal class IncomingHandler(
        private val client: TreebolicMessengerClient
    ) : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            if (msg.what == ITreebolicService.MSG_RESULT_MODEL) {
                val resultData = msg.data
                resultData.classLoader = ParcelableModel::class.java.classLoader
                val urlScheme = resultData.getString(ITreebolicService.RESULT_URLSCHEME)
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
                client.modelListener.onModel(model, urlScheme)
            } else {
                super.handleMessage(msg)
            }
        }
    }

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
     * Bind status
     */
    private var isBound = false

    /**
     * Messenger returned by service when binding
     */
    private var service: Messenger? = null

    /**
     * Messenger used to receive data from service
     */
    private var inMessenger: Messenger? = null

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

            // if we have received the service, and hence registered with it
            if (service != null) {
                try {
                    val msg = Message.obtain(null, ITreebolicService.MSG_UNREGISTER_CLIENT)
                    msg.replyTo = inMessenger
                    service!!.send(msg)
                } catch (_: RemoteException) {
                    // there is nothing special we need to do if the service has crashed.
                }
            }

            // detach our existing connection.
            context.unbindService(connection!!)
            isBound = false
        }
    }

    /**
     * Bind client to service
     */
    private fun bind() {
        // prepare connection
        inMessenger = Messenger(IncomingHandler(this))
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder0: IBinder) {
                Log.d(TAG, "Service bound")
                this@TreebolicMessengerClient.isBound = true

                // pass service in-messenger to post results to
                this@TreebolicMessengerClient.service = Messenger(binder0)
                val msg = Message.obtain(null, ITreebolicService.MSG_REGISTER_CLIENT)
                msg.replyTo = this@TreebolicMessengerClient.inMessenger
                try {
                    service!!.send(msg)
                } catch (e: RemoteException) {
                    Log.e(TAG, "Send error", e)
                }

                // signal connected
                connectionListener.onConnected(true)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                this@TreebolicMessengerClient.service = null

                // signal disconnected
                connectionListener.onConnected(false)
            }
        }

        // bind
        val intent = Intent()
        intent.component = ComponentName(servicePackage, serviceName)
        if (!context.bindService(intent, connection!!, Context.BIND_AUTO_CREATE)) {
            Log.e(TAG, "Service failed to bind")
            Toast.makeText(context, R.string.fail_bind, Toast.LENGTH_LONG).show()
        }
    }

    override fun requestModel(source: String, base: String?, imageBase: String?, settings: String?, forward: Intent?) {
        // bundle
        val bundle = Bundle()
        bundle.putString(ITreebolicService.EXTRA_SOURCE, source)
        bundle.putString(ITreebolicService.EXTRA_BASE, base)
        bundle.putString(ITreebolicService.EXTRA_IMAGEBASE, imageBase)
        bundle.putString(ITreebolicService.EXTRA_SETTINGS, settings)
        bundle.putParcelable(ITreebolicService.EXTRA_FORWARD_RESULT_TO, forward)

        // request message
        val msg = Message.obtain(null, ITreebolicService.MSG_REQUEST_MODEL, 0, 0)

        // attach bundle
        msg.data = bundle

        // send message
        if (service != null) {
            try {
                service!!.send(msg)
            } catch (e: RemoteException) {
                Log.e(TAG, "Send error", e)
            }
        }
    }

    companion object {

        private const val TAG = "MessengerC"
    }
}
