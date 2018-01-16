package org.treebolic.owl.service;

import android.support.annotation.NonNull;

import org.treebolic.services.TreebolicAIDLBoundService;

/**
 * Bound service for Owl data
 */
public class TreebolicOwlAIDLBoundService extends TreebolicAIDLBoundService
{
	/**
	 * Constructor
	 */
	public TreebolicOwlAIDLBoundService()
	{
		super();
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		this.factory = new ModelFactory(this);
	}

	@NonNull
	@Override
	public String getUrlScheme()
	{
		return "owl:";
	}
}
