/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.owl.service

import android.content.Context
import org.treebolic.services.IModelFactory
import org.treebolic.services.TreebolicBroadcastService
import java.io.IOException

/**
 * Treebolic Owl broadcast service
 */
class TreebolicOwlBroadcastService : TreebolicBroadcastService() {

    @Throws(IOException::class)
    override fun createModelFactory(context: Context): IModelFactory {
        return ModelFactory(context)
    }

    override val urlScheme: String
        get() = "owl:"
}
