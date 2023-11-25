/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.wordnet.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.treebolic.AppCompatCommonActivity;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Convenience class for provider to acquire database data from provider
 *
 * @author Bernard Bou
 */
public class QueryProviderActivity extends AppCompatCommonActivity
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
	private static final String PROVIDER_NAME = "androidx.core.content.FileProvider";

	/**
	 * Android provider activity for wordnet data
	 */
	private static final String PROVIDER_ACTIVITY = "org.wordnet.provider.FileProviderActivity";

	/**
	 * Data deployer
	 */
	private Deployer deployer;

	/**
	 * Activity result launcher
	 */
	protected ActivityResultLauncher<Intent> activityResultLauncher;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// view
		setContentView(R.layout.activity_query_file_provider);

		// deployer
		this.deployer = new Deployer(getFilesDir());

		// activity result launcher
		this.activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			boolean success = result.getResultCode() == Activity.RESULT_OK;
			if (success)
			{
				// retrieve arguments
				final Intent returnIntent = result.getData();
				if (returnIntent != null)
				{
					final Uri uri = returnIntent.getData();
					if (uri != null)
					{
						final String path = uri.getPath();
						final boolean asTarGz = path != null && path.endsWith(".tar.gz");
						ParcelFileDescriptor fileDescriptor = null;
						FileInputStream fin = null;

						// try to open the file for "read" access using the returned URI. If the file isn't found, write to the error log and return.
						try
						{
							// get the content resolver instance for this context, and use it to get a ParcelFileDescriptor for the file.
							fileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
							if (fileDescriptor != null)
							{
								// get a regular file descriptor for the file
								final FileDescriptor fd = fileDescriptor.getFileDescriptor();

								fin = new FileInputStream(fd);
								this.deployer.process(fin, asTarGz);

								Toast.makeText(this, R.string.ok_data, Toast.LENGTH_SHORT).show();
							}
						}
						catch (@NonNull final IOException e)
						{
							Log.e(QueryProviderActivity.TAG, "provider data " + uri, e);
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
								catch (@NonNull final IOException ignored)
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
								catch (@NonNull final IOException ignored)
								{
									//
								}
							}
						}
					}
				}
			}
			finish();
		});

		// button
		final Button closeButton = findViewById(R.id.button);
		closeButton.setOnClickListener(v -> {
			final Intent requestFileIntent = new Intent();
			requestFileIntent.setAction(Intent.ACTION_DEFAULT);
			requestFileIntent.setComponent(new ComponentName(QueryProviderActivity.PROVIDER_PKG, QueryProviderActivity.PROVIDER_ACTIVITY));
			activityResultLauncher.launch(requestFileIntent);
		});
	}

	/**
	 * Whether provider is available
	 *
	 * @param context context
	 * @return true if provider is available
	 */
	static public boolean isProviderAvailable(@NonNull final Context context)
	{
		return QueryProviderActivity.getProvider(context) != null;
	}

	/**
	 * Provider info
	 *
	 * @param context context
	 * @return provider info
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	static public ProviderInfo getProvider(@NonNull final Context context)
	{
		try
		{
			// PackageManager pm = context.getPackageManager();
			// PackageInfo pack = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU ? //
			// 		pm.getPackageInfo(PROVIDER_PKG, PackageManager.PackageInfoFlags.of(PackageManager.GET_PROVIDERS)) : //
			// 		pm.getPackageInfo(PROVIDER_PKG, PackageManager.GET_PROVIDERS);
			// ProviderInfo[] providers = pack.providers;
			// if (providers != null)
			// {
			// 	for (ProviderInfo provider0 : providers)
			// 	{
			// 		Log.d(TAG, "provider name: " + provider0.name);
			// 		Log.d(TAG, "provider package: " + provider0.packageName);
			// 		Log.d(TAG, "provider authority: " + provider0.authority);
			// 	}
			// }

			PackageManager pm = context.getPackageManager();
			return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU ? //
					pm.getProviderInfo(new ComponentName(QueryProviderActivity.PROVIDER_PKG, QueryProviderActivity.PROVIDER_NAME), PackageManager.ComponentInfoFlags.of(0)) : //
					pm.getProviderInfo(new ComponentName(QueryProviderActivity.PROVIDER_PKG, QueryProviderActivity.PROVIDER_NAME), 0);
		}
		catch (@NonNull final NameNotFoundException ignored)
		{
			//
		}
		return null;
	}
}
