package org.treebolic.wordnet.service;

import android.support.v7.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Convenience class for provider to acquire database data from provider
 *
 * @author Bernard Bou
 */
public class QueryProviderActivity extends AppCompatActivity
{
	/**
	 * Log tag
	 */
	private static final String TAG = "QueryProviderActivity";

	/**
	 * Android provider package for wordnet data
	 */
	private static final String PROVIDER_PKG = "org.wordnet.provider";

	/**
	 * Android provider name for wordnet data
	 */
	private static final String PROVIDER_NAME = "android.support.v4.content.FileProvider";

	/**
	 * Android provider activity for wordnet data
	 */
	private static final String PROVIDER_ACTIVITY = "org.wordnet.provider.FileProviderActivity";

	/**
	 * Request code
	 */
	private static final int REQUEST_CODE = 8888;

	/**
	 * Data deployer
	 */
	private Deployer deployer;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// view
		setContentView(R.layout.activity_query_file_provider);

		// deployer
		this.deployer = new Deployer(getFilesDir());

		// button
		final Button closeButton = (Button) findViewById(R.id.button);
		closeButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				final Intent requestFileIntent = new Intent();
				requestFileIntent.setAction(Intent.ACTION_DEFAULT);
				requestFileIntent.setComponent(new ComponentName(QueryProviderActivity.PROVIDER_PKG, QueryProviderActivity.PROVIDER_ACTIVITY));
				startActivityForResult(requestFileIntent, QueryProviderActivity.REQUEST_CODE);
			}
		});
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent returnIntent)
	{
		if (requestCode == QueryProviderActivity.REQUEST_CODE)
		{
			if (resultCode == AppCompatActivity.RESULT_OK)
			{
				// retrieve arguments
				final Uri returnUri = returnIntent.getData();
				if (returnUri != null)
				{
					final boolean asTarGz = returnUri.getPath().endsWith(".tar.gz");
					ParcelFileDescriptor fileDescriptor = null;
					FileInputStream fin = null;

					// try to open the file for "read" access using the returned URI. If the file isn't found, write to the error log and return.
					//noinspection TryWithIdenticalCatches
					try
					{
						// get the content resolver instance for this context, and use it to get a ParcelFileDescriptor for the file.
						fileDescriptor = getContentResolver().openFileDescriptor(returnUri, "r");
						if (fileDescriptor != null)
						{
							// get a regular file descriptor for the file
							final FileDescriptor fd = fileDescriptor.getFileDescriptor();

							fin = new FileInputStream(fd);
							this.deployer.process(fin, asTarGz);

							Toast.makeText(this, R.string.ok_data, Toast.LENGTH_SHORT).show();
						}
					}
					catch (final FileNotFoundException e)
					{
						Log.e(QueryProviderActivity.TAG, "provider data " + returnUri, e);
						Toast.makeText(this, R.string.fail_data, Toast.LENGTH_SHORT).show();
					}
					catch (final IOException e)
					{
						Log.e(QueryProviderActivity.TAG, "provider data " + returnUri, e);
						Toast.makeText(this, R.string.fail_data, Toast.LENGTH_SHORT).show();
					}
					finally
					{
						if (fileDescriptor != null)
						{
							try
							{
								fileDescriptor.close();
							}
							catch (final IOException e)
							{
								//
							}
						}
						if (fin != null)
						{
							try
							{
								fin.close();
							}
							catch (final IOException e)
							{
								//
							}
						}
					}
				}
			}
			finish();
		}

		// super
		super.onActivityResult(requestCode, resultCode, returnIntent);
	}

	/**
	 * Whether provider is available
	 *
	 * @param context context
	 * @return true if provider is available
	 */
	static public boolean isProviderAvailable(final Context context)
	{
		return QueryProviderActivity.getProvider(context) != null;
	}

	/**
	 * Provider info
	 *
	 * @param context context
	 * @return provider info
	 */
	static public ProviderInfo getProvider(final Context context)
	{
		try
		{
			/*
			 * PackageInfo pack = context.getPackageManager().getPackageInfo(PROVIDER_PKG, PackageManager.GET_PROVIDERS); ProviderInfo[] providers =
			 * pack.providers; if (providers != null) { for (ProviderInfo provider0 : providers) { Log.d(TAG, "provider name: " + provider0.name); Log.d(TAG,
			 * "provider package: " + provider0.packageName); Log.d(TAG, "provider authority: " + provider0.authority); } }
			 */
			return context.getPackageManager().getProviderInfo(new ComponentName(QueryProviderActivity.PROVIDER_PKG, QueryProviderActivity.PROVIDER_NAME), 0);
		}
		catch (final NameNotFoundException e)
		{
			//
		}
		return null;
	}
}
