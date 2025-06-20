/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.clients.iface

import treebolic.model.Model

/**
 * Model consumer interface
 *
 * @author Bernard Bou
 */
interface IModelListener {

    /**
     * Model available callback
     *
     * @param model     model
     * @param modelUrlScheme url scheme
     */
    fun onModel(model: Model?, modelUrlScheme: String?)
}
