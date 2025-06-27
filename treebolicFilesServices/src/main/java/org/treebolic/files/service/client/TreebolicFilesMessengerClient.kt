/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.files.service.client

import android.content.Context
import org.treebolic.clients.TreebolicMessengerClient
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.files.BuildConfig
import org.treebolic.files.service.TreebolicFilesMessengerService

/**
 * Treebolic Files messenger bound client
 *
 * @param context            context
 * @param connectionListener connection listener
 * @param modelListener      model listener
 *
 * @author Bernard Bou
 */
class TreebolicFilesMessengerClient(context: Context, connectionListener: IConnectionListener, modelListener: IModelListener) :
    TreebolicMessengerClient(context, (BuildConfig.APPLICATION_ID + '/') + TreebolicFilesMessengerService::class.java.name, connectionListener, modelListener)
