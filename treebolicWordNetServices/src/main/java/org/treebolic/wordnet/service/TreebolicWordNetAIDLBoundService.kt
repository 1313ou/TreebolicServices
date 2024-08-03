/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.wordnet.service

import android.util.Log
import org.treebolic.services.TreebolicAIDLBoundService

/**
 * Bound service for WordNet data
 */
class TreebolicWordNetAIDLBoundService : TreebolicAIDLBoundService() {

    override fun onCreate() {
        super.onCreate()
        try {
            this.factory = ModelFactory(this)
        } catch (e: Exception) {
            Log.e(TAG, "Model factory constructor failed", e)
        }
    }

    override val urlScheme: String
        get() = "wordnet:"

    companion object {

        private const val TAG = "TWordNetAIDLS"
    }
}
