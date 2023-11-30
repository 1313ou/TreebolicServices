/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.owl.service.client;

import android.content.Context;

import org.treebolic.clients.TreebolicBroadcastClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.owl.service.TreebolicOwlBroadcastService;

/**
 * Treebolic Owl broadcast service client
 *
 * @author Bernard Bou
 */
public class TreebolicOwlBroadcastClient extends TreebolicBroadcastClient
{
	/**
	 * Constructor
	 *
	 * @param context            context
	 * @param connectionListener connection listener
	 * @param modelListener      model listener
	 */
	public TreebolicOwlBroadcastClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, "org.treebolic.owl.service" + '/' + TreebolicOwlBroadcastService.class.getName(), connectionListener, modelListener);
	}
}
