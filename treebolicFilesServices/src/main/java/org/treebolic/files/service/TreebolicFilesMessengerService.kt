/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.files.service

import org.treebolic.services.TreebolicMessengerService

/**
 * Treebolic Files messenger bound service
 *
 * @author Bernard Bou
 */
class TreebolicFilesMessengerService : TreebolicMessengerService() {

    override fun onCreate() {
        super.onCreate()
        factory = ModelFactory(this)
    }

    override val urlScheme: String
        get() = "directory:"
}
