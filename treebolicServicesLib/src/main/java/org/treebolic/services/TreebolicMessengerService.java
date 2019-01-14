package org.treebolic.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import org.treebolic.services.iface.ITreebolicService;

import java.lang.ref.WeakReference;
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
	 * Make model task
	 */
	static private class MakeTask extends AbstractMakeTask
	{
		private final Bundle bundle;
		private final List<Messenger> clients;

		private MakeTask(final String source, final String base, final String imageBase, final String settings, final String urlScheme, final IModelFactory factory, final Bundle bundle, final List<Messenger> clients)
		{
			super(source, base, imageBase, settings, urlScheme, factory);
			this.bundle = bundle;
			this.clients = clients;
		}

		@Override
		protected void onPostExecute(final Model model)
		{
			// reuse bundle
			this.bundle.clear();

			// pack model into message bundle
			IntentFactory.putModelResult(this.bundle, model, this.urlScheme);

			// send message to all clients
			Log.d(TreebolicMessengerService.TAG, this.clients.size() + " clients");
			for (int i = this.clients.size() - 1; i >= 0; i--)
			{
				// return model to clients as a message
				final Message msg = Message.obtain();
				msg.what = ITreebolicService.MSG_RESULT_MODEL;
				msg.setData(this.bundle);

				try
				{
					Log.d(TreebolicMessengerService.TAG, "Returning model " + model);
					this.clients.get(i).send(msg);
				}
				catch (@NonNull final RemoteException ignored)
				{
					// The client is dead. Remove it from the list;
					// we are going through the list from back to front
					// so this is safe to do inside the loop.
					this.clients.remove(i);
				}
			}
		}
	}

	/**
	 * Make model and forward task
	 */
	static private class MakeTaskAndForward extends AbstractMakeTask
	{
		@NonNull
		private final WeakReference<Context> contextWeakReference;
		private final Intent forward;

		private MakeTaskAndForward(final String source, final String base, final String imageBase, final String settings, final String urlScheme, final IModelFactory factory, final Context context, final Intent forward)
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
			Log.d(TreebolicMessengerService.TAG, "Forwarding model");
			final Context context = this.contextWeakReference.get();
			if (context != null)
			{
				context.startActivity(this.forward);
			}
		}
	}

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
		 * @param service0 service
		 */
		private IncomingHandler(final TreebolicMessengerService service0)
		{
			super();
			this.service = service0;
		}

		@Override
		public void handleMessage(@NonNull final Message msg)
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
	@NonNull
	final private Messenger messenger;

	/**
	 * List of clients
	 */
	@NonNull
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
	 * @param bundle data
	 */
	private void makeModel(@NonNull final Bundle bundle)
	{
		final String source = bundle.getString(ITreebolicService.EXTRA_SOURCE);
		final String base = bundle.getString(ITreebolicService.EXTRA_BASE);
		final String imageBase = bundle.getString(ITreebolicService.EXTRA_IMAGEBASE);
		final String settings = bundle.getString(ITreebolicService.EXTRA_SETTINGS);
		final Intent forward = bundle.getParcelable(ITreebolicService.EXTRA_FORWARD_RESULT_TO);

		// make model
		final AsyncTask<Void, Void, Model> task = forward == null ? //
				new MakeTask(source, base, imageBase, settings, getUrlScheme(), this.factory, bundle, this.clients) : //
				new MakeTaskAndForward(source, base, imageBase, settings, getUrlScheme(), this.factory, this, forward);
		task.execute();
	}
}
