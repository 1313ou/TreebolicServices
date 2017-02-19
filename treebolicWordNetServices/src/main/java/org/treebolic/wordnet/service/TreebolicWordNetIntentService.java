package org.treebolic.wordnet.service;

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
	private static final String TAG = "TWordNetIntentS"; //$NON-NLS-1$

	/**
	 * Constructor
	 */
	public TreebolicWordNetIntentService()
	{
		super("TreebolicWordNetIntentService"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.IntentService#onCreate()
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
			Log.e(TreebolicWordNetIntentService.TAG, "Model factory constructor failed", e); //$NON-NLS-1$
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