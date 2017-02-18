package org.treebolic.services;

import org.treebolic.services.iface.IModelFactory;
import org.treebolic.services.iface.ITreebolicAIDLService;
import org.treebolic.services.iface.ITreebolicService;

import treebolic.model.Model;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Treebolic bound service for data
 */
abstract public class TreebolicAIDLBoundService extends Service implements ITreebolicService
{
	/**
	 * Log tag
	 */
	static private final String TAG = "Treebolic AIDL Bound Service";

	/**
	 * Model factory
	 */
	protected IModelFactory factory;

	/**
	 * Binder that returns an interface to the service
	 */
	private final ITreebolicAIDLService.Stub binder = new ITreebolicAIDLService.Stub()
	{
		/*
		 * (non-Javadoc)
		 *
		 * @see org.treebolic.services.iface.ITreebolicAIDLService#makeModel(java.lang.String, java.lang.String, java.lang.String, java.lang.String,
		 * android.os.ResultReceiver)
		 */
		@Override
		public void makeModel(final String source, final String base, final String imageBase, final String settings, final ResultReceiver resultReceiver)
				throws RemoteException
		{
			// make model
			new AsyncTask<Void, Void, Model>()
			{
				@Override
				protected Model doInBackground(final Void... args)
				{
					try
					{
						// make model
						return TreebolicAIDLBoundService.this.factory.make(source, base, imageBase, settings);
					}
					catch (final Exception e)
					{
						Log.e(TreebolicAIDLBoundService.TAG, "Error making model", e);
					}
					return null;
				}

				@Override
				protected void onPostExecute(final Model model)
				{
					// pack model
					final Bundle bundle = new Bundle();
					IntentFactory.putModelResult(bundle, model, TreebolicAIDLBoundService.this.getUrlScheme());

					// use result receiver
					Log.d(TreebolicAIDLBoundService.TAG, "Returning model " + model);
					resultReceiver.send(0, bundle);
				}
			}.execute();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.treebolic.services.iface.ITreebolicAIDLServiceBinder#makeAndForwardModel(java.lang.String, java.lang.String, java.lang.String,
		 * java.lang.String, android.content.Intent)
		 */
		@Override
		public void makeAndForwardModel(final String source, final String base, final String imageBase, final String settings, final Intent forward)
				throws RemoteException
		{
			// make model
			new AsyncTask<Void, Void, Model>()
			{
				@Override
				protected Model doInBackground(final Void... args)
				{
					try
					{
						// make model
						return TreebolicAIDLBoundService.this.factory.make(source, base, imageBase, settings);
					}
					catch (final Exception e)
					{
						Log.e(TreebolicAIDLBoundService.TAG, "Error making model", e);
					}
					return null;
				}

				@Override
				protected void onPostExecute(final Model model)
				{
					// do not return to client but forward it to activity
					IntentFactory.putModelArg(forward, model, TreebolicAIDLBoundService.this.getUrlScheme());
					forward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					Log.d(TreebolicAIDLBoundService.TAG, "Forwarding model");
					startActivity(forward);
				}
			}.execute();
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
	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(final Intent intent)
	{
		Log.d(TreebolicAIDLBoundService.TAG, "Binding service");
		return this.binder;
	}
}
