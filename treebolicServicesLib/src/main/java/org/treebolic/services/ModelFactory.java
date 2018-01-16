package org.treebolic.services;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

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
	static private final String TAG = "Model Factory";

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

		// model
		final Model model = this.provider.makeModel(source, ModelFactory.makeBaseURL(base), makeParameters(source, base, imageBase, settings));
		Log.d(ModelFactory.TAG, "model=" + model);
		return model;
	}

	/**
	 * Make base URL
	 *
	 * @param base base
	 * @return base URL
	 */
	private static URL makeBaseURL(@Nullable final String base)
	{
		try
		{
			return new URL(base != null && !base.endsWith("/") ? base + "/" : base);
		}
		catch (@NonNull final MalformedURLException ignored)
		{
			//
		}
		return null;
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
	@NonNull
	private Properties makeParameters(@Nullable final String source, @Nullable final String base, @Nullable final String imageBase, @Nullable final String settings)
	{
		final Properties theseParameters = new Properties();
		if (source != null)
		{
			theseParameters.setProperty("source", source);
		}
		if (base != null)
		{
			theseParameters.setProperty("base", base);
		}
		if (imageBase != null)
		{
			theseParameters.setProperty("imagebase", imageBase);
		}
		if (settings != null)
		{
			theseParameters.setProperty("settings", settings);
		}

		// source aliases
		final String[] sourceAliases = getSourceAliases();
		if (source != null && sourceAliases != null)
		{
			for (final String sourceAlias : sourceAliases)
			{
				theseParameters.setProperty(sourceAlias, source);
			}
		}

		return theseParameters;
	}
}
