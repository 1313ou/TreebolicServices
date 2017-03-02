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

	@Override
	public void onCreate()
	{
		super.onCreate();
		this.factory = new ModelFactory(this);
	}

	@Override
	public String getUrlScheme()
	{
		return "directory:";
	}
}
