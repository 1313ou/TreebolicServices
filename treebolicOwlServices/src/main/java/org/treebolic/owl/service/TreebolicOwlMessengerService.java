package org.treebolic.owl.service;

import org.treebolic.services.TreebolicMessengerService;

/**
 * Treebolic Owl bound messenger service
 *
 * @author Bernard Bou
 */
public class TreebolicOwlMessengerService extends TreebolicMessengerService
{
	/**
	 * Constructor
	 */
	public TreebolicOwlMessengerService()
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
