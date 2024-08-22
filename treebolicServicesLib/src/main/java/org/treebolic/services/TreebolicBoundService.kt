/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.services

import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.TransactionTooLargeException
import android.util.Log
import org.treebolic.clients.iface.IModelListener
import org.treebolic.services.TaskRunner.execute
import org.treebolic.services.Utils.warn
import org.treebolic.services.iface.ITreebolicService
import org.treebolic.services.iface.ITreebolicServiceBinder
import treebolic.model.Model
import java.lang.ref.WeakReference
import java.util.concurrent.Callable

/**
 * Treebolic bound service for data
 */
abstract class TreebolicBoundService : Service(), ITreebolicService {

    /**
     * Model factory
     */
    @JvmField
    protected var factory: IModelFactory? = null

    /**
     * Binder given to clients
     */
    inner class TreebolicServiceBinder internal constructor(factory: IModelFactory?, private val urlScheme: String) : Binder(), ITreebolicServiceBinder {

        private val factory = factory!!

        override fun makeModel(source: String, base: String?, imageBase: String?, settings: String?, modelListener: IModelListener) {
            val callable = makeModelCallable(source, base, imageBase, settings, factory)
            val callback = makeModelCallback(urlScheme, modelListener)
            execute(callable, callback)
        }

        override fun makeModel(source: String, base: String?, imageBase: String?, settings: String?, forward: Intent) {
            val callable = makeModelCallable(source, base, imageBase, settings, factory)
            val callback = makeModelForwardCallback(WeakReference(this@TreebolicBoundService), urlScheme, forward)
            execute(callable, callback)
        }
    }

    /**
     * Binder that returns an interface to the service
     */
    private var binder: IBinder? = null

    override fun onCreate() {
        super.onCreate()
        checkNotNull(factory)
        binder = TreebolicServiceBinder(factory, urlScheme)
    }

    /**
     * When binding to the service, we return an interface to the service
     */
    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "Binding service")
        checkNotNull(binder)
        return binder!!
    }

    companion object {

        private const val TAG = "BoundS"

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
        fun makeModelCallback(urlScheme: String?, modelListener: IModelListener): (Model?) -> Unit {
            return { model ->
                Log.d(TAG, "Returning model $model")
                modelListener.onModel(model, urlScheme)
            }
        }

        /**
         * Forward callback
         */
        fun makeModelForwardCallback(contextWeakReference: WeakReference<Context>, urlScheme: String?, forward: Intent): (Model?) -> Unit {
            return { model ->

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
