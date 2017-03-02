package org.treebolic.files.service;

import android.annotation.SuppressLint;

import org.treebolic.services.TreebolicBoundService;

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

	@Override
	public String getUrlScheme()
	{
		return "directory:";
	}
}
