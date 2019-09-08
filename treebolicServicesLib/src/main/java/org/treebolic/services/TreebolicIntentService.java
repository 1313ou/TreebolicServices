/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.treebolic.services.iface.ITreebolicService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import treebolic.model.Model;
import treebolic.model.ModelDump;

/**
 * Treebolic service for handling asynchronous task requests in a service on a separate handler thread
 */
abstract public class TreebolicIntentService extends IntentService implements ITreebolicService
{
	/**
	 * Log tag
	 */
	static private final String TAG = "IntentS";

	/**
	 * Abstract: Model factory
	 */
	@SuppressWarnings("WeakerAccess")
	protected IModelFactory factory;

	/**
	 * Constructor
	 */
	public TreebolicIntentService(final String name)
	{
		super(name);
	}

	@Override
	protected void onHandleIntent(@Nullable final Intent intent)
	{
		if (intent != null)
		{
			final String action = intent.getAction();
			if (ITreebolicService.ACTION_MAKEMODEL.equals(action))
			{
				final String source = intent.getStringExtra(ITreebolicService.EXTRA_SOURCE);
				final String base = intent.getStringExtra(ITreebolicService.EXTRA_BASE);
				final String imageBase = intent.getStringExtra(ITreebolicService.EXTRA_IMAGEBASE);
				final String settings = intent.getStringExtra(ITreebolicService.EXTRA_SETTINGS);
				final Intent forward = intent.getParcelableExtra(ITreebolicService.EXTRA_FORWARD_RESULT_TO);

				try
				{
					final Model model = this.factory.make(source, base, imageBase, settings);
					Log.d(TAG, "model(service)=" + (model != null ? ModelDump.toString(model) : "null"));

					// return/ forward
					if (forward == null)
					{
						// pack model
						final Bundle bundle = new Bundle();
						IntentFactory.putModelResult(bundle, model, getUrlScheme());

						// use result receiver
						final ResultReceiver resultReceiver = intent.getParcelableExtra(ITreebolicService.EXTRA_RECEIVER);
						Log.d(TreebolicIntentService.TAG, "Returning model " + model);
						resultReceiver.send(0, bundle);
					}
					else
					{
						// do not return to client but forward it to service
						IntentFactory.putModelArg(forward, model, getUrlScheme());
						forward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						Log.d(TreebolicIntentService.TAG, "Forwarding model");
						startActivity(forward);
					}
				}
				catch (@NonNull final Exception e)
				{
					Log.d(TreebolicIntentService.TAG, "Model factory error", e);
				}
			}
		}
	}
}
