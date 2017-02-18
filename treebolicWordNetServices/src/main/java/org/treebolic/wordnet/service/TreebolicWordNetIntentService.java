package org.treebolic.wordnet.service;

import org.treebolic.services.TreebolicIntentService;

import android.util.Log;

/**
 * Treebolic WordNet intent service
 */
public class TreebolicWordNetIntentService extends TreebolicIntentService
{
	/**
	 * Log tag
	 */
	private static final String TAG = "Treebolic WordNet IntentService"; //$NON-NLS-1$

	/**
	 * Constructor
	 *
	 * @throws Exception
	 */
	public TreebolicWordNetIntentService() throws Exception
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