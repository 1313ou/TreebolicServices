package org.treebolic.files.service;

import org.treebolic.services.TreebolicIntentService;

/**
 * Treebolic Files intent service
 */
public class TreebolicFilesIntentService extends TreebolicIntentService
{
	/**
	 * Constructor
	 */
	public TreebolicFilesIntentService()
	{
		super("TreebolicFilesIntentService");
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
