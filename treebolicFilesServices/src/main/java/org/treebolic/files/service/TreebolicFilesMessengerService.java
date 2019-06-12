/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.files.service;

import org.treebolic.services.TreebolicMessengerService;

import androidx.annotation.NonNull;

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
