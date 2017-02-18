package org.treebolic.files.service;

import org.treebolic.services.TreebolicIntentService;

/**
 * Treebolic Files intent service
 */
public class TreebolicFilesIntentService extends TreebolicIntentService
{
	/**
	 * Constructor
	 *
	 * @throws Exception
	 */
	public TreebolicFilesIntentService() throws Exception
	{
		super("TreebolicFilesIntentService"); //$NON-NLS-1$
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
		return "directory:"; //$NON-NLS-1$
	}
}
