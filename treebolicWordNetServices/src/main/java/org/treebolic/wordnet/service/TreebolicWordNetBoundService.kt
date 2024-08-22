/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.wordnet.service

import android.util.Log
import org.treebolic.services.TreebolicBoundService

/**
 * Bound service for WordNet data
 */
class TreebolicWordNetBoundService : TreebolicBoundService() {

    override fun onCreate() {
        try {
            factory = ModelFactory(this)
        } catch (e: Exception) {
            Log.e(TAG, "Model factory constructor failed", e)
        }
        super.onCreate()
    }

    override val urlScheme: String
        get() = "wordnet:"

    companion object {

        private const val TAG = "TWordNetBoundS"
    }
}
