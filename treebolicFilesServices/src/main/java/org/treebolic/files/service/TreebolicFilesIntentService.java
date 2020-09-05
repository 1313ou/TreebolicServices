/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.files.service;

import org.treebolic.services.TreebolicIntentService;

import androidx.annotation.NonNull;

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
