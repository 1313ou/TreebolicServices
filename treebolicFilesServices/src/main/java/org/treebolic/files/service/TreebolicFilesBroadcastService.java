/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.files.service;

import android.content.Context;

import org.treebolic.services.IModelFactory;
import org.treebolic.services.TreebolicBroadcastService;

import java.io.IOException;

import androidx.annotation.NonNull;

/**
 * Treebolic Files broadcast service
 */
public class TreebolicFilesBroadcastService extends TreebolicBroadcastService
{
	/**
	 * Constructor
	 */
	public TreebolicFilesBroadcastService()
	{
		super();
	}

	@Override
	protected IModelFactory createModelFactory(@NonNull final Context context) throws IOException
	{
		return new ModelFactory(context);
	}

	@SuppressWarnings("SameReturnValue")
	@NonNull
	@Override
	public String getUrlScheme()
	{
		return "directory:";
	}
}
