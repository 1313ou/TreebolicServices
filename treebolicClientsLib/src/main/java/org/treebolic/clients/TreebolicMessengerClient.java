/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.clients;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.treebolic.ParcelableModel;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.clients.iface.ITreebolicClient;
import org.treebolic.services.iface.ITreebolicService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import treebolic.model.Model;

/**
 * Treebolic messenger bound client
 *
 * @author Bernard Bou
 */
public class TreebolicMessengerClient implements ITreebolicClient
{
	/**
	 * Log tag
	 */
	static private final String TAG = "MessengerC";

	/**
	 * Handler of incoming messages (results) from service
	 */
	static class IncomingHandler extends Handler
	{
		/**
		 * Client
		 */
		private final TreebolicMessengerClient client;

		/**
		 * Constructor
		 *
		 * @param client0 client
		 */
		@SuppressWarnings("WeakerAccess")
		public IncomingHandler(final TreebolicMessengerClient client0)
		{
			super(Looper.getMainLooper());
			this.client = client0;
		}

		@Override
		public void handleMessage(@NonNull final Message msg)
		{
			if (msg.what == ITreebolicService.MSG_RESULT_MODEL)
			{
				final Bundle resultData = msg.getData();
				resultData.setClassLoader(ParcelableModel.class.getClassLoader());
				final String urlScheme = resultData.getString(ITreebolicService.RESULT_URLSCHEME);
				final boolean isSerialized = resultData.getBoolean(ITreebolicService.RESULT_SERIALIZED);
				Model model = null;
				if (isSerialized)
				{
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
					{
						model = resultData.getSerializable(ITreebolicService.RESULT_MODEL, Model.class);
					}
					else
					{
						model = (Model) resultData.getSerializable(ITreebolicService.RESULT_MODEL);
					}
				}
				else
				{
					Parcelable parcelable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? //
							resultData.getParcelable(ITreebolicService.RESULT_MODEL, Parcelable.class) : //
							resultData.getParcelable(ITreebolicService.RESULT_MODEL);
					if (parcelable != null)
					{
						if (!ParcelableModel.class.equals(parcelable.getClass()))
						{
							Log.d(TreebolicMessengerClient.TAG, "Parcel/Unparcel from source classloader " + parcelable.getClass().getClassLoader() + " to target classloader " + ParcelableModel.class.getClassLoader());

							// obtain parcel
							final Parcel parcel = Parcel.obtain();

							// write parcel
							parcel.setDataPosition(0);
							parcelable.writeToParcel(parcel, 0);

							// read parcel
							parcel.setDataPosition(0);
							parcelable = new ParcelableModel(parcel);

							// recycle
							parcel.recycle();
						}
						final ParcelableModel parcelModel = (ParcelableModel) parcelable;
						model = parcelModel.getModel();
					}
				}
				this.client.modelListener.onModel(model, urlScheme);
			}
			else
			{
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Service package
	 */
	@SuppressWarnings("WeakerAccess")
	protected final String servicePackage;

	/**
	 * Service name
	 */
	@SuppressWarnings("WeakerAccess")
	protected final String serviceName;

	/**
	 * Connection listener
	 */
	private final IConnectionListener connectionListener;

	/**
	 * Model listener
	 */
	private final IModelListener modelListener;

	/**
	 * Connection
	 */
	@Nullable
	private ServiceConnection connection;

	/**
	 * Bind status
	 */
	private boolean isBound = false;

	/**
	 * Messenger returned by service when binding
	 */
	@Nullable
	private Messenger service;

	/**
	 * Messenger used to receive data from service
	 */
	private Messenger inMessenger;

	/**
	 * Constructor
	 *
	 * @param context0            context
	 * @param service0            service full name (pkg/class)
	 * @param connectionListener0 connection listener
	 * @param modelListener0      model listener
	 */
	@SuppressWarnings("WeakerAccess")
	public TreebolicMessengerClient(final Context context0, @NonNull final String service0, final IConnectionListener connectionListener0, final IModelListener modelListener0)
	{
		this.context = context0;
		this.connectionListener = connectionListener0;
		this.modelListener = modelListener0;
		final String[] serviceNameComponents = service0.split("/");
		this.servicePackage = serviceNameComponents[0];
		this.serviceName = serviceNameComponents[1];
	}

	@Override
	public void connect()
	{
		bind();
	}

	@Override
	public void disconnect()
	{
		if (this.isBound)
		{
			Log.d(TreebolicMessengerClient.TAG, "Service disconnected");
			// Toast.makeText(this.context, R.string.disconnected, Toast.LENGTH_SHORT).show();

			// if we have received the service, and hence registered with it
			if (this.service != null)
			{
				try
				{
					final Message msg = Message.obtain(null, ITreebolicService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = this.inMessenger;
					this.service.send(msg);
				}
				catch (@NonNull final RemoteException ignored)
				{
					// there is nothing special we need to do if the service has crashed.
				}
			}

			// detach our existing connection.
			assert this.connection != null;
			this.context.unbindService(this.connection);
			this.isBound = false;
		}
	}

	/**
	 * Bind client to service
	 */
	private void bind()
	{
		// prepare connection
		this.inMessenger = new Messenger(new IncomingHandler(this));
		this.connection = new ServiceConnection()
		{
			@Override
			public void onServiceConnected(final ComponentName name, final IBinder binder0)
			{
				Log.d(TreebolicMessengerClient.TAG, "Service bound");
				TreebolicMessengerClient.this.isBound = true;

				// pass service in-messenger to post results to
				TreebolicMessengerClient.this.service = new Messenger(binder0);
				final Message msg = Message.obtain(null, ITreebolicService.MSG_REGISTER_CLIENT);
				msg.replyTo = TreebolicMessengerClient.this.inMessenger;
				try
				{
					TreebolicMessengerClient.this.service.send(msg);
				}
				catch (@NonNull final RemoteException e)
				{
					Log.e(TreebolicMessengerClient.TAG, "Send error", e);
				}

				// signal connected
				TreebolicMessengerClient.this.connectionListener.onConnected(true);
			}

			@Override
			public void onServiceDisconnected(final ComponentName name)
			{
				TreebolicMessengerClient.this.service = null;

				// signal disconnected
				TreebolicMessengerClient.this.connectionListener.onConnected(false);
			}
		};

		// bind
		final Intent intent = new Intent();
		intent.setComponent(new ComponentName(this.servicePackage, this.serviceName));
		if (!this.context.bindService(intent, this.connection, Context.BIND_AUTO_CREATE))
		{
			Log.e(TreebolicMessengerClient.TAG, "Service failed to bind");
			Toast.makeText(this.context, R.string.fail_bind, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void requestModel(final String source, final String base, final String imageBase, final String settings, final Intent forward)
	{
		// bundle
		final Bundle bundle = new Bundle();
		bundle.putString(ITreebolicService.EXTRA_SOURCE, source);
		bundle.putString(ITreebolicService.EXTRA_BASE, base);
		bundle.putString(ITreebolicService.EXTRA_IMAGEBASE, imageBase);
		bundle.putString(ITreebolicService.EXTRA_SETTINGS, settings);
		bundle.putParcelable(ITreebolicService.EXTRA_FORWARD_RESULT_TO, forward);

		// request message
		final Message msg = Message.obtain(null, ITreebolicService.MSG_REQUEST_MODEL, 0, 0);

		// attach bundle
		msg.setData(bundle);

		// send message
		try
		{
			assert TreebolicMessengerClient.this.service != null;
			TreebolicMessengerClient.this.service.send(msg);
		}
		catch (@NonNull final RemoteException e)
		{
			Log.e(TreebolicMessengerClient.TAG, "Send error", e);
		}
	}
}
