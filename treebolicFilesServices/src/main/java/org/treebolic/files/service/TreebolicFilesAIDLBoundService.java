package org.treebolic.files.service;

import androidx.annotation.NonNull;

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

	@SuppressWarnings("SameReturnValue")
	@NonNull
	@Override
	public String getUrlScheme()
	{
		return "directory:";
	}
}
