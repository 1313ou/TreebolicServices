package org.treebolic.owl.service;

import java.net.MalformedURLException;
import java.net.URL;

import org.treebolic.services.Utils;

import android.content.Context;
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
	static private final String TAG = "OWL Model Factory"; //$NON-NLS-1$

	/**
	 * Constructor
	 *
	 * @param context
	 *            context
	 * @throws MalformedURLException
	 * @throws Exception
	 */
	public ModelFactory(final Context context)
	{
		super(new Provider(), Utils.makeLogProviderContext(ModelFactory.TAG), makeLocator(context));
	}

	static private ILocator makeLocator(final Context context)
	{
		try
		{
			return new ILocator()
			{
				private URL thisBase = context.getFilesDir().toURI().toURL();

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
	 * @see org.treebolic.services.ModelFactory#getSourceAliases()
	 */
	@Override
	protected String[] getSourceAliases()
	{
		return new String[] { "owl" }; //$NON-NLS-1$
	}
}
