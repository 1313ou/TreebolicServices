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
		this.asTarGz = this.downloadUrl.endsWith(".tar.gz");
		super.start(R.string.wordnet);
	}

	@Override
	protected boolean doProcessing()
	{
		return true;
	}

	@Override
	protected boolean process(final InputStream inputStream) throws IOException
	{
		new Deployer(DownloadActivity.this.getFilesDir()).process(inputStream, this.asTarGz);
		return true;
	}
}
