/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.files.service

import android.annotation.SuppressLint
import org.treebolic.services.TreebolicBoundService

/**
 * Bound service for Files data
 */
@SuppressLint("Registered")
class TreebolicFilesBoundService : TreebolicBoundService() {

    override fun onCreate() {
        factory = ModelFactory(this)
        super.onCreate()
    }

    override val urlScheme: String
        get() = "directory:"
}
