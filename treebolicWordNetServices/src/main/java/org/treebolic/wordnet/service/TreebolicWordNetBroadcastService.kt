/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.wordnet.service;

import android.content.Context;
import android.util.Log;

import org.treebolic.services.IModelFactory;
import org.treebolic.services.TreebolicBroadcastService;

import java.io.IOException;

import androidx.annotation.NonNull;

/**
 * Treebolic WordNet broadcast service
 */
public class TreebolicWordNetBroadcastService extends TreebolicBroadcastService
{
	/**
	 * Log tag
	 */
	private static final String TAG = "TWordNetBroadcastS";

	/**
	 * Constructor
	 */
	public TreebolicWordNetBroadcastService()
	{
		super();
	}

	@NonNull
	@Override
	protected IModelFactory createModelFactory(@NonNull final Context context) throws IOException
	{
		try
		{
			return new ModelFactory(context);
		}
		catch (@NonNull final IOException e)
		{
			Log.e(TAG, "Model factory constructor failed", e);
			throw e;
		}
	}

	@SuppressWarnings("SameReturnValue")
	@NonNull
	@Override
	public String getUrlScheme()
	{
		return "wordnet:";
	}
}