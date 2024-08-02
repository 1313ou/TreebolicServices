/*
 * Copyright (c) 2023. Bernard Bou
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
		this.factory = new ModelFactory(this);
		super.onCreate();
	}

	@SuppressWarnings("SameReturnValue")
	@NonNull
	@Override
	public String getUrlScheme()
	{
		return "owl:";
	}
}
