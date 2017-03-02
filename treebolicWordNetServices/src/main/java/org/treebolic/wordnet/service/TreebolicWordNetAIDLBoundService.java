package org.treebolic.wordnet.service;

import android.util.Log;

import org.treebolic.services.TreebolicAIDLBoundService;

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
		catch (final Exception e)
		{
			Log.e(TreebolicWordNetAIDLBoundService.TAG, "Model factory constructor failed", e);
		}
	}

	@Override
	public String getUrlScheme()
	{
		return "wordnet:";
	}
}
