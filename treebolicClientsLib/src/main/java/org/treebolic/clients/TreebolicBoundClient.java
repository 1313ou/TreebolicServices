/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.clients;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.clients.iface.ITreebolicClient;
import org.treebolic.services.iface.ITreebolicServiceBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Treebolic bound client
 *
 * @author Bernard Bou
 */
public class TreebolicBoundClient implements ITreebolicClient
{
	/**
	 * Log tag
	 */
	static private final String TAG = "BoundC";

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
	private ITreebolicServiceBinder binder;

	/**
	 * Constructor
	 *
	 * @param context0            context
	 * @param service0            service full name (pkg/class)
	 * @param connectionListener0 connection listener
	 * @param modelListener0      model listener
	 */
	@SuppressWarnings("WeakerAccess")
	public TreebolicBoundClient(final Context context0, @NonNull final String service0, final IConnectionListener connectionListener0, final IModelListener modelListener0)
	{
		this.context = context0;
		this.modelListener = modelListener0;
		this.connectionListener = connectionListener0;
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
			Log.d(TreebolicBoundClient.TAG, "Service disconnected");
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
				Log.d(TreebolicBoundClient.TAG, "Service connected");
				TreebolicBoundClient.this.isBound = true;
				TreebolicBoundClient.this.binder = (ITreebolicServiceBinder) binder0;

				// signal connected
				TreebolicBoundClient.this.connectionListener.onConnected(true);
			}

			@Override
			public void onServiceDisconnected(final ComponentName name)
			{
				TreebolicBoundClient.this.binder = null;

				// signal disconnected
				TreebolicBoundClient.this.connectionListener.onConnected(false);
			}
		};

		final Intent intent = new Intent();
		intent.setComponent(new ComponentName(this.servicePackage, this.serviceName));
		if (!this.context.bindService(intent, this.connection, Context.BIND_AUTO_CREATE))
		{
			Log.e(TreebolicBoundClient.TAG, "Service failed to bind");
			Toast.makeText(this.context, R.string.fail_bind, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void requestModel(final String source, final String base, final String imageBase, final String settings, @Nullable final Intent forward)
	{
		assert this.binder != null;
		if (forward == null)
		{
			this.binder.makeModel(source, base, imageBase, settings, this.modelListener);
		}
		else
		{
			this.binder.makeModel(source, base, imageBase, settings, forward);
		}
	}
}
