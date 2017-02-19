package org.treebolic.files.service;

import org.treebolic.services.TreebolicAIDLBoundService;

/**
 * Bound service for Files data
 */
public class TreebolicFilesAIDLBoundService extends TreebolicAIDLBoundService
{
	/**
	 * Constructor
	 */
	public TreebolicFilesAIDLBoundService()
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
