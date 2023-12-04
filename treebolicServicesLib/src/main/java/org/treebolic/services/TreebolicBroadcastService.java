/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.treebolic.services.iface.ITreebolicService;

import java.io.IOException;

import androidx.annotation.NonNull;
import treebolic.model.Model;
import treebolic.model.ModelDump;

/**
 * Treebolic service for handling broadcast asynchronous task requests
 */
abstract public class TreebolicBroadcastService extends BroadcastReceiver implements ITreebolicService
{
	/**
	 * Log tag
	 */
	static private final String TAG = "BroadcastS";

	/**
	 * Abstract: Model factory
	 */
	@SuppressWarnings("WeakerAccess")
	protected IModelFactory factory;

	/**
	 * Abstract: Create model factory
	 */
	@SuppressWarnings("WeakerAccess")
	abstract protected IModelFactory createModelFactory(@NonNull final Context context) throws IOException;

	/**
	 * Constructor
	 */
	public TreebolicBroadcastService()
	{
		super();
	}

	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		if (intent != null)
		{
			final String action = intent.getAction();
			if (ITreebolicService.ACTION_MAKEMODEL.equals(action))
			{
				try
				{
					this.factory = createModelFactory(context);

					final String source = intent.getStringExtra(ITreebolicService.EXTRA_SOURCE);
					final String base = intent.getStringExtra(ITreebolicService.EXTRA_BASE);
					final String imageBase = intent.getStringExtra(ITreebolicService.EXTRA_IMAGEBASE);
					final String settings = intent.getStringExtra(ITreebolicService.EXTRA_SETTINGS);
					final Intent forward = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? //
							intent.getParcelableExtra(ITreebolicService.EXTRA_FORWARD_RESULT_TO, Intent.class) : //
							intent.getParcelableExtra(ITreebolicService.EXTRA_FORWARD_RESULT_TO);
					try
					{
						final Model model = factory.make(source, base, imageBase, settings);

						// return/ forward
						if (forward == null)
						{
							// pack model
							final Bundle bundle = new Bundle();
							IntentFactory.putModelResult(bundle, model, getUrlScheme());

							// use result receiver
							final ResultReceiver resultReceiver = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? //
									intent.getParcelableExtra(ITreebolicService.EXTRA_RECEIVER, ResultReceiver.class) : //
									intent.getParcelableExtra(ITreebolicService.EXTRA_RECEIVER);
							Log.d(TAG, "Returning model " + model);
							assert resultReceiver != null;
							resultReceiver.send(0, bundle);
						}
						else
						{
							// do not return to client but forward it to service
							IntentFactory.putModelArg(forward, model, getUrlScheme());
							forward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
							Log.d(TAG, "Forwarding model");
							context.startActivity(forward);
						}
					}
					catch (@NonNull final Exception e)
					{
						Log.d(TAG, "Model factory error", e);
					}
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}
}
