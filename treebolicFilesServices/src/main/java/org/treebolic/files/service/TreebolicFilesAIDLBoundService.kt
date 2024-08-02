/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.files.service

import org.treebolic.services.TreebolicAIDLBoundService

/**
 * Bound service for Files data
 */
class TreebolicFilesAIDLBoundService : TreebolicAIDLBoundService() {

    override fun onCreate() {
        super.onCreate()
        this.factory = ModelFactory(this)
    }

    override val urlScheme: String
        get() = "directory:"
}
