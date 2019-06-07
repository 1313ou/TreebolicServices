package org.treebolic.files.service;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

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

	@SuppressWarnings("SameReturnValue")
	@NonNull
	@Override
	public String getUrlScheme()
	{
		return "directory:";
	}
}
