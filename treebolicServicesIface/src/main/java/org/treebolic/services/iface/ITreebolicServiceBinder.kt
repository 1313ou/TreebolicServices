/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.services.iface

import android.content.Intent
import org.treebolic.clients.iface.IModelListener

interface ITreebolicServiceBinder {

    /**
     * Make model from source and pass to consumer
     *
     * @param source        source
     * @param base          base
     * @param imageBase     image base
     * @param settings      settings
     * @param modelListener listener model listener
     */
    fun makeModel(source: String, base: String?, imageBase: String?, settings: String?, modelListener: IModelListener)

    /**
     * Make model from source and forward it to activity
     *
     * @param source    source
     * @param base      base
     * @param imageBase image base
     * @param settings  settings
     * @param forward   forward intent
     */
    fun makeModel(source: String, base: String?, imageBase: String?, settings: String?, forward: Intent)
}
