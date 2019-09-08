/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.services;

import android.content.Context;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import treebolic.ILocator;
import treebolic.model.Model;
import treebolic.provider.IProvider;
import treebolic.provider.IProviderContext;

/**
 * Model factory
 *
 * @author Bernard Bou
 */
public abstract class ModelFactory implements IModelFactory
{
	/**
	 * Log tag
	 */
	static private final String TAG = "AModelFactory";

	/**
	 * Provider
	 */
	@SuppressWarnings("WeakerAccess")
	final protected IProvider provider;

	/**
	 * Provider locatorContext
	 */
	@SuppressWarnings("WeakerAccess")
	final protected IProviderContext providerContext;

	/**
	 * Context
	 */
	@SuppressWarnings("WeakerAccess")
	final protected ILocator locatorContext;

	/**
	 * Context
	 */
	@SuppressWarnings("WeakerAccess")
	final protected Context applicationContext;

	/**
	 * Constructor
	 *
	 * @param provider0           provider
	 * @param providerContext0    provider contextWeakReference
	 * @param locatorContext0     locator contextWeakReference
	 * @param applicationContext0 application contextWeakReference
	 */
	@SuppressWarnings("WeakerAccess")
	public ModelFactory(final IProvider provider0, final IProviderContext providerContext0, final ILocator locatorContext0, @SuppressWarnings("SameParameterValue") final Context applicationContext0)
	{
		super();
		this.provider = provider0;
		this.providerContext = providerContext0;
		this.locatorContext = locatorContext0;
		this.applicationContext = applicationContext0;
	}

	/**
	 * Source aliases
	 *
	 * @return array of aliases for source
	 */
	@SuppressWarnings({"WeakerAccess", "SameReturnValue"})
	@Nullable
	protected String[] getSourceAliases()
	{
		return null;
	}

	/**
	 * What is returned by provider locatorContext getDataDir()
	 *
	 * @return true if locatorContext.getFilesDir, false if base
	 */
	@SuppressWarnings("SameReturnValue")
	protected boolean useFilesDir()
	{
		return false;
	}

	@Nullable
	@Override
	public Model make(final String source, final String base, final String imageBase, final String settings)
	{
		// provider
		this.provider.setContext(this.providerContext);
		this.provider.setLocator(this.locatorContext);
		this.provider.setHandle(this.applicationContext);

		URL baseUrl = makeBaseURL(base);

		// model
		final Model model = this.provider.makeModel(source, baseUrl, makeParameters(source, base, imageBase, settings));
		Log.d(ModelFactory.TAG, "model=" + model);
		return model;
	}

	/**
	 * Make base URL
	 *
	 * @param base base
	 * @return base URL
	 */
	@SuppressWarnings("WeakerAccess")
	@Nullable
	protected URL makeBaseURL(@Nullable final String base)
	{
		if (base == null)
		{
			return this.locatorContext.getBase();
		}
		else
		{
			try
			{
				return new URL(!base.endsWith("/") ? base + "/" : base);
			}
			catch (@NonNull final MalformedURLException ignored)
			{
				//
			}
			return null;
		}
	}

	/**
	 * Make parameters
	 *
	 * @param source    source
	 * @param base      base
	 * @param imageBase image base
	 * @param settings  settings
	 * @return parameters
	 */
	@SuppressWarnings("WeakerAccess")
	@NonNull
	protected Properties makeParameters(@Nullable final String source, @Nullable final String base, @Nullable final String imageBase, @Nullable final String settings)
	{
		final Properties parameters = new Properties();
		if (source != null)
		{
			parameters.setProperty("source", source);
		}
		if (base != null)
		{
			parameters.setProperty("base", base);
		}
		if (imageBase != null)
		{
			parameters.setProperty("imagebase", imageBase);
		}
		if (settings != null)
		{
			parameters.setProperty("settings", settings);
		}

		// source aliases
		final String[] sourceAliases = getSourceAliases();
		if (source != null && sourceAliases != null)
		{
			for (final String sourceAlias : sourceAliases)
			{
				parameters.setProperty(sourceAlias, source);
			}
		}

		return parameters;
	}
}
