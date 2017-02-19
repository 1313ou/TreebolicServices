package org.treebolic.files.service;

import android.annotation.SuppressLint;

import org.treebolic.services.TreebolicBoundService;

/**
 * Bound service for Files data
 */
@SuppressLint("Registered")
public class TreebolicFilesBoundService extends TreebolicBoundService
{
	/**
	 * Constructor
	 */
	public TreebolicFilesBoundService()
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
