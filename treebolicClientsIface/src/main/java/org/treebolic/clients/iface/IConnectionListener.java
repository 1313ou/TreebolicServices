/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.clients.iface;

/**
 * Model consumer interface
 *
 * @author Bernard Bou
 */
public interface IConnectionListener
{
	/**
	 * Connected callback
	 *
	 * @param success whether connection is successful
	 */
	void onConnected(@SuppressWarnings("SameParameterValue") boolean success);
}
