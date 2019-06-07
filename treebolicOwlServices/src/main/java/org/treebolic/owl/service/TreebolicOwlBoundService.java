package org.treebolic.owl.service;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import org.treebolic.services.TreebolicBoundService;

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
