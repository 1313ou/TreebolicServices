package org.treebolic.services;

import android.support.annotation.Nullable;

import treebolic.model.Model;

public interface IModelFactory
{
	/**
	 * Make model
	 *
	 * @param source    source
	 * @param base      base
	 * @param imageBase image base
	 * @param settings  settings
	 * @return model
	 */
	@Nullable
	Model make(final String source, final String base, final String imageBase, final String settings);
}
