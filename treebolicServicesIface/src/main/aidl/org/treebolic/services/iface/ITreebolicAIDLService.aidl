package org.treebolic.services.iface;

import org.treebolic.ParcelableModel;

import android.content.Intent;
import android.os.ResultReceiver;

interface ITreebolicAIDLService
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
	 * @param resultReceiver
	 *            result receiver
	 */
	void makeModel(in String source, in String base, in String imageBase, in String settings, in ResultReceiver resultReceiver);

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
	void makeAndForwardModel(in String source, in String base, in String imageBase, in String settings, in Intent forward);
}
