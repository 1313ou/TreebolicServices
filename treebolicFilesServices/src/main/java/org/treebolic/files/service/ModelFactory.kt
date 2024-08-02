/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.files.service;

import android.content.Context;

import org.treebolic.services.Utils;

import java.net.URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import treebolic.ILocator;
import treebolic.provider.files.Provider2;

/**
 * Model factory
 *
 * @author Bernard Bou
 */
public class ModelFactory extends org.treebolic.services.ModelFactory
{
	/**
	 * Log tag
	 */
	static private final String TAG = "FilesModelFactory";

	/**
	 * Constructor
	 *
	 * @param context context
	 */
	public ModelFactory(@NonNull final Context context)
	{
		super(new Provider2(), Utils.makeLogProviderContext(TAG), makeLocator(context), null);
	}

	@NonNull
	static private ILocator makeLocator(@NonNull final Context context)
	{
		// Not used
		return new ILocator()
		{
			@Nullable
			@Override
			public URL getBase()
			{
				return null;
			}

			@Nullable
			@Override
			public URL getImagesBase()
			{
				return null;
			}
		};
	}

	@NonNull
	@Override
	protected String[] getSourceAliases()
	{
		return new String[]{"files"};
	}
}
