/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.files.service.client

import android.content.Context
import org.treebolic.clients.TreebolicBoundClient
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.files.BuildConfig
import org.treebolic.files.service.TreebolicFilesBoundService

/**
 * Treebolic Files bound client
 *
 * @param context            context
 * @param connectionListener connection listener
 * @param modelListener      model listener
 *
 * @author Bernard Bou
 */
class TreebolicFilesBoundClient(context: Context, connectionListener: IConnectionListener, modelListener: IModelListener) :
    TreebolicBoundClient(context, (BuildConfig.APPLICATION_ID + '/') + TreebolicFilesBoundService::class.java.getName(), connectionListener, modelListener)

