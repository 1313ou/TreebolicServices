/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.owl.service.client;

import android.content.Context;

import org.treebolic.clients.TreebolicAIDLBoundClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.owl.BuildConfig;
import org.treebolic.owl.service.TreebolicOwlAIDLBoundService;

/**
 * Treebolic Owl bound client
 *
 * @author Bernard Bou
 */
public class TreebolicOwlAIDLBoundClient extends TreebolicAIDLBoundClient
{
	/**
	 * Constructor
	 *
	 * @param context            context
	 * @param connectionListener connection listener
	 * @param modelListener      model listener
	 */
	public TreebolicOwlAIDLBoundClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, BuildConfig.APPLICATION_ID + '/' + TreebolicOwlAIDLBoundService.class.getName(), connectionListener, modelListener);
	}
}
