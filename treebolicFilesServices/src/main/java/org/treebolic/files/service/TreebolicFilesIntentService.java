package org.treebolic.files.service;

import android.support.annotation.NonNull;

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

	@SuppressWarnings("SameReturnValue")
	@NonNull
	@Override
	public String getUrlScheme()
	{
		return "directory:";
	}
}
