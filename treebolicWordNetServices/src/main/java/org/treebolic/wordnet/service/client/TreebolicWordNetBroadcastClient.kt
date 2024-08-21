/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.wordnet.service.client

import android.content.Context
import org.treebolic.clients.TreebolicBroadcastClient
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.wordnet.BuildConfig
import org.treebolic.wordnet.service.TreebolicWordNetBroadcastService

/**
 * Treebolic WordNet broadcast service client
 *
 * @param context            context
 * @param connectionListener connection listener
 * @param modelListener      model listener
 *
 * @author Bernard Bou
 */
class TreebolicWordNetBroadcastClient(context: Context, connectionListener: IConnectionListener, modelListener: IModelListener) :
    TreebolicBroadcastClient(context, (BuildConfig.APPLICATION_ID + '/') + TreebolicWordNetBroadcastService::class.java.getName(), connectionListener, modelListener)

