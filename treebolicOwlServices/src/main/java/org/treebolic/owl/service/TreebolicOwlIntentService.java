package org.treebolic.owl.service;

import android.support.annotation.NonNull;

import org.treebolic.services.TreebolicIntentService;

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

	@NonNull
	@Override
	public String getUrlScheme()
	{
		return "owl:";
	}
}
