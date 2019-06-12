/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.services.iface;

import android.content.Intent;

import org.treebolic.clients.iface.IModelListener;

public interface ITreebolicServiceBinder
{
	/**
	 * Make model from source and pass to consumer
	 *
	 * @param source        source
	 * @param base          base
	 * @param imageBase     image base
	 * @param settings      settings
	 * @param modelListener listener model listener
	 */
	void makeModel(final String source, final String base, final String imageBase, final String settings, final IModelListener modelListener);

	/**
	 * Make model from source and forward it to activity
	 *
	 * @param source    source
	 * @param base      base
	 * @param imageBase image base
	 * @param settings  settings
	 * @param forward   forward intent
	 */
	void makeModel(final String source, final String base, final String imageBase, final String settings, final Intent forward);
}
