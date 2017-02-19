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
	private static final String TAG = "TWordNetMessengerS"; //$NON-NLS-1$

	/**
	 * Constructor
	 */
	public TreebolicWordNetMessengerService()
	{
		super();
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
			Log.e(TreebolicWordNetMessengerService.TAG, "Model factory constructor failed", e); //$NON-NLS-1$
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
