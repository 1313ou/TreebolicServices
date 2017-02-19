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
	private static final String TAG = "TWordNetAIDLS"; //$NON-NLS-1$

	/**
	 * Constructor
	 */
	public TreebolicWordNetAIDLBoundService()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Service#onCreate()
	 */
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
			Log.e(TreebolicWordNetAIDLBoundService.TAG, "Model factory constructor failed", e); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.treebolic.services.iface.ITreebolicService#getUrlScheme()
	 */
	@Override
	public String getUrlScheme()
	{
		return "wordnet:"; //$NON-NLS-1$
	}
}
