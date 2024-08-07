/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.wordnet.service;

import android.content.Context;
import android.util.Log;

import org.treebolic.services.Utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.NonNull;
import treebolic.ILocator;

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
	static private final String TAG = "WnSModelFactory";

	/**
	 * Type of provider
	 */
	static private final boolean SIMPLE = true;

	/**
	 * Constructor
	 *
	 * @param context context
	 *                //@throws Exception exception
	 */
	public ModelFactory(@NonNull final Context context) throws IOException
	{
		super(ModelFactory.SIMPLE ? new treebolic.provider.wordnet.jwi.simple.Provider() : new treebolic.provider.wordnet.jwi.full.Provider(), Utils.makeLogProviderContext(TAG), makeLocator(context), null);
	}

	@NonNull
	static private ILocator makeLocator(@NonNull final Context context)
	{
		try
		{
			return new ILocator()
			{
				private final URL base = context.getFilesDir().toURI().toURL();

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

	@SuppressWarnings("SameReturnValue")
	@Override
	protected boolean useFilesDir()
	{
		return true;
	}

	@NonNull
	@Override
	protected String[] getSourceAliases()
	{
		return new String[]{"word"};
	}
}
