/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.wordnet.service.client;

import android.content.Context;

import org.treebolic.clients.TreebolicMessengerClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.wordnet.BuildConfig;
import org.treebolic.wordnet.service.TreebolicWordNetMessengerService;

/**
 * Treebolic WordNet messenger bound client
 *
 * @author Bernard Bou
 */
public class TreebolicWordNetMessengerClient extends TreebolicMessengerClient
{
	/**
	 * Constructor
	 *
	 * @param context            context
	 * @param connectionListener connection listener
	 * @param modelListener      model listener
	 */
	public TreebolicWordNetMessengerClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, BuildConfig.APPLICATION_ID + '/' + TreebolicWordNetMessengerService.class.getName(), connectionListener, modelListener);
	}
}