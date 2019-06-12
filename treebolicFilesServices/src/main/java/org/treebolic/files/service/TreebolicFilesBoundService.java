/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.files.service;

import android.annotation.SuppressLint;

import org.treebolic.services.TreebolicBoundService;

import androidx.annotation.NonNull;

/**
 * Bound service for Files data
 */
@SuppressLint("Registered")
public class TreebolicFilesBoundService extends TreebolicBoundService
{
	/**
	 * Constructor
	 */
	public TreebolicFilesBoundService()
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
