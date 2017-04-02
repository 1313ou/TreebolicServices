package org.treebolic.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.treebolic.clients.iface.IModelListener;
import org.treebolic.services.iface.ITreebolicService;
import org.treebolic.services.iface.ITreebolicServiceBinder;

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
	 * Model factory
	 */
	protected IModelFactory factory;

	/**
	 * Binder given to clients
	 */
	public class TreebolicServiceBinder extends Binder implements ITreebolicServiceBinder
	{
		/**
		 * Constructor
		 */
		TreebolicServiceBinder(@SuppressWarnings("unused") final TreebolicBoundService service0)
		{
			//
		}

		@Override
		public void makeModel(final String source, final String base, final String imageBase, final String settings, final IModelListener modelListener)
		{
			// make model
			new AsyncTask<Void, Void, Model>()
			{
				@Override
				protected Model doInBackground(final Void... args)
				{
					try
					{
						return TreebolicBoundService.this.factory.make(source, base, imageBase, settings);
					}
					catch (final Exception e)
					{
						//
					}
					return null;
				}

				@Override
				protected void onPostExecute(final Model model)
				{
					Log.d(TreebolicBoundService.TAG, "Returning model " + model);
					modelListener.onModel(model, getUrlScheme());
				}
			}.execute();
		}

		@Override
		public void makeModel(final String source, final String base, final String imageBase, final String settings, final Intent forward)
		{
			// make model
			new AsyncTask<Void, Void, Model>()
			{
				@Override
				protected Model doInBackground(final Void... args)
				{
					try
					{
						return TreebolicBoundService.this.factory.make(source, base, imageBase, settings);
					}
					catch (final Exception e)
					{
						//
					}
					return null;
				}

				@Override
				protected void onPostExecute(final Model model)
				{
					// do not return to client but forward it to activity
					IntentFactory.putModelArg(forward, model, getUrlScheme());
					forward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					Log.d(TreebolicBoundService.TAG, "Forwarding model");
					startActivity(forward);
				}
			}.execute();
		}
	}

	/**
	 * Binder that returns an interface to the service
	 */
	private final IBinder binder = new TreebolicServiceBinder(this);

	/**
	 * Constructor
	 */
	public TreebolicBoundService()
	{
		//
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
