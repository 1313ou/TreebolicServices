/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.files.service.client;

import android.content.Context;

import org.treebolic.clients.TreebolicBroadcastClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.files.BuildConfig;
import org.treebolic.files.service.TreebolicFilesBroadcastService;

/**
 * Treebolic Files broadcast service client
 *
 * @author Bernard Bou
 */
public class TreebolicFilesBroadcastClient extends TreebolicBroadcastClient
{
	/**
	 * Constructor
	 *
	 * @param context            context
	 * @param connectionListener connection listener
	 * @param modelListener      model listener
	 */
	public TreebolicFilesBroadcastClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, BuildConfig.APPLICATION_ID + '/' + TreebolicFilesBroadcastService.class.getName(), connectionListener, modelListener);
	}
}
