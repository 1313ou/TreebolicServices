/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.owl.service;

import android.content.Context;

import org.treebolic.services.IModelFactory;
import org.treebolic.services.TreebolicBroadcastService;

import java.io.IOException;

import androidx.annotation.NonNull;

/**
 * Treebolic Owl broadcast service
 */
public class TreebolicOwlBroadcastService extends TreebolicBroadcastService
{
	/**
	 * Constructor
	 */
	public TreebolicOwlBroadcastService()
	{
		super();
	}

	@Override
	protected IModelFactory createModelFactory(@NonNull final Context context) throws IOException
	{
		return new ModelFactory(context);
	}

	@SuppressWarnings("SameReturnValue")
	@NonNull
	@Override
	public String getUrlScheme()
	{
		return "owl:";
	}
}
