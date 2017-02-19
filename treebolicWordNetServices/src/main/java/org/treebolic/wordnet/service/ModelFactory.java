package org.treebolic.wordnet.service;

import android.content.Context;

import org.treebolic.services.Utils;

import java.net.MalformedURLException;
import java.net.URL;

import treebolic.ILocator;

/**
 * Model factory
 *
 * @author Bernard Bou
 */
/**
 * @author bbou
 */
public class ModelFactory extends org.treebolic.services.ModelFactory
{
	/**
	 * Log tag
	 */
	static private final String TAG = "WordNet Model Factory"; //$NON-NLS-1$

	/**
	 * Type of provider
	 */
	static private final boolean SIMPLE = true;

	/**
	 * Constructor
	 *
	 * @param context
	 *            context
	 * @throws Exception
	 */
	public ModelFactory(final Context context) throws Exception
	{
		//noinspection ConstantConditions
		super(ModelFactory.SIMPLE ? new treebolic.provider.wordnet.jwi.simple.Provider() : new treebolic.provider.wordnet.jwi.full.Provider(),
				Utils.makeLogProviderContext(ModelFactory.TAG), makeLocator(context));
	}

	static private ILocator makeLocator(final Context context)
	{
		try
		{
			return new ILocator()
			{
				private final URL thisBase = context.getFilesDir().toURI().toURL();

				@Override
				public URL getBase()
				{
					return this.thisBase;
				}

				@Override
				public URL getImagesBase()
				{
					return this.thisBase;
				}
			};
		}
		catch (MalformedURLException e)
		{
			//
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.treebolic.services.ModelFactory#useFilesDir()
	 */
	@Override
	protected boolean useFilesDir()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.treebolic.services.ModelFactory#getSourceAliases()
	 */
	@Override
	protected String[] getSourceAliases()
	{
		return new String[] { "word" }; //$NON-NLS-1$
	}
}
