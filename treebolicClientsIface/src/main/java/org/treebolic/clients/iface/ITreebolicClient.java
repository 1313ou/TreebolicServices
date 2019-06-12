/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.clients.iface;

import android.content.Intent;

/**
 * Interface to client
 *
 * @author Bernard Bou
 */
public interface ITreebolicClient
{
	/**
	 * Connect
	 */
	void connect();

	/**
	 * Disconnect
	 */
	void disconnect();

	/**
	 * Request model from source
	 *
	 * @param source    source
	 * @param base      document base
	 * @param imageBase base image base
	 * @param settings  settings
	 * @param forward   forward intent
	 */
	void requestModel(String source, String base, String imageBase, String settings, Intent forward);
}
