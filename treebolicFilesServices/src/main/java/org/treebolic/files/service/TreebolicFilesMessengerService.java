package org.treebolic.files.service;

import org.treebolic.services.TreebolicMessengerService;

/**
 * Treebolic Files messenger bound service
 *
 * @author Bernard Bou
 */
public class TreebolicFilesMessengerService extends TreebolicMessengerService
{
	/**
	 * Constructor
	 *
	 * @throws Exception
	 */
	public TreebolicFilesMessengerService() throws Exception
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
