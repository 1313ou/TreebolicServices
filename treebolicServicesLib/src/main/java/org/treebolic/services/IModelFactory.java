/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.services;

import androidx.annotation.Nullable;
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
