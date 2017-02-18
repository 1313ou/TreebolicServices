package org.treebolic.owl.service;

import org.treebolic.services.TreebolicIntentService;

/**
 * Treebolic Owl intent service
 */
public class TreebolicOwlIntentService extends TreebolicIntentService
{
	/**
	 * Constructor
	 */
	public TreebolicOwlIntentService()
	{
		super("TreebolicOwlIntentService"); //$NON-NLS-1$
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
		this.factory = new ModelFactory(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.treebolic.services.iface.ITreebolicService#getUrlScheme()
	 */
	@Override
	public String getUrlScheme()
	{
		return "owl:"; //$NON-NLS-1$
	}
}
