/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.services

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.os.TransactionTooLargeException
import android.util.Log
import org.treebolic.services.Utils.warn
import org.treebolic.services.iface.ITreebolicService
import java.io.IOException

/**
 * Treebolic service for handling broadcast asynchronous task requests
 */
abstract class TreebolicBroadcastService : BroadcastReceiver(), ITreebolicService {

    /**
     * Model factory
     */
    protected var factory: IModelFactory? = null

    /**
     * Create model factory
     */
    @Throws(IOException::class)
    protected abstract fun createModelFactory(context: Context): IModelFactory?

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ITreebolicService.ACTION_MAKEMODEL == action) {
                try {
                    factory = createModelFactory(context)

                    val source = intent.getStringExtra(ITreebolicService.EXTRA_SOURCE)!!
                    val base = intent.getStringExtra(ITreebolicService.EXTRA_BASE)
                    val imageBase = intent.getStringExtra(ITreebolicService.EXTRA_IMAGEBASE)
                    val settings = intent.getStringExtra(ITreebolicService.EXTRA_SETTINGS)

                    @Suppress("DEPRECATION")
                    val forward = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 
                        intent.getParcelableExtra(ITreebolicService.EXTRA_FORWARD_RESULT_TO, Intent::class.java) else  
                        intent.getParcelableExtra(ITreebolicService.EXTRA_FORWARD_RESULT_TO)
                    try {
                        val model = factory!!.make(source, base, imageBase, settings)

                        // return/ forward
                        if (forward == null) {
                            // pack model
                            val bundle = Bundle()
                            IntentFactory.putModelResult(bundle, model, urlScheme)

                            // use result receiver
                            @Suppress("DEPRECATION")
                            val resultReceiver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                intent.getParcelableExtra(ITreebolicService.EXTRA_RECEIVER, ResultReceiver::class.java)!! else
                                intent.getParcelableExtra(ITreebolicService.EXTRA_RECEIVER)!!
                            Log.d(TAG, "Returning model $model")
                            resultReceiver.send(0, bundle)
                        } else {
                            // do not return to client but forward it to service
                            IntentFactory.putModelArg(forward, model, urlScheme)
                            forward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            Log.d(TAG, "Forwarding model")
                            try {
                                context.startActivity(forward)
                            } catch (_: ActivityNotFoundException) {
                                warn(context, R.string.activity_not_found)
                            } catch (rte: RuntimeException) {
                                if (rte.cause is TransactionTooLargeException) {
                                    warn(context, R.string.transaction_too_large)
                                } else {
                                    throw rte
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Model factory error", e)
                    }
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
        }
    }

    companion object {

        private const val TAG = "BroadcastS"
    }
}
