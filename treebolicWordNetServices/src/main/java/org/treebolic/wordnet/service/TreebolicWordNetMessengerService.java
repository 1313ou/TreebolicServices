/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.wordnet.service;

import android.util.Log;

import org.treebolic.services.TreebolicMessengerService;

import androidx.annotation.NonNull;

/**
 * Treebolic WordNet bound messenger service
 *
 * @author Bernard Bou
 */
public class TreebolicWordNetMessengerService extends TreebolicMessengerService
{
	/**
	 * Log tag
	 */
	private static final String TAG = "TWordNetMessengerS";

	/**
	 * Constructor
	 */
	public TreebolicWordNetMessengerService()
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
			Log.e(TreebolicWordNetMessengerService.TAG, "Model factory constructor failed", e);
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
