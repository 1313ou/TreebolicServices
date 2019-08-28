/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.services;

import android.util.Log;

import androidx.annotation.NonNull;
import treebolic.provider.IProviderContext;

@SuppressWarnings("WeakerAccess")
public class Utils
{
	/**
	 * Make provider locatorContext
	 *
	 * @return provider locatorContext
	 */
	@NonNull
	public static IProviderContext makeLogProviderContext(final String tag)
	{
		return new IProviderContext()
		{
			@Override
			public void message(final String text)
			{
				Log.d(tag, "Message:" + text);
				// Toast.makeText(locatorContext, text, Toast.LENGTH_LONG).show();
			}

			@Override
			public void progress(final String text, final boolean fail)
			{
				Log.d(tag, "Progress:" + text);
				// Toast.makeText(locatorContext, text, Toast.LENGTH_LONG).show();
			}

			@Override
			public void warn(final String text)
			{
				Log.d(tag, "Warn:" + text);
				// Toast.makeText(locatorContext, text, Toast.LENGTH_LONG).show();
			}
		};
	}
}
