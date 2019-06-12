/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.owl.service;

import java.io.File;
import java.io.InputStream;

import androidx.annotation.NonNull;

/**
 * Data deployer
 *
 * @author Bernard Bou
 */
@SuppressWarnings("WeakerAccess")
public class Deployer
{
	/**
	 * Directory path
	 */
	static private final String PATH = "treebolic";

	/**
	 * Parent directory
	 */
	@NonNull
	private final File dir;

	/**
	 * Constructor
	 *
	 * @param parentDir parent directory
	 */
	public Deployer(final File parentDir)
	{
		this.dir = new File(parentDir, Deployer.PATH);
	}

	/**
	 * Process download stream (such as expanding download zipped stream, copy file, etc)
	 *
	 * @param fin file input stream
	 * @return directory
	 */
	@NonNull
	public File process(final InputStream fin)
	{
		return this.dir;
	}
}
