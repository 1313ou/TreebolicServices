/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.owl.service.client;

import android.content.Context;

import org.treebolic.clients.TreebolicBoundClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.owl.BuildConfig;
import org.treebolic.owl.service.TreebolicOwlBoundService;

import androidx.annotation.NonNull;

/**
 * Treebolic Owl bound client
 *
 * @author Bernard Bou
 */
public class TreebolicOwlBoundClient extends TreebolicBoundClient
{
	/**
	 * Constructor
	 *
	 * @param context            context
	 * @param connectionListener connection listener
	 * @param modelListener      model listener
	 */
	public TreebolicOwlBoundClient(@NonNull final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, BuildConfig.APPLICATION_ID + '/' + TreebolicOwlBoundService.class.getName(), connectionListener, modelListener);
	}
}
