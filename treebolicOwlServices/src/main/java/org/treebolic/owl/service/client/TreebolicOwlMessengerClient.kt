/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.owl.service.client

import android.content.Context
import org.treebolic.clients.TreebolicMessengerClient
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.owl.BuildConfig
import org.treebolic.owl.service.TreebolicOwlMessengerService

/**
 * Treebolic Owl messenger bound client
 *
 * @param context            context
 * @param connectionListener connection listener
 * @param modelListener      model listener
 *
 * @author Bernard Bou
 */
class TreebolicOwlMessengerClient(context: Context, connectionListener: IConnectionListener, modelListener: IModelListener) :
    TreebolicMessengerClient(context, (BuildConfig.APPLICATION_ID + '/') + TreebolicOwlMessengerService::class.java.name, connectionListener, modelListener)

