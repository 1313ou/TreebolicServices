package org.treebolic.services;

import android.util.Log;

import treebolic.provider.IProviderContext;

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
			@Override
			public void message(final String text)
			{
				Log.d(tag, "Message:" + text);
				// Toast.makeText(locator, text, Toast.LENGTH_LONG).show();
			}

			@Override
			public void progress(final String text, final boolean fail)
			{
				Log.d(tag, "Progress:" + text);
				// Toast.makeText(locator, text, Toast.LENGTH_LONG).show();
			}

			@Override
			public void warn(final String text)
			{
				Log.d(tag, "Warn:" + text);
				// Toast.makeText(locator, text, Toast.LENGTH_LONG).show();
			}
		};
	}
}
