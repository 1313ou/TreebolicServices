/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.owl.service;

import android.content.Context;

import org.treebolic.services.Utils;
import org.treebolic.storage.Storage;

import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.NonNull;
import treebolic.ILocator;
import treebolic.provider.owl.owlapi.Provider;

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
		super(new Provider(), Utils.makeLogProviderContext(ModelFactory.TAG), makeLocator(context), null);
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
		catch (MalformedURLException ignored)
		{
			//
		}
		return null;
	}

	@NonNull
	@Override
	protected String[] getSourceAliases()
	{
		return new String[]{"owl"};
	}
}
