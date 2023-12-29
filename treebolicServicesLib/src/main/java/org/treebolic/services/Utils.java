/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
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

	/**
	 * Warning to run on UI thread
	 *
	 * @param context   context
	 * @param messageId massage resource id
	 */
	static void warn(@NonNull Context context, @StringRes int messageId)
	{
		new Handler(Looper.getMainLooper()).post(() -> {
			Toast.makeText(context, messageId, Toast.LENGTH_LONG).show();
		});
	}
}
