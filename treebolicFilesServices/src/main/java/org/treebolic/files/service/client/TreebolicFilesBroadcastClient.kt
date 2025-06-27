/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.files.service.client

import android.content.Context
import org.treebolic.clients.TreebolicBroadcastClient
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.files.BuildConfig
import org.treebolic.files.service.TreebolicFilesBroadcastService

/**
 * Treebolic Files broadcast service client
 *
 * @param context            context
 * @param connectionListener connection listener
 * @param modelListener      model listener
 *
 * @author Bernard Bou
 */
class TreebolicFilesBroadcastClient(context: Context, connectionListener: IConnectionListener, modelListener: IModelListener) :
    TreebolicBroadcastClient(context, (BuildConfig.APPLICATION_ID + '/') + TreebolicFilesBroadcastService::class.java.name, connectionListener, modelListener)

