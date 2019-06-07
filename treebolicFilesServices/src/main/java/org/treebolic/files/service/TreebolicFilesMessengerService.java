package org.treebolic.files.service;

import android.support.annotation.NonNull;

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
	 */
	public TreebolicFilesMessengerService()
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
