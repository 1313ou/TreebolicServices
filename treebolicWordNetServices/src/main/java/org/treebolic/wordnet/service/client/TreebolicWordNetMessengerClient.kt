/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.wordnet.service.client

import android.content.Context
import org.treebolic.clients.TreebolicMessengerClient
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.wordnet.BuildConfig
import org.treebolic.wordnet.service.TreebolicWordNetMessengerService

/**
 * Treebolic WordNet messenger bound client
 *
 * @param context            context
 * @param connectionListener connection listener
 * @param modelListener      model listener
 *
 * @author Bernard Bou
 */
class TreebolicWordNetMessengerClient(context: Context, connectionListener: IConnectionListener, modelListener: IModelListener) :
    TreebolicMessengerClient(context, (BuildConfig.APPLICATION_ID + '/') + TreebolicWordNetMessengerService::class.java.getName(), connectionListener, modelListener)
