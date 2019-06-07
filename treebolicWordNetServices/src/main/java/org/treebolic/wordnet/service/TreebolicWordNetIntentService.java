package org.treebolic.wordnet.service;

import android.support.annotation.NonNull;
import android.util.Log;

import org.treebolic.services.TreebolicIntentService;

/**
 * Treebolic WordNet intent service
 */
public class TreebolicWordNetIntentService extends TreebolicIntentService
{
	/**
	 * Log tag
	 */
	private static final String TAG = "TWordNetIntentS";

	/**
	 * Constructor
	 */
	public TreebolicWordNetIntentService()
	{
		super("TreebolicWordNetIntentService");
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
			Log.e(TreebolicWordNetIntentService.TAG, "Model factory constructor failed", e);
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