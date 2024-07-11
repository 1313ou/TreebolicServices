/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.owl.service;

import android.content.Context;
import android.util.Log;

import org.treebolic.services.Utils;
import org.treebolic.storage.Storage;

import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.NonNull;
import treebolic.ILocator;
import treebolic.provider.owl.Provider2;

/**
 * Owl Model factory
 *
 * @author Bernard Bou
 */
public class ModelFactory extends org.treebolic.services.ModelFactory
{
	/**
	 * Log tag
	 */
	static private final String TAG = "OwlModelFactory";

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
		try
		{
			return new ILocator()
			{
				// Used when the base url is null
				// private final URL base = context.getFilesDir().toURI().toURL();
				private final URL base = Storage.getTreebolicStorage(context.getApplicationContext()).toURI().toURL();

				@Override
				public URL getBase()
				{
					return this.base;
				}

				@Override
				public URL getImagesBase()
				{
					return this.base;
				}
			};
		}
		catch (MalformedURLException mue)
		{
			Log.e(TAG, context.getFilesDir().toURI().toString(), mue);
			throw new RuntimeException(mue);
		}
	}

	@NonNull
	@Override
	protected String[] getSourceAliases()
	{
		return new String[]{"owl"};
	}
}
