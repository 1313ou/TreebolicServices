/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.services;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import treebolic.model.Model;

/**
 * Make model task stub
 */
abstract class AbstractMakeTask extends AsyncTask<Void, Void, Model>
{
	/**
	 * Log tag
	 */
	static private final String TAG = "AMakeTask";

	private final String source;
	private final String base;
	private final String imageBase;
	private final String settings;
	private final IModelFactory factory;
	final String urlScheme;

	AbstractMakeTask(final String source, final String base, final String imageBase, final String settings, final String urlScheme, final IModelFactory factory)
	{
		this.source = source;
		this.base = base;
		this.imageBase = imageBase;
		this.settings = settings;
		this.factory = factory;
		this.urlScheme = urlScheme;
	}

	@Nullable
	@Override
	protected Model doInBackground(final Void... args)
	{
		try
		{
			return this.factory.make(this.source, this.base, this.imageBase, this.settings);
		}
		catch (@NonNull final Exception e)
		{
			Log.e(TAG, "Error making model", e);
		}
		return null;
	}
}
