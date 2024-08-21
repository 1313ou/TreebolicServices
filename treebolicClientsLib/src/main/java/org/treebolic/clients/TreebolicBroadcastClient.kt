/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.clients

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.os.ResultReceiver
import android.util.Log
import android.widget.Toast
import org.treebolic.ParcelableModel
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.clients.iface.ITreebolicClient
import org.treebolic.services.iface.ITreebolicService
import treebolic.model.Model

/**
 * Treebolic broadcast service client
 *
 * @property context            context
 * @property connectionListener connection listener
 * @property modelListener      model listener
 * @param serviceFullName       service full name (pkg/class)
 *
 * @author Bernard Bou
 */
open class TreebolicBroadcastClient(
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
     * Result receiver
     */
    private val receiver: ResultReceiver?

    /**
     * Constructor
     */
    init {
        val serviceNameComponents = serviceFullName.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        this.servicePackage = serviceNameComponents[0]
        this.serviceName = serviceNameComponents[1]
        this.receiver = object : ResultReceiver(
            Handler(Looper.getMainLooper())
        ) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
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
                modelListener.onModel(if (resultCode == 0) model else null, urlScheme)
            }
        }
    }

    override fun connect() {
        connectionListener.onConnected(true)
    }

    override fun disconnect() {
        connectionListener.onConnected(false)
    }

    override fun requestModel(source: String, base: String?, imageBase: String?, settings: String?, forward: Intent?) {
        val component = ComponentName(this.servicePackage, this.serviceName)

        val intent = Intent()
        intent.setComponent(component)
        intent.setAction(ITreebolicService.ACTION_MAKEMODEL)
        intent.putExtra(ITreebolicService.EXTRA_SOURCE, source)
        intent.putExtra(ITreebolicService.EXTRA_BASE, base)
        intent.putExtra(ITreebolicService.EXTRA_IMAGEBASE, imageBase)
        intent.putExtra(ITreebolicService.EXTRA_SETTINGS, settings)
        intent.putExtra(ITreebolicService.EXTRA_RECEIVER, this.receiver)
        intent.putExtra(ITreebolicService.EXTRA_FORWARD_RESULT_TO, forward)

        context.sendBroadcast(intent)

        Log.d(TAG, "Intent broadcast to " + this.servicePackage + '/' + this.serviceName)
        Toast.makeText(this.context, R.string.started, Toast.LENGTH_LONG).show()
    }

    companion object {

        private const val TAG = "BroadcastC"
    }
}
