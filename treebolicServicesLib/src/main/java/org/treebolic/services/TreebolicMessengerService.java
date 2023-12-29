/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.services;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;
import android.util.Log;

import org.treebolic.services.iface.ITreebolicService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
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
	static private final String TAG = "MessengerS";

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
	static public TaskRunner.Callback<Model> makeModelCallback(@NonNull final Bundle bundle, final String urlScheme, @NonNull final List<Messenger> clients)
	{
		return (model) -> {

			// reuse bundle
			bundle.clear();

			// pack model into message bundle
			IntentFactory.putModelResult(bundle, model, urlScheme);

			// send message to all clients
			Log.d(TAG, clients.size() + " clients");
			for (int i = clients.size() - 1; i >= 0; i--)
			{
				// return model to clients as a message
				final Message msg = Message.obtain();
				msg.what = ITreebolicService.MSG_RESULT_MODEL;
				msg.setData(bundle);

				try
				{
					Log.d(TAG, "Returning model " + model);
					clients.get(i).send(msg);
				}
				catch (@NonNull final RemoteException ignored)
				{
					// The client is dead. Remove it from the list;
					// we are going through the list from back to front
					// so this is safe to do inside the loop.
					clients.remove(i);
				}
			}
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
					Utils.warn(context, R.string.activity_not_found);
				}
				catch (RuntimeException rte)
				{
					if (rte.getCause() instanceof TransactionTooLargeException)
					{
						Utils.warn(context, R.string.transaction_too_large);
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
	 * Abstract: Model factory
	 */
	@SuppressWarnings("WeakerAccess")
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
			super(Looper.getMainLooper());
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
		final Intent forward = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? //
				bundle.getParcelable(ITreebolicService.EXTRA_FORWARD_RESULT_TO, Intent.class) : //
				bundle.getParcelable(ITreebolicService.EXTRA_FORWARD_RESULT_TO);

		// make model
		final Callable<Model> callable = makeModelCallable(source, base, imageBase, settings, this.factory);
		final TaskRunner.Callback<Model> callback = forward == null ? //
				makeModelCallback(bundle, getUrlScheme(), this.clients) : //
				makeModelForwardCallback(new WeakReference<>(this), getUrlScheme(), forward);
		TaskRunner.execute(callable, callback);
	}
}
