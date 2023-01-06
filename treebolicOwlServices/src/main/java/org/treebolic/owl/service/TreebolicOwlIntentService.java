/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.owl.service;

import org.treebolic.services.TreebolicIntentService;

import androidx.annotation.NonNull;

/**
 * Treebolic Owl intent service
 */
public class TreebolicOwlIntentService extends TreebolicIntentService
{
	/**
	 * Constructor
	 */
	public TreebolicOwlIntentService()
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
