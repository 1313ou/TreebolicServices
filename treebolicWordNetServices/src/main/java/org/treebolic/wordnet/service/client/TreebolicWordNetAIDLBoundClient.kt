/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.wordnet.service.client

import android.content.Context
import org.treebolic.clients.TreebolicAIDLBoundClient
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.wordnet.BuildConfig
import org.treebolic.wordnet.service.TreebolicWordNetAIDLBoundService

/**
 * Treebolic WordNet bound client
 *
 * @param context            context
 * @param connectionListener connection listener
 * @param modelListener      model listener
 *
 * @author Bernard Bou
 */
class TreebolicWordNetAIDLBoundClient(context: Context, connectionListener: IConnectionListener, modelListener: IModelListener) :
    TreebolicAIDLBoundClient(context, (BuildConfig.APPLICATION_ID + '/') + TreebolicWordNetAIDLBoundService::class.java.getName(), connectionListener, modelListener)
