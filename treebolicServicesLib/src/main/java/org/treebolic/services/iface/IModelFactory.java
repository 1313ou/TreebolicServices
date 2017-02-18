package org.treebolic.services.iface;

import java.io.IOException;

import treebolic.model.Model;

public interface IModelFactory
{
	/**
	 * Make model
	 *
	 * @param source
	 *            source
	 * @param base
	 *            base
	 * @param imageBase
	 *            image base
	 * @param settings
	 *            settings
	 * @return model
	 * @throws IOException
	 */
	public Model make(final String source, final String base, final String imageBase, final String settings) throws Exception;
}
