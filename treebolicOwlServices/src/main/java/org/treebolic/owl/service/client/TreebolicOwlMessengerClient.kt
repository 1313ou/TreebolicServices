/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.owl.service.client;

import android.content.Context;

import org.treebolic.clients.TreebolicMessengerClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.owl.BuildConfig;
import org.treebolic.owl.service.TreebolicOwlMessengerService;

import androidx.annotation.NonNull;

/**
 * Treebolic Owl messenger bound client
 *
 * @author Bernard Bou
 */
public class TreebolicOwlMessengerClient extends TreebolicMessengerClient
{
	/**
	 * Constructor
	 *
	 * @param context            context
	 * @param connectionListener connection listener
	 * @param modelListener      model listener
	 */
	public TreebolicOwlMessengerClient(@NonNull final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, BuildConfig.APPLICATION_ID + '/' + TreebolicOwlMessengerService.class.getName(), connectionListener, modelListener);
	}
}
