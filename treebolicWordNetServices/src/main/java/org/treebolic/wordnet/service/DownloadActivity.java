/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.wordnet.service;

import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;

/**
 * WordNet download activity
 *
 * @author Bernard Bou
 */
public class DownloadActivity extends org.treebolic.download.DownloadActivity
{
	/**
	 * Whether stream is tar.gz (zip otherwise)
	 */
	private boolean asTarGz;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.downloadUrl = Settings.getStringPref(this, Settings.PREF_DOWNLOAD);
		if (this.downloadUrl == null || this.downloadUrl.isEmpty())
		{
			Toast.makeText(this, R.string.error_null_download_url, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public void start()
	{
		assert this.downloadUrl != null;
		this.asTarGz = this.downloadUrl.endsWith(".tar.gz");
		super.start(R.string.wordnet);
	}

	// P O S T P R O C E S S I N G

	@SuppressWarnings("SameReturnValue")
	@Override
	protected boolean doProcessing()
	{
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	@Override
	protected boolean process(@NonNull final InputStream inputStream) throws IOException
	{
		new Deployer(DownloadActivity.this.getFilesDir()).process(inputStream, this.asTarGz);
		return true;
	}
}
