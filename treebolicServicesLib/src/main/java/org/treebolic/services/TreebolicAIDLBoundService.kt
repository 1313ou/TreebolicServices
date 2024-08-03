/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.services

import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.os.TransactionTooLargeException
import android.util.Log
import org.treebolic.services.TaskRunner.execute
import org.treebolic.services.Utils.warn
import org.treebolic.services.iface.ITreebolicAIDLService
import org.treebolic.services.iface.ITreebolicService
import treebolic.model.Model
import java.lang.ref.WeakReference
import java.util.concurrent.Callable

/**
 * Treebolic bound service for data
 */
abstract class TreebolicAIDLBoundService: Service(), ITreebolicService {

    /**
     * Model factory
     */
    @JvmField
    protected var factory: IModelFactory? = null

    /**
     * Binder that returns an interface to the service
     */
    private val binder: ITreebolicAIDLService.Stub = object : ITreebolicAIDLService.Stub(
    ) {
        override fun makeModel(source: String, base: String, imageBase: String, settings: String, resultReceiver: ResultReceiver) {
            val callable = makeModelCallable(source, base, imageBase, settings, factory!!)
            val callback: (Model?) -> Unit = makeModelCallback(urlScheme, resultReceiver)
            execute(callable, callback)
        }

        override fun makeAndForwardModel(source: String, base: String, imageBase: String, settings: String, forward: Intent) {
            val callable = makeModelCallable(source, base, imageBase, settings, factory!!)
            val callback: (Model?) -> Unit = makeModelForwardCallback(WeakReference(this@TreebolicAIDLBoundService), urlScheme, forward)
            execute(callable, callback)
        }
    }

    /**
     * When binding to the service, we return an interface to the service
     */
    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "Binding service")
        return this.binder
    }

    companion object {

        private const val TAG = "AidlBoundS"

        /**
         * Make callable
         */
        fun makeModelCallable(source: String, base: String?, imageBase: String?, settings: String?, factory: IModelFactory): Callable<Model?> {
            return Callable {
                try {
                    return@Callable factory.make(source, base, imageBase, settings)
                } catch (e: Exception) {
                    Log.e(TAG, "Error making model", e)
                }
                null
            }
        }

        /**
         * Return callback
         */
        fun makeModelCallback(urlScheme: String?, resultReceiver: ResultReceiver): (Model?) -> Unit {
            return { model ->

                // pack model
                val bundle = Bundle()
                IntentFactory.putModelResult(bundle, model, urlScheme)

                // use result receiver
                Log.d(TAG, "Returning model $model")
                resultReceiver.send(0, bundle)
            }
        }

        /**
         * Forward callback
         */
        fun makeModelForwardCallback(contextWeakReference: WeakReference<Context>, urlScheme: String?, forward: Intent): (Model?) -> Unit {
            return  { model ->

                // do not return to client but forward it to service
                IntentFactory.putModelArg(forward, model, urlScheme)
                forward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                Log.d(TAG, "Forwarding model")
                val context = contextWeakReference.get()
                if (context != null) {
                    try {
                        context.startActivity(forward)
                    } catch (anfe: ActivityNotFoundException) {
                        warn(context, R.string.activity_not_found)
                    } catch (rte: RuntimeException) {
                        if (rte.cause is TransactionTooLargeException) {
                            warn(context, R.string.transaction_too_large)
                        } else {
                            throw rte
                        }
                    }
                }
            }
        }
    }
}
