/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.wordnet.service;

import android.content.Context;

import org.treebolic.services.Utils;

import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
	static private final String TAG = "WordNet Model Factory";

	/**
	 * Type of provider
	 */
	static private final boolean SIMPLE = true;

	/**
	 * Constructor
	 *
	 * @param context context
	 * //@throws Exception exception
	 */
	public ModelFactory(@NonNull final Context context) throws Exception
	{
		super(ModelFactory.SIMPLE ? new treebolic.provider.wordnet.jwi.simple.Provider() : new treebolic.provider.wordnet.jwi.full.Provider(), Utils.makeLogProviderContext(ModelFactory.TAG), makeLocator(context), null);
	}

	@Nullable
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
		catch (MalformedURLException ignored)
		{
			//
		}
		return null;
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
