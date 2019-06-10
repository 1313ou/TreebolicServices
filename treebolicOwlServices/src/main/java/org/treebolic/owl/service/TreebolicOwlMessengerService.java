package org.treebolic.owl.service;

import androidx.annotation.NonNull;

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
		return "owl:";
	}
}
