package org.treebolic.owl.service;

import org.treebolic.services.TreebolicAIDLBoundService;

/**
 * Bound service for Owl data
 */
public class TreebolicOwlAIDLBoundService extends TreebolicAIDLBoundService
{
	/**
	 * Constructor
	 */
	public TreebolicOwlAIDLBoundService()
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
		return "owl:"; //$NON-NLS-1$
	}
}
