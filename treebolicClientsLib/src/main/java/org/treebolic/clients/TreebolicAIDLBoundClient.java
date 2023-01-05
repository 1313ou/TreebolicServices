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
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import org.treebolic.ParcelableModel;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.clients.iface.ITreebolicClient;
import org.treebolic.services.iface.ITreebolicAIDLService;
import org.treebolic.services.iface.ITreebolicService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import treebolic.model.Model;

/**
 * Treebolic bound client
 *
 * @author Bernard Bou
 */
public class TreebolicAIDLBoundClient implements ITreebolicClient
{
	/**
	 * Log tag
	 */
	static private final String TAG = "AidlBoundC";

	/**
	 * Abstract: Service package
	 */
	@SuppressWarnings("WeakerAccess")
	protected final String servicePackage;

	/**
	 * Abstract: Service name
	 */
	@SuppressWarnings("WeakerAccess")
	protected final String serviceName;

	/**
	 * Context
	 */
	private final Context context;

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
	 * Bind state
	 */
	private boolean isBound = false;

	/**
	 * Binder
	 */
	@Nullable
	private ITreebolicAIDLService binder;

	/**
	 * Result receiver
	 */
	@NonNull
	private final ResultReceiver receiver;

	/**
	 * Constructor
	 *
	 * @param context0            context
	 * @param service0            service full name (pkg/class)
	 * @param connectionListener0 connectionListener
	 * @param modelListener0      modelListener
	 */
	@SuppressWarnings("WeakerAccess")
	public TreebolicAIDLBoundClient(final Context context0, @NonNull final String service0, final IConnectionListener connectionListener0, final IModelListener modelListener0)
	{
		this.context = context0;
		this.modelListener = modelListener0;
		this.connectionListener = connectionListener0;
		final String[] serviceNameComponents = service0.split("/");
		this.servicePackage = serviceNameComponents[0];
		this.serviceName = serviceNameComponents[1];
		this.receiver = new ResultReceiver(new Handler(Looper.getMainLooper()))
		{
			@Override
			protected void onReceiveResult(final int resultCode, @NonNull final Bundle resultData)
			{
				resultData.setClassLoader(ParcelableModel.class.getClassLoader());

				// scheme
				final String urlScheme = resultData.getString(ITreebolicService.RESULT_URLSCHEME);

				// model
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
							Log.d(TreebolicAIDLBoundClient.TAG, "Parcel/Unparcel from source classloader " + parcelable.getClass().getClassLoader() + " to target classloader " + ParcelableModel.class.getClassLoader());

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
				TreebolicAIDLBoundClient.this.modelListener.onModel(resultCode == 0 ? model : null, urlScheme);
			}
		};
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
			Log.d(TreebolicAIDLBoundClient.TAG, "Service disconnected");
			// Toast.makeText(this.context, R.string.disconnected, Toast.LENGTH_SHORT).show();

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
		this.connection = new ServiceConnection()
		{
			@Override
			public void onServiceConnected(final ComponentName name, final IBinder binder0)
			{
				Log.d(TreebolicAIDLBoundClient.TAG, "Service connected");
				TreebolicAIDLBoundClient.this.isBound = true;
				TreebolicAIDLBoundClient.this.binder = ITreebolicAIDLService.Stub.asInterface(binder0);

				// signal connected
				TreebolicAIDLBoundClient.this.connectionListener.onConnected(true);
			}

			@Override
			public void onServiceDisconnected(final ComponentName name)
			{
				TreebolicAIDLBoundClient.this.binder = null;

				// signal disconnected
				TreebolicAIDLBoundClient.this.connectionListener.onConnected(false);
			}
		};

		final Intent intent = new Intent();
		intent.setComponent(new ComponentName(this.servicePackage, this.serviceName));
		if (!this.context.bindService(intent, this.connection, Context.BIND_AUTO_CREATE))
		{
			Log.e(TreebolicAIDLBoundClient.TAG, "Service failed to bind " + this.servicePackage + '/' + this.serviceName);
			Toast.makeText(this.context, R.string.fail_bind, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void requestModel(final String source, final String base, final String imageBase, final String settings, @Nullable final Intent forward)
	{
		assert this.binder != null;
		if (forward == null)
		{
			try
			{
				this.binder.makeModel(source, base, imageBase, settings, this.receiver);
			}
			catch (@NonNull final RemoteException e)
			{
				Log.e(TreebolicAIDLBoundClient.TAG, "Service request failed", e);
			}
		}
		else
		{
			try
			{
				this.binder.makeAndForwardModel(source, base, imageBase, settings, forward);
			}
			catch (@NonNull final RemoteException e)
			{
				Log.e(TreebolicAIDLBoundClient.TAG, "Service request failed", e);
			}
		}
	}
}
