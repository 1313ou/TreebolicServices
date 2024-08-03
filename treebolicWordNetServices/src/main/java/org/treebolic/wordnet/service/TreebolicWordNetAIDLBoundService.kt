/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.wordnet.service;

import android.util.Log;

import org.treebolic.services.TreebolicAIDLBoundService;

import androidx.annotation.NonNull;

/**
 * Bound service for WordNet data
 */
public class TreebolicWordNetAIDLBoundService extends TreebolicAIDLBoundService
{
	/**
	 * Log tag
	 */
	private static final String TAG = "TWordNetAIDLS";

	/**
	 * Constructor
	 */
	public TreebolicWordNetAIDLBoundService()
	{
		super();
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		try
		{
			this.factory = new ModelFactory(this);
		}
		catch (@NonNull final Exception e)
		{
			Log.e(TAG, "Model factory constructor failed", e);
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
