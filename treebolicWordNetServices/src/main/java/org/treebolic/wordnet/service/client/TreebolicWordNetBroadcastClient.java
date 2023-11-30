/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.wordnet.service.client;

import android.content.Context;

import org.treebolic.clients.TreebolicBroadcastClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.wordnet.service.TreebolicWordNetBroadcastService;

/**
 * Treebolic WordNet broadcast service client
 *
 * @author Bernard Bou
 */
public class TreebolicWordNetBroadcastClient extends TreebolicBroadcastClient
{
	/**
	 * Constructor
	 *
	 * @param context            context
	 * @param connectionListener connection listener
	 * @param modelListener      model listener
	 */
	public TreebolicWordNetBroadcastClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, "org.treebolic.wordnet.service" + '/' + TreebolicWordNetBroadcastService.class.getName(), connectionListener, modelListener);
	}
}
