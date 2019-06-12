/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.owl.service.client;

import android.content.Context;

import org.treebolic.clients.TreebolicIntentClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;

/**
 * Treebolic Owl intent service client
 *
 * @author Bernard Bou
 */
public class TreebolicOwlIntentClient extends TreebolicIntentClient
{
	/**
	 * Constructor
	 *
	 * @param context            context
	 * @param connectionListener connection listener
	 * @param modelListener      model listener
	 */
	public TreebolicOwlIntentClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, "org.treebolic.owl.service" + '/' + org.treebolic.owl.service.TreebolicOwlIntentService.class.getName(), connectionListener, modelListener);
	}
}
