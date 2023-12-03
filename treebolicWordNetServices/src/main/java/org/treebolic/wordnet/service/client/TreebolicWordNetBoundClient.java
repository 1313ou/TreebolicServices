/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.wordnet.service.client;

import android.content.Context;

import org.treebolic.clients.TreebolicBoundClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.wordnet.BuildConfig;
import org.treebolic.wordnet.service.TreebolicWordNetBoundService;

/**
 * Treebolic WordNet bound client
 *
 * @author Bernard Bou
 */
public class TreebolicWordNetBoundClient extends TreebolicBoundClient
{
	/**
	 * Constructor
	 *
	 * @param context            context
	 * @param connectionListener connection listener
	 * @param modelListener      model listener
	 */
	public TreebolicWordNetBoundClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, BuildConfig.APPLICATION_ID + '/' + TreebolicWordNetBoundService.class.getName(), connectionListener, modelListener);
	}
}
