package org.treebolic.wordnet.service;

import android.annotation.SuppressLint;
import android.util.Log;

import org.treebolic.services.TreebolicBoundService;

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
		super.onCreate();
		try
		{
			this.factory = new ModelFactory(this);
		}
		catch (final Exception e)
		{
			Log.e(TreebolicWordNetBoundService.TAG, "Model factory constructor failed", e);
		}
	}

	@Override
	public String getUrlScheme()
	{
		return "wordnet:";
	}
}
