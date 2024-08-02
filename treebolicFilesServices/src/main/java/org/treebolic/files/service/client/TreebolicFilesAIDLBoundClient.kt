/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.files.service.client

import android.content.Context
import androidx.multidex.BuildConfig
import org.treebolic.clients.TreebolicAIDLBoundClient
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.files.service.TreebolicFilesAIDLBoundService

/**
 * Treebolic Files bound client
 *
 * @param context            context
 * @param connectionListener connection listener
 * @param modelListener      model listener
 *
 * @author Bernard Bou
 */
class TreebolicFilesAIDLBoundClient(context: Context, connectionListener: IConnectionListener?, modelListener: IModelListener?) :
    TreebolicAIDLBoundClient(context, BuildConfig.APPLICATION_ID + '/' + TreebolicFilesAIDLBoundService::class.java.name, connectionListener!!, modelListener!!)

