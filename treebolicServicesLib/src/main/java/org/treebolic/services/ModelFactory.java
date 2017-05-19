package org.treebolic.services;

import android.content.Context;
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
	final IProvider provider;

	/**
	 * Provider locatorContext
	 */
	final IProviderContext providerContext;

	/**
	 * Context
	 */
	final ILocator locatorContext;

	/**
	 * Context
	 */
	final Context applicationContext;

	/**
	 * Constructor
	 *
	 * @param provider0           provider
	 * @param providerContext0    provider context
	 * @param locatorContext0     locator context
	 * @param applicationContext0 application context
	 */
	public ModelFactory(final IProvider provider0, final IProviderContext providerContext0, final ILocator locatorContext0, final Context applicationContext0)
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
	private static URL makeBaseURL(final String base)
	{
		try
		{
			return new URL(base != null && !base.endsWith("/") ? base + "/" : base);
		}
		catch (final MalformedURLException e)
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
	private Properties makeParameters(final String source, final String base, final String imageBase, final String settings)
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
