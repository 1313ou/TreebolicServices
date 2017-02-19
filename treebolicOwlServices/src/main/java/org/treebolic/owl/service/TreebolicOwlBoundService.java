package org.treebolic.owl.service;

import android.annotation.SuppressLint;

import org.treebolic.services.TreebolicBoundService;

/**
 * Bound service for Owl data
 */
@SuppressLint("Registered")
public class TreebolicOwlBoundService extends TreebolicBoundService
{
	/**
	 * Constructor
	 */
	public TreebolicOwlBoundService()
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
