/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.treebolic.clients.iface.IModelListener;
import org.treebolic.services.iface.ITreebolicService;
import org.treebolic.services.iface.ITreebolicServiceBinder;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
import treebolic.model.Model;

/**
 * Treebolic bound service for data
 */
abstract public class TreebolicBoundService extends Service implements ITreebolicService
{
	/**
	 * Log tag
	 */
	static private final String TAG = "BoundS";

	/**
	 * Make callable
	 */
	static public Callable<Model> makeModelCallable(final String source, final String base, final String imageBase, final String settings, final IModelFactory factory)
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
	static public TaskRunner.Callback<Model> makeModelCallback(final String urlScheme, final IModelListener modelListener)
	{
		return (model) -> {

			Log.d(TAG, "Returning model " + model);
			modelListener.onModel(model, urlScheme);
		};
	}


	/**
	 * Forward callback
	 */
	static public TaskRunner.Callback<Model> makeModelForwardCallback(final WeakReference<Context> contextWeakReference, final String urlScheme, final Intent forward)
	{
		return (model) -> {

			// do not return to client but forward it to service
			IntentFactory.putModelArg(forward, model, urlScheme);
			forward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Log.d(TAG, "Forwarding model");
			final Context context = contextWeakReference.get();
			if (context != null)
			{
				context.startActivity(forward);
			}
		};
	}

	/**
	 * Model factory
	 */
	@SuppressWarnings("WeakerAccess")
	protected IModelFactory factory;

	/**
	 * Binder given to clients
	 */
	static public class TreebolicServiceBinder extends Binder implements ITreebolicServiceBinder
	{
		private final IModelFactory factory;

		private final String urlScheme;

		@NonNull
		private final WeakReference<Context> contextWeakReference;

		/**
		 * Constructor
		 */
		TreebolicServiceBinder(final IModelFactory service, final String urlScheme, final Context context)
		{
			this.factory = service;
			this.urlScheme = urlScheme;
			this.contextWeakReference = new WeakReference<>(context);
		}

		@Override
		public void makeModel(final String source, final String base, final String imageBase, final String settings, final IModelListener modelListener)
		{
			final Callable<Model> callable = makeModelCallable(source, base, imageBase, settings, this.factory);
			final TaskRunner.Callback<Model> callback = makeModelCallback(this.urlScheme, modelListener);
			TaskRunner.execute(callable, callback);
		}

		@Override
		public void makeModel(final String source, final String base, final String imageBase, final String settings, final Intent forward)
		{
			final Context context = this.contextWeakReference.get();
			if (context != null)
			{
				final Callable<Model> callable = makeModelCallable(source, base, imageBase, settings, this.factory);
				final TaskRunner.Callback<Model> callback = makeModelForwardCallback(this.contextWeakReference, this.urlScheme, forward);
				TaskRunner.execute(callable, callback);
			}
		}
	}

	/**
	 * Binder that returns an interface to the service
	 */
	@SuppressWarnings("ConstantConditions")
	private final IBinder binder = new TreebolicServiceBinder(this.factory, this.getUrlScheme(), this);

	/**
	 * Constructor
	 */
	public TreebolicBoundService()
	{
		super();
	}

	/**
	 * When binding to the service, we return an interface to the service
	 */
	@Override
	public IBinder onBind(final Intent intent)
	{
		Log.d(TreebolicBoundService.TAG, "Binding service");
		return this.binder;
	}
}
