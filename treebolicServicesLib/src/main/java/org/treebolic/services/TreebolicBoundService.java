/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.treebolic.clients.iface.IModelListener;
import org.treebolic.services.iface.ITreebolicService;
import org.treebolic.services.iface.ITreebolicServiceBinder;

import java.lang.ref.WeakReference;

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
	static private final String TAG = "TBoundS";

	/**
	 * Make model task
	 */
	static private class MakeTask extends AbstractMakeTask
	{
		private final IModelListener modelListener;

		private MakeTask(final String source, final String base, final String imageBase, final String settings, final String urlScheme, final IModelFactory factory, final IModelListener modelListener)
		{
			super(source, base, imageBase, settings, urlScheme, factory);
			this.modelListener = modelListener;
		}

		@Override
		protected void onPostExecute(final Model model)
		{
			Log.d(TreebolicBoundService.TAG, "Returning model " + model);
			this.modelListener.onModel(model, this.urlScheme);
		}
	}

	/**
	 * Make model and forward task
	 */
	static private class MakeAndForwardTask extends AbstractMakeTask
	{
		@NonNull
		private final WeakReference<Context> contextWeakReference;

		private final Intent forward;

		private MakeAndForwardTask(final String source, final String base, final String imageBase, final String settings, final String urlScheme, final IModelFactory factory, final Context context, final Intent forward)
		{
			super(source, base, imageBase, settings, urlScheme, factory);
			this.contextWeakReference = new WeakReference<>(context);
			this.forward = forward;
		}

		@Override
		protected void onPostExecute(final Model model)
		{
			// do not return to client but forward it to service
			IntentFactory.putModelArg(this.forward, model, this.urlScheme);
			this.forward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Log.d(TreebolicBoundService.TAG, "Forwarding model");
			final Context context = this.contextWeakReference.get();
			if (context != null)
			{
				context.startActivity(this.forward);
			}
		}
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
			// make model
			final AsyncTask<Void, Void, Model> task = new MakeTask(source, base, imageBase, settings, this.urlScheme, this.factory, modelListener);
			task.execute();
		}

		@Override
		public void makeModel(final String source, final String base, final String imageBase, final String settings, final Intent forward)
		{
			final Context context = this.contextWeakReference.get();
			if (context != null)
			{
				// make model
				final AsyncTask<Void, Void, Model> task = new MakeAndForwardTask(source, base, imageBase, settings, this.urlScheme, this.factory, context, forward);
				task.execute();
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
