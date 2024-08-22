/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.services

import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.os.TransactionTooLargeException
import android.util.Log
import org.treebolic.services.TaskRunner.execute
import org.treebolic.services.Utils.warn
import org.treebolic.services.iface.ITreebolicService
import treebolic.model.Model
import java.lang.ref.WeakReference
import java.util.concurrent.Callable

/**
 * Treebolic messenger service
 *
 * @author Bernard Bou
 */
abstract class TreebolicMessengerService : Service(), ITreebolicService {

    /**
     * Abstract: Model factory
     */
    @JvmField
    protected var factory: IModelFactory? = null

    /**
     * Handler of incoming messages from clients.
     */
    internal class IncomingHandler internal constructor(

        private val service: TreebolicMessengerService
    ) : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                ITreebolicService.MSG_REQUEST_MODEL -> {
                    val bundle = msg.data
                    service.makeModel(bundle)
                }

                ITreebolicService.MSG_REGISTER_CLIENT -> service.clients.add(msg.replyTo)
                ITreebolicService.MSG_UNREGISTER_CLIENT -> service.clients.remove(msg.replyTo)
                else -> super.handleMessage(msg)
            }
        }
    }

    /**
     * Target we publish for clients to send messages to
     */
    private val messenger = Messenger(IncomingHandler(this))

    /**
     * List of clients
     */
    private val clients: MutableList<Messenger> = ArrayList()

    override fun onBind(intent: Intent): IBinder? {
        return messenger.binder
    }

    /**
     * Make model
     *
     * @param bundle data
     */
    private fun makeModel(bundle: Bundle) {
        val source = bundle.getString(ITreebolicService.EXTRA_SOURCE)!!
        val base = bundle.getString(ITreebolicService.EXTRA_BASE)
        val imageBase = bundle.getString(ITreebolicService.EXTRA_IMAGEBASE)
        val settings = bundle.getString(ITreebolicService.EXTRA_SETTINGS)

        @Suppress("DEPRECATION")
        val forward = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) //
            bundle.getParcelable(ITreebolicService.EXTRA_FORWARD_RESULT_TO, Intent::class.java) else  //
            bundle.getParcelable(ITreebolicService.EXTRA_FORWARD_RESULT_TO)

        // make model
        val callable = makeModelCallable(source, base, imageBase, settings, factory!!)
        val callback: (Model?) -> Unit = if (forward == null) //
            makeModelCallback(bundle, urlScheme, clients) else  //
            makeModelForwardCallback(WeakReference(this), urlScheme, forward)
        execute(callable, callback)
    }

    companion object {

        private const val TAG = "MessengerS"

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
        fun makeModelCallback(bundle: Bundle, urlScheme: String?, clients: MutableList<Messenger>): (Model?) -> Unit {
            return { model ->

                // reuse bundle
                bundle.clear()

                // pack model into message bundle
                IntentFactory.putModelResult(bundle, model, urlScheme)

                // send message to all clients
                Log.d(TAG, clients.size.toString() + " clients")
                for (i in clients.indices.reversed()) {
                    // return model to clients as a message
                    val msg = Message.obtain()
                    msg.what = ITreebolicService.MSG_RESULT_MODEL
                    msg.data = bundle

                    try {
                        Log.d(TAG, "Returning model $model")
                        clients[i].send(msg)
                    } catch (ignored: RemoteException) {
                        // The client is dead. Remove it from the list.
                        // We are going through the list from back to front, so this is safe to do inside the loop.
                        clients.removeAt(i)
                    }
                }
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
