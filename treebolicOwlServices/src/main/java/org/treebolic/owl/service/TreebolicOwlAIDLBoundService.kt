/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.owl.service;

import org.treebolic.services.TreebolicAIDLBoundService;

import androidx.annotation.NonNull;

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

	@SuppressWarnings("SameReturnValue")
	@NonNull
	@Override
	public String getUrlScheme()
	{
		return "owl:";
	}
}
