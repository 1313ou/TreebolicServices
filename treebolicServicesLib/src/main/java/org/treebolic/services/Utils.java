package org.treebolic.services;

import treebolic.provider.IProviderContext;
import android.util.Log;

public class Utils
{
	/**
	 * Make provider locator
	 *
	 * @return provider locator
	 */
	public static IProviderContext makeLogProviderContext(final String tag)
	{
		return new IProviderContext()
		{
			/*
			 * (non-Javadoc)
			 *
			 * @see treebolic.provider.IProviderContext#message(java.lang.String)
			 */
			@Override
			public void message(final String text)
			{
				Log.d(tag, "Message:" + text);
				// Toast.makeText(locator, text, Toast.LENGTH_LONG).show();
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see treebolic.provider.IProviderContext#putProgress(java.lang.String, boolean)
			 */
			@Override
			public void progress(final String text, final boolean fail)
			{
				Log.d(tag, "Progress:" + text);
				// Toast.makeText(locator, text, Toast.LENGTH_LONG).show();
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see treebolic.provider.IProviderContext#warn(java.lang.String)
			 */
			@Override
			public void warn(final String text)
			{
				Log.d(tag, "Warn:" + text);
				// Toast.makeText(locator, text, Toast.LENGTH_LONG).show();
			}
		};
	}
}
