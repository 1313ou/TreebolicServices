/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.owl.service;

import org.treebolic.services.TreebolicMessengerService;

import androidx.annotation.NonNull;

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
