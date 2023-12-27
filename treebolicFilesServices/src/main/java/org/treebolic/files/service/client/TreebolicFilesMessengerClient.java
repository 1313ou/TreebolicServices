/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.files.service.client;

import android.content.Context;

import org.treebolic.clients.TreebolicMessengerClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.files.BuildConfig;
import org.treebolic.files.service.TreebolicFilesMessengerService;

import androidx.annotation.NonNull;

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
	public TreebolicFilesMessengerClient(@NonNull final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, BuildConfig.APPLICATION_ID + '/' + TreebolicFilesMessengerService.class.getName(), connectionListener, modelListener);
	}
}