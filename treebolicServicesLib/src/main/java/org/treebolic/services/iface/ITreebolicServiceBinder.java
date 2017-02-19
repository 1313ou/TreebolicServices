package org.treebolic.services.iface;

import org.treebolic.clients.iface.IModelListener;

import android.content.Intent;

public interface ITreebolicServiceBinder
{
	/**
	 * Make model from source and pass to consumer
	 *
	 * @param source
	 *            source
	 * @param base
	 *            base
	 * @param imageBase
	 *            image base
	 * @param settings
	 *            settings
	 * @param modelListener
	 *            listener model listener
	 */
	void makeModel(final String source, final String base, final String imageBase, final String settings, final IModelListener modelListener);

	/**
	 * Make model from source and forward it to activity
	 *
	 * @param source
	 *            source
	 * @param base
	 *            base
	 * @param imageBase
	 *            image base
	 * @param settings
	 *            settings
	 * @param forward
	 *            forward intent
	 */
	void makeModel(final String source, final String base, final String imageBase, final String settings, final Intent forward);
}
