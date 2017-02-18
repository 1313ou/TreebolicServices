package org.treebolic.files.service;

import java.net.MalformedURLException;
import java.net.URL;

import org.treebolic.services.Utils;

import android.content.Context;
import treebolic.ILocator;
import treebolic.provider.files.Provider;

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
	static private final String TAG = "Files Model Factory"; //$NON-NLS-1$

	/**
	 * Constructor
	 *
	 * @param context
	 *            context
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
		return new String[] { "files" }; //$NON-NLS-1$
	}
}
