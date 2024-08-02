/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.owl.service

import org.treebolic.services.TreebolicMessengerService
import java.io.IOException

/**
 * Treebolic Owl bound messenger service
 *
 * @author Bernard Bou
 */
class TreebolicOwlMessengerService : TreebolicMessengerService() {

    @Throws(IOException::class)
    override fun onCreate() {
        super.onCreate()
        factory = ModelFactory(this)
    }

    override val urlScheme: String
        get() = "owl:"
}
