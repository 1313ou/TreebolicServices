package org.treebolic.wordnet.service;

import android.util.Log;

import org.treebolic.services.TreebolicMessengerService;

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
		catch (final Exception e)
		{
			Log.e(TreebolicWordNetMessengerService.TAG, "Model factory constructor failed", e);
		}
	}

	@Override
	public String getUrlScheme()
	{
		return "wordnet:";
	}
}
