/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.clients;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import org.treebolic.ParcelableModel;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.services.iface.ITreebolicService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import treebolic.model.Model;

/**
 * Treebolic intent service client
 *
 * @author Bernard Bou
 */
public class TreebolicIntentClient implements org.treebolic.clients.iface.ITreebolicClient
{
	/**
	 * Log tag
	 */
	static private final String TAG = "IntentC";

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
	 * Result receiver
	 */
	@Nullable
	private final ResultReceiver receiver;

	/**
	 * Constructor
	 *
	 * @param context0            context
	 * @param service0            service full name (pkg/class)
	 * @param connectionListener0 connection listener
	 * @param modelListener0      model listener
	 */
	@SuppressWarnings("WeakerAccess")
	public TreebolicIntentClient(final Context context0, @NonNull final String service0, final IConnectionListener connectionListener0, final IModelListener modelListener0)
	{
		this.context = context0;
		this.connectionListener = connectionListener0;
		this.modelListener = modelListener0;
		final String[] serviceNameComponents = service0.split("/");
		this.servicePackage = serviceNameComponents[0];
		this.serviceName = serviceNameComponents[1];
		this.receiver = new ResultReceiver(new Handler())
		{
			@Override
			protected void onReceiveResult(final int resultCode, @NonNull final Bundle resultData)
			{
				resultData.setClassLoader(ParcelableModel.class.getClassLoader());

				final String urlScheme = resultData.getString(ITreebolicService.RESULT_URLSCHEME);
				final boolean isSerialized = resultData.getBoolean(ITreebolicService.RESULT_SERIALIZED);
				Model model = null;
				if (isSerialized)
				{
					model = (Model) resultData.getSerializable(ITreebolicService.RESULT_MODEL);
				}
				else
				{
					Parcelable parcelable = resultData.getParcelable(ITreebolicService.RESULT_MODEL);
					if (parcelable != null)
					{
						if (!ParcelableModel.class.equals(parcelable.getClass()))
						{
							Log.d(TreebolicIntentClient.TAG, "Parcel/Unparcel from source classloader " + parcelable.getClass().getClassLoader() + " to target classloader " + ParcelableModel.class.getClassLoader());

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
				TreebolicIntentClient.this.modelListener.onModel(resultCode == 0 ? model : null, urlScheme);
			}
		};
	}

	@Override
	public void connect()
	{
		this.connectionListener.onConnected(true);
	}

	@Override
	public void disconnect()
	{
		this.connectionListener.onConnected(false);
	}

	@Override
	public void requestModel(final String source, final String base, final String imageBase, final String settings, final Intent forward)
	{
		final Intent intent = new Intent();
		intent.setComponent(new ComponentName(this.servicePackage, this.serviceName));
		intent.setAction(ITreebolicService.ACTION_MAKEMODEL);
		intent.putExtra(ITreebolicService.EXTRA_SOURCE, source);
		intent.putExtra(ITreebolicService.EXTRA_BASE, base);
		intent.putExtra(ITreebolicService.EXTRA_IMAGEBASE, imageBase);
		intent.putExtra(ITreebolicService.EXTRA_SETTINGS, settings);
		intent.putExtra(ITreebolicService.EXTRA_RECEIVER, this.receiver);
		intent.putExtra(ITreebolicService.EXTRA_FORWARD_RESULT_TO, forward);
		if (this.context.startService(intent) == null)
		{
			Log.e(TreebolicIntentClient.TAG, "Intent service failed to start " + this.servicePackage + '/' + this.serviceName);
			Toast.makeText(this.context, R.string.fail_start, Toast.LENGTH_LONG).show();
		}
	}
}
