/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.owl.service

import org.treebolic.services.TreebolicAIDLBoundService
import java.io.IOException

/**
 * Bound service for Owl data
 */
class TreebolicOwlAIDLBoundService : TreebolicAIDLBoundService() {

    @Throws(IOException::class)
    override fun onCreate() {
        super.onCreate()
        factory = ModelFactory(this)
    }

    override val urlScheme: String
        get() = "owl:"
}
