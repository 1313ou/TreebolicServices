package org.treebolic.wordnet.service;

import android.util.Log;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Source data deployer
 *
 * @author Bernard Bou
 */
@SuppressWarnings("WeakerAccess")
public class Deployer
{
	/**
	 * Log tag
	 */
	static private final String TAG = "TWordNetDeployer";

	/**
	 * Sub path in main directory
	 */
	static private final String PATH = "wordnet";

	/**
	 * Dir to write data to
	 */
	private final File dir;

	/**
	 * Constructor
	 *
	 * @param dir0 parent dir to write data to
	 */
	public Deployer(final File dir0)
	{
		this.dir = new File(dir0, Deployer.PATH);
	}

	/**
	 * Data status
	 *
	 * @return data status
	 */
	public boolean status()
	{
		return treebolic.provider.wordnet.jwi.DataManager.check(this.dir);
	}

	/**
	 * Clean up data
	 */
	public void cleanup()
	{
		for (final File file : this.dir.listFiles())
		{
			//noinspection ResultOfMethodCallIgnored
			file.delete();
		}
	}

	/**
	 * Process input stream
	 *
	 * @param fin     input stream
	 * @param asTarGz process as tar.ge stream
	 * @return File
	 * @throws IOException io exception
	 */
	@SuppressWarnings("UnusedReturnValue")
	public File process(final InputStream fin, final boolean asTarGz) throws IOException
	{
		if (asTarGz)
		{
			return Deployer.extractTarGz(fin, this.dir, true, ".*/?dic/?.*", ".*/?dbfiles/?.*");
		}
		return treebolic.provider.wordnet.jwi.DataManager.expand(fin, null, this.dir);
	}

	/**
	 * Extract tar.gz stream
	 *
	 * @param fin     input stream
	 * @param destDir destination dir
	 * @param flat    flatten
	 * @param include include regexp filter
	 * @param exclude exclude regexp filter
	 * @throws IOException io exception
	 */
	private static File extractTarGz(final InputStream fin, final File destDir, @SuppressWarnings("SameParameterValue") final boolean flat, @SuppressWarnings("SameParameterValue") final String include, @SuppressWarnings("SameParameterValue") final String exclude) throws IOException
	{
		final Pattern includePattern = include == null ? null : Pattern.compile(include);
		final Pattern excludePattern = exclude == null ? null : Pattern.compile(exclude);

		// prepare destination
		//noinspection ResultOfMethodCallIgnored
		destDir.mkdirs();

		// input stream
		TarArchiveInputStream tarIn = null;
		try
		{
			tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(fin)));

			// loop through entries
			for (TarArchiveEntry tarEntry = tarIn.getNextTarEntry(); tarEntry != null; tarEntry = tarIn.getNextTarEntry())
			{
				String entryName = tarEntry.getName();

				// include
				if (includePattern != null)
				{
					if (!includePattern.matcher(entryName).matches())
					{
						continue;
					}
				}

				// exclude
				if (excludePattern != null)
				{
					if (excludePattern.matcher(entryName).matches())
					{
						continue;
					}
				}

				// switch as per type
				if (tarEntry.isDirectory())
				{
					// create dir if we don't flatten
					if (!flat)
					{
						//noinspection ResultOfMethodCallIgnored
						new File(destDir, entryName).mkdirs();
					}
				}
				else
				{
					// create a file with the same name as the tarEntry
					if (flat)
					{
						final int index = entryName.lastIndexOf('/');
						if (index != -1)
						{
							entryName = entryName.substring(index + 1);
						}
					}

					final File destFile = new File(destDir, entryName);
					Log.d(Deployer.TAG, "Deploying in " + destFile.getCanonicalPath());

					// create destination
					//noinspection ResultOfMethodCallIgnored
					destFile.createNewFile();

					// copy
					BufferedOutputStream bout = null;
					try
					{
						bout = new BufferedOutputStream(new FileOutputStream(destFile));
						final byte[] buffer = new byte[1024];
						for (int len = tarIn.read(buffer); len != -1; len = tarIn.read(buffer))
						{
							bout.write(buffer, 0, len);
						}
					}
					finally
					{
						if (bout != null)
						{
							try
							{
								bout.close();
							}
							catch (IOException ignored)
							{
							}
						}
					}
				}
			}
		}
		finally
		{
			if (tarIn != null)
			{
				try
				{
					tarIn.close();
				}
				catch (IOException ignored)
				{
				}
			}
		}
		return destDir;
	}
}
