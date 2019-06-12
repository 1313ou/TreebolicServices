/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.wordnet.service.client;

import android.content.Context;

import org.treebolic.clients.TreebolicAIDLBoundClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;

/**
 * Treebolic WordNet bound client
 *
 * @author Bernard Bou
 */
public class TreebolicWordNetAIDLBoundClient extends TreebolicAIDLBoundClient
{
	/**
	 * Constructor
	 *
	 * @param context            context
	 * @param connectionListener connection listener
	 * @param modelListener      model listener
	 */
	public TreebolicWordNetAIDLBoundClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, "org.treebolic.wordnet.service" + '/' + org.treebolic.wordnet.service.TreebolicWordNetAIDLBoundService.class.getName(), connectionListener, modelListener);
	}
}
