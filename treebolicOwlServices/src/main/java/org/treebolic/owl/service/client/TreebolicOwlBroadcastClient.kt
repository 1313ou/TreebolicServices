/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.owl.service.client

import android.content.Context
import org.treebolic.clients.TreebolicBroadcastClient
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.owl.BuildConfig
import org.treebolic.owl.service.TreebolicOwlBroadcastService

/**
 * Treebolic Owl broadcast service client
 *
 * @param context            context
 * @param connectionListener connection listener
 * @param modelListener      model listener
 *
 * @author Bernard Bou
 */
class TreebolicOwlBroadcastClient(context: Context, connectionListener: IConnectionListener, modelListener: IModelListener) :
    TreebolicBroadcastClient(context, (BuildConfig.APPLICATION_ID + '/') + TreebolicOwlBroadcastService::class.java.name, connectionListener, modelListener)
