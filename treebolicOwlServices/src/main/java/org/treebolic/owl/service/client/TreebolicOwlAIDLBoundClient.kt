/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.owl.service.client

import android.content.Context
import androidx.multidex.BuildConfig
import org.treebolic.clients.TreebolicAIDLBoundClient
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.owl.service.TreebolicOwlAIDLBoundService

/**
 * Treebolic Owl bound client
 *
 * @param context            context
 * @param connectionListener connection listener
 * @param modelListener      model listener
 *
 * @author Bernard Bou
 */
class TreebolicOwlAIDLBoundClient(context: Context, connectionListener: IConnectionListener, modelListener: IModelListener) :
    TreebolicAIDLBoundClient(context, (BuildConfig.APPLICATION_ID + '/') + TreebolicOwlAIDLBoundService::class.java.getName(), connectionListener, modelListener)
