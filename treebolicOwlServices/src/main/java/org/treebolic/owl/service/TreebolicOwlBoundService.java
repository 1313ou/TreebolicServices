/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.owl.service;

import android.annotation.SuppressLint;

import org.treebolic.services.TreebolicBoundService;

import androidx.annotation.NonNull;

/**
 * Bound service for Owl data
 */
@SuppressLint("Registered")
public class TreebolicOwlBoundService extends TreebolicBoundService
{
	/**
	 * Constructor
	 */
	public TreebolicOwlBoundService()
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
