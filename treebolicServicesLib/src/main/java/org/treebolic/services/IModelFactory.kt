/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.services

import treebolic.model.Model

interface IModelFactory {

    /**
     * Make model
     *
     * @param source    source
     * @param base      base
     * @param imageBase image base
     * @param settings  settings
     * @return model
     */
    fun make(source: String, base: String?, imageBase: String?, settings: String?): Model?
}
