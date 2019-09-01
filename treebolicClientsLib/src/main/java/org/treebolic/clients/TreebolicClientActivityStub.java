/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.clients;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.treebolic.AppCompatCommonActivity;
import org.treebolic.TreebolicIface;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.clients.iface.ITreebolicClient;

import androidx.annotation.Nullable;

/**
 * Treebolic server. Produces Treebolic model from data. Acts as client to service.
 *
 * @author Bernard Bou
 */
abstract public class TreebolicClientActivityStub extends AppCompatCommonActivity implements IConnectionListener, IModelListener
{
	/**
	 * Log tag
	 */
	static private final String TAG = "TreebolicClientA";

	/**
	 * Client
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	protected ITreebolicClient client;

	/**
	 * Client status true=up
	 */
	@SuppressWarnings("WeakerAccess")
	protected boolean clientStatus = false;

	/**
	 * Url scheme
	 */
	@SuppressWarnings("unused")
	protected String urlScheme;

	// L I F E C Y C L E

	@Override
	protected void onResume()
	{
		stop();
		start();
		super.onResume();
	}

	// @Override
	// public void onModel(Model model){}

	@Override
	protected void onPause()
	{
		stop();
		super.onPause();
	}

	// C L I E N T M A N A G E M E N T

	@Nullable
	abstract protected ITreebolicClient makeClient();

	/**
	 * Start client
	 */
	@SuppressWarnings("WeakerAccess")
	protected void start()
	{
		this.client = makeClient();
		if (this.client != null)
		{
			Log.d(TreebolicClientActivityStub.TAG, "Connecting client-service");
			this.client.connect();
		}
	}

	/**
	 * Stop client
	 */
	@SuppressWarnings("WeakerAccess")
	protected void stop()
	{
		if (this.client != null)
		{
			Log.d(TreebolicClientActivityStub.TAG, "Disconnecting client-service");
			this.client.disconnect();
			this.client = null;
		}
	}

	/**
	 * Request model
	 */
	@SuppressWarnings("WeakerAccess")
	protected void request()
	{
		// get query from activity intent
		final Intent intent = getIntent();
		if (TreebolicIface.ACTION_MAKEMODEL.equals(intent.getAction()))
		{
			final String query = intent.getStringExtra(TreebolicIface.ARG_SOURCE);
			final String base = intent.getStringExtra(TreebolicIface.ARG_BASE);
			final String imageBase = intent.getStringExtra(TreebolicIface.ARG_IMAGEBASE);
			final String settings = intent.getStringExtra(TreebolicIface.ARG_SETTINGS);

			if (query == null || query.isEmpty())
			{
				Toast.makeText(this, R.string.fail_nullquery, Toast.LENGTH_SHORT).show();
				return;
			}
			else if (this.client == null)
			{
				Toast.makeText(this, R.string.fail_nullclient, Toast.LENGTH_SHORT).show();
				return;
			}

			// request model from query
			this.client.requestModel(query, base, imageBase, settings, null);
		}
	}

	// C O N N E C T I O N L I S T E N E R

	@Override
	public void onConnected(final boolean flag)
	{
		this.clientStatus = flag;

		// Toast.makeText(this, R.string.bound, Toast.LENGTH_SHORT).show();
		request();
	}

	// M O D E L L I S T E N E R

	// @Override
	// abstract public void onModel();
}
