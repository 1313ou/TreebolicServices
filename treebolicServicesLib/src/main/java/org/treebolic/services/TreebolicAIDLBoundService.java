package org.treebolic.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import android.util.Log;

import org.treebolic.services.iface.ITreebolicAIDLService;
import org.treebolic.services.iface.ITreebolicService;

import java.lang.ref.WeakReference;

import treebolic.model.Model;

/**
 * Treebolic bound service for data
 */
abstract public class TreebolicAIDLBoundService extends Service implements ITreebolicService
{
	/**
	 * Log tag
	 */
	static private final String TAG = "TAIDLBoundS";

	/**
	 * Make model task
	 */
	static private class MakeTask extends AbstractMakeTask
	{
		private final ResultReceiver resultReceiver;

		private MakeTask(final String source, final String base, final String imageBase, final String settings, final String urlScheme, final IModelFactory factory, final ResultReceiver resultReceiver)
		{
			super(source, base, imageBase, settings, urlScheme, factory);
			this.resultReceiver = resultReceiver;
		}

		@Override
		protected void onPostExecute(final Model model)
		{
			// pack model
			final Bundle bundle = new Bundle();
			IntentFactory.putModelResult(bundle, model, this.urlScheme);

			// use result receiver
			Log.d(TreebolicAIDLBoundService.TAG, "Returning model " + model);
			this.resultReceiver.send(0, bundle);
		}
	}

	/**
	 * Make model and forward task
	 */
	static private class MakeAndForwardTask extends AbstractMakeTask
	{
		@NonNull
		final WeakReference<Context> contextWeakReference;

		final Intent forward;

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
			Log.d(TreebolicAIDLBoundService.TAG, "Forwarding model");
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
	 * Binder that returns an interface to the service
	 */
	private final ITreebolicAIDLService.Stub binder = new ITreebolicAIDLService.Stub()
	{
		@Override
		public void makeModel(final String source, final String base, final String imageBase, final String settings, final ResultReceiver resultReceiver)
		{
			new MakeTask(source, base, imageBase, settings, TreebolicAIDLBoundService.this.getUrlScheme(), TreebolicAIDLBoundService.this.factory, resultReceiver).execute();
		}

		@Override
		public void makeAndForwardModel(final String source, final String base, final String imageBase, final String settings, final Intent forward)
		{
			// make model
			new MakeAndForwardTask(source, base, imageBase, settings, TreebolicAIDLBoundService.this.getUrlScheme(), TreebolicAIDLBoundService.this.factory, TreebolicAIDLBoundService.this, forward).execute();
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
	@Override
	public IBinder onBind(final Intent intent)
	{
		Log.d(TreebolicAIDLBoundService.TAG, "Binding service");
		return this.binder;
	}
}
