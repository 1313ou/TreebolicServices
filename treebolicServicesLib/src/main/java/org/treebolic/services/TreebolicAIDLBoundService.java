/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.services;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.TransactionTooLargeException;
import android.util.Log;
import android.widget.Toast;

import org.treebolic.services.iface.ITreebolicAIDLService;
import org.treebolic.services.iface.ITreebolicService;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
import treebolic.model.Model;

/**
 * Treebolic bound service for data
 */
abstract public class TreebolicAIDLBoundService extends Service implements ITreebolicService
{
	/**
	 * Log tag
	 */
	static private final String TAG = "AidlBoundS";

	/**
	 * Make callable
	 */
	@NonNull
	static public Callable<Model> makeModelCallable(final String source, final String base, final String imageBase, final String settings, @NonNull final IModelFactory factory)
	{
		return () -> {

			try
			{
				return factory.make(source, base, imageBase, settings);
			}
			catch (@NonNull final Exception e)
			{
				Log.e(TAG, "Error making model", e);
			}
			return null;
		};
	}

	/**
	 * Return callback
	 */
	@NonNull
	static public TaskRunner.Callback<Model> makeModelCallback(final String urlScheme, @NonNull final ResultReceiver resultReceiver)
	{
		return (model) -> {

			// pack model
			final Bundle bundle = new Bundle();
			IntentFactory.putModelResult(bundle, model, urlScheme);

			// use result receiver
			Log.d(TAG, "Returning model " + model);
			resultReceiver.send(0, bundle);
		};
	}


	/**
	 * Forward callback
	 */
	@NonNull
	static public TaskRunner.Callback<Model> makeModelForwardCallback(@NonNull final WeakReference<Context> contextWeakReference, final String urlScheme, @NonNull final Intent forward)
	{
		return (model) -> {

			// do not return to client but forward it to service
			IntentFactory.putModelArg(forward, model, urlScheme);
			forward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Log.d(TAG, "Forwarding model");
			final Context context = contextWeakReference.get();
			if (context != null)
			{
				try
				{
					context.startActivity(forward);
				}
				catch (ActivityNotFoundException anfe)
				{
					Toast.makeText(context, R.string.activity_not_found, Toast.LENGTH_LONG).show();
				}
				catch (RuntimeException rte)
				{
					if (rte.getCause() instanceof TransactionTooLargeException)
					{
						Toast.makeText(context, R.string.transaction_too_large, Toast.LENGTH_LONG).show();
					}
					else
					{
						throw rte;
					}
				}
			}
		};
	}

	/**
	 * Model factory
	 */
	@SuppressWarnings("WeakerAccess")
	protected IModelFactory factory;

	/**
	 * Binder that returns an interface to the service
	 */
	private final ITreebolicAIDLService.Stub binder = new ITreebolicAIDLService.Stub()
	{
		@Override
		public void makeModel(final String source, final String base, final String imageBase, final String settings, @NonNull final ResultReceiver resultReceiver)
		{
			final Callable<Model> callable = makeModelCallable(source, base, imageBase, settings, TreebolicAIDLBoundService.this.factory);
			final TaskRunner.Callback<Model> callback = makeModelCallback(getUrlScheme(), resultReceiver);
			TaskRunner.execute(callable, callback);
		}

		@Override
		public void makeAndForwardModel(final String source, final String base, final String imageBase, final String settings, @NonNull final Intent forward)
		{
			final Callable<Model> callable = makeModelCallable(source, base, imageBase, settings, TreebolicAIDLBoundService.this.factory);
			final TaskRunner.Callback<Model> callback = makeModelForwardCallback(new WeakReference<>(TreebolicAIDLBoundService.this), getUrlScheme(), forward);
			TaskRunner.execute(callable, callback);
		}
	};

	/**
	 * Constructor
	 */
	public TreebolicAIDLBoundService()
	{
		//
	}

	/**
	 * When binding to the service, we return an interface to the service
	 */
	@NonNull
	@Override
	public IBinder onBind(final Intent intent)
	{
		Log.d(TAG, "Binding service");
		return this.binder;
	}
}
