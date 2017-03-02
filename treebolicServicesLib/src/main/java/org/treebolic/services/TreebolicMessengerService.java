package org.treebolic.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.treebolic.services.iface.IModelFactory;
import org.treebolic.services.iface.ITreebolicService;

import java.util.ArrayList;
import java.util.List;

import treebolic.model.Model;

/**
 * Treebolic messenger service
 *
 * @author Bernard Bou
 */
abstract public class TreebolicMessengerService extends Service implements ITreebolicService
{
	/**
	 * Log tag
	 */
	static private final String TAG = "TMessengerBoundS";

	/**
	 * Abstract: Model factory
	 */
	protected IModelFactory factory;

	/**
	 * Handler of incoming messages from clients.
	 */
	static class IncomingHandler extends Handler
	{
		/**
		 * Service
		 */
		private final TreebolicMessengerService service;

		/**
		 * Constructor
		 *
		 * @param service0
		 *            service
		 */
		public IncomingHandler(final TreebolicMessengerService service0)
		{
			super();
			this.service = service0;
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void handleMessage(final Message msg)
		{
			switch (msg.what)
			{
			case ITreebolicService.MSG_REQUEST_MODEL:
				final Bundle bundle = msg.getData();
				this.service.makeModel(bundle);
				break;

			case ITreebolicService.MSG_REGISTER_CLIENT:
				this.service.clients.add(msg.replyTo);
				break;

			case ITreebolicService.MSG_UNREGISTER_CLIENT:
				this.service.clients.remove(msg.replyTo);
				break;

			default:
				super.handleMessage(msg);
			}
		}

	}

	/**
	 * Target we publish for clients to send messages to
	 */
	final Messenger messenger;

	/**
	 * List of clients
	 */
	private final List<Messenger> clients;

	/**
	 * Constructor
	 */
	public TreebolicMessengerService()
	{
		this.clients = new ArrayList<>();
		this.messenger = new Messenger(new IncomingHandler(this));
	}

	@Override
	public IBinder onBind(final Intent intent)
	{
		// Toast.makeText(getApplicationContext(), R.string.bound, Toast.LENGTH_SHORT).show();
		// return messenger to send messages to
		return this.messenger.getBinder();
	}

	/**
	 * Make model
	 *
	 * @param bundle
	 *            data
	 */
	private void makeModel(final Bundle bundle)
	{
		final String source = bundle.getString(ITreebolicService.EXTRA_SOURCE);
		final String base = bundle.getString(ITreebolicService.EXTRA_BASE);
		final String imageBase = bundle.getString(ITreebolicService.EXTRA_IMAGEBASE);
		final String settings = bundle.getString(ITreebolicService.EXTRA_SETTINGS);
		final Intent forward = bundle.getParcelable(ITreebolicService.EXTRA_FORWARD_RESULT_TO);

		// make model
		new AsyncTask<Void, Void, Model>()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			protected Model doInBackground(final Void... args)
			{
				return TreebolicMessengerService.this.makeModel(source, base, imageBase, settings);
			}

			@SuppressWarnings("synthetic-access")
			@Override
			protected void onPostExecute(final Model model)
			{
				if (forward == null)
				{
					// reuse bundle
					bundle.clear();

					// pack model into message bundle
					IntentFactory.putModelResult(bundle, model, getUrlScheme());

					// return model to clients as a message
					final Message msg = Message.obtain(null, ITreebolicService.MSG_RESULT_MODEL, 0, 0);
					msg.setData(bundle);

					// send message to all clients
					for (int i = TreebolicMessengerService.this.clients.size() - 1; i >= 0; i--)
					{
						try
						{
							Log.d(TreebolicMessengerService.TAG, "Returning model " + model);
							TreebolicMessengerService.this.clients.get(i).send(msg);
						}
						catch (final RemoteException e)
						{
							// The client is dead. Remove it from the list;
							// we are going through the list from back to front
							// so this is safe to do inside the loop.
							TreebolicMessengerService.this.clients.remove(i);
						}
					}
				}
				else
				{
					// do not return to client but forward it to activity
					IntentFactory.putModelArg(forward, model, getUrlScheme());
					forward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					Log.d(TreebolicMessengerService.TAG, "Forwarding model");
					startActivity(forward);
				}
			}
		}.execute();
	}

	/**
	 * Make model (guarded)
	 *
	 * @param source
	 *            source
	 * @param base
	 *            base
	 * @param imageBase
	 *            image base
	 * @param settings
	 *            settings
	 * @return model
	 */
	private Model makeModel(final String source, final String base, final String imageBase, final String settings)
	{
		try
		{
			return this.factory.make(source, base, imageBase, settings);
		}
		catch (final Exception e)
		{
			Log.e(TreebolicMessengerService.TAG, "Model factory error", e);
		}
		return null;
	}
}
