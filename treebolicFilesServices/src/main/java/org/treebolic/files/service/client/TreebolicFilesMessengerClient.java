/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.files.service.client;

import android.content.Context;

import org.treebolic.clients.TreebolicMessengerClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;

/**
 * Treebolic Files messenger bound client
 *
 * @author Bernard Bou
 */
public class TreebolicFilesMessengerClient extends TreebolicMessengerClient
{
	/**
	 * Constructor
	 *
	 * @param context            context
	 * @param connectionListener connection listener
	 * @param modelListener      model listener
	 */
	public TreebolicFilesMessengerClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, "org.treebolic.files.service" + '/' + org.treebolic.files.service.TreebolicFilesMessengerService.class.getName(), connectionListener, modelListener);
	}
}