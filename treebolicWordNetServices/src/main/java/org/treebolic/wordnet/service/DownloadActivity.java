package org.treebolic.wordnet.service;

import java.io.IOException;
import java.io.InputStream;

import android.os.Bundle;
import android.widget.Toast;

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

	/*
	 * (non-Javadoc)
	 *
	 * @see org.treebolic.download.DownloadActivity#onCreate(android.os.Bundle)
	 */
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

	/*
	 * (non-Javadoc)
	 *
	 * @see org.treebolic.download.DownloadActivity#start()
	 */
	@Override
	public void start()
	{
		this.asTarGz = this.downloadUrl.endsWith(".tar.gz"); //$NON-NLS-1$
		super.start(R.string.wordnet);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.treebolic.download.DownloadActivity#doProcessing()
	 */
	@Override
	protected boolean doProcessing()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.treebolic.download.DownloadActivity#process(java.io.InputStream)
	 */
	@Override
	protected boolean process(final InputStream inputStream) throws IOException
	{
		new Deployer(DownloadActivity.this.getFilesDir()).process(inputStream, this.asTarGz);
		return true;
	}
}
