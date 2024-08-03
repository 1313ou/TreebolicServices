/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.wordnet.service

import android.util.Log
import org.treebolic.services.TreebolicMessengerService

/**
 * Treebolic WordNet bound messenger service
 *
 * @author Bernard Bou
 */
class TreebolicWordNetMessengerService : TreebolicMessengerService() {

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

        private const val TAG = "TWordNetMessengerS"
    }
}
