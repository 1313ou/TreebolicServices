/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.wordnet.service

import android.content.Context
import android.util.Log
import org.treebolic.services.IModelFactory
import org.treebolic.services.TreebolicBroadcastService
import java.io.IOException

/**
 * Treebolic WordNet broadcast service
 */
class TreebolicWordNetBroadcastService : TreebolicBroadcastService() {

    @Throws(IOException::class)
    override fun createModelFactory(context: Context): IModelFactory {
        try {
            return ModelFactory(context)
        } catch (e: IOException) {
            Log.e(TAG, "Model factory constructor failed", e)
            throw e
        }
    }

    override val urlScheme: String
        get() = "wordnet:"

    companion object {

        private const val TAG = "TWordNetBroadcastS"
    }
}