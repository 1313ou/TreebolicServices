/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.owl.service.client

import android.content.Context
import androidx.multidex.BuildConfig
import org.treebolic.clients.TreebolicBoundClient
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.owl.service.TreebolicOwlBoundService

/**
 * Treebolic Owl bound client
 *
 * @param context            context
 * @param connectionListener connection listener
 * @param modelListener      model listener
 *
 * @author Bernard Bou
 */
class TreebolicOwlBoundClient(context: Context, connectionListener: IConnectionListener, modelListener: IModelListener) :
    TreebolicBoundClient(context, (BuildConfig.APPLICATION_ID + '/') + TreebolicOwlBoundService::class.java.getName(), connectionListener, modelListener)

