/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.wordnet.service;

import android.annotation.SuppressLint;
import android.util.Log;

import org.treebolic.services.TreebolicBoundService;

import androidx.annotation.NonNull;

/**
 * Bound service for WordNet data
 */
@SuppressLint("Registered")
public class TreebolicWordNetBoundService extends TreebolicBoundService
{
	/**
	 * Log tag
	 */
	private static final String TAG = "TWordNetBoundS";

	/**
	 * Constructor
	 */
	public TreebolicWordNetBoundService()
	{
		super();
	}

	@Override
	public void onCreate()
	{
		try
		{
			this.factory = new ModelFactory(this);
		}
		catch (@NonNull final Exception e)
		{
			Log.e(TAG, "Model factory constructor failed", e);
		}
		super.onCreate();
	}

	@SuppressWarnings("SameReturnValue")
	@NonNull
	@Override
	public String getUrlScheme()
	{
		return "wordnet:";
	}
}
