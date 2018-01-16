package org.treebolic.owl.service;

import android.content.Context;

import org.treebolic.services.Utils;

import java.net.MalformedURLException;
import java.net.URL;

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
	static private final String TAG = "OWL Model Factory";

	/**
	 * Constructor
	 *
	 * @param context
	 *            context
	 */
	public ModelFactory(final Context context)
	{
		super(new Provider(), Utils.makeLogProviderContext(ModelFactory.TAG), makeLocator(context), null);
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
		catch (MalformedURLException ignored)
		{
			//
		}
		return null;
	}

	@Override
	protected String[] getSourceAliases()
	{
		return new String[] { "owl" };
	}
}
