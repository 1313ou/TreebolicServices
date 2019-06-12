/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
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
		super("TreebolicOwlIntentService");
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
