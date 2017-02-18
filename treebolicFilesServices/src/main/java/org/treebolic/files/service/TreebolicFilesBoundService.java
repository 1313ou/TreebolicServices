package org.treebolic.files.service;

import org.treebolic.services.TreebolicBoundService;

/**
 * Bound service for Files data
 */
public class TreebolicFilesBoundService extends TreebolicBoundService
{
	/**
	 * Constructor
	 *
	 * @throws Exception
	 */
	public TreebolicFilesBoundService() throws Exception
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
