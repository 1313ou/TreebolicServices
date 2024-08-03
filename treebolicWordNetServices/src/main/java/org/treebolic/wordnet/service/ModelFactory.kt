/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.wordnet.service

import android.content.Context
import android.util.Log
import org.treebolic.services.ModelFactory
import org.treebolic.services.Utils.makeLogProviderContext
import treebolic.ILocator
import treebolic.provider.wordnet.jwi.simple.Provider
import java.net.MalformedURLException
import java.net.URL

/**
 * Model factory
 *
 * @param context context
 *
 * @author Bernard Bou
 */
class ModelFactory(context: Context) : ModelFactory(if (SIMPLE) Provider() else treebolic.provider.wordnet.jwi.full.Provider(), makeLogProviderContext(TAG), makeLocator(context), context.applicationContext) {

    override fun useFilesDir(): Boolean {
        return true
    }

    override val sourceAliases: Array<String>
        get() = arrayOf("word")

    companion object {

        private const val TAG = "WnSModelFactory"

        /**
         * Type of provider
         */
        private const val SIMPLE = true

        private fun makeLocator(context: Context): ILocator {
            try {
                return object : ILocator {
                    private val base: URL = context.filesDir.toURI().toURL()

                    override fun getBase(): URL {
                        return this.base
                    }

                    override fun getImagesBase(): URL {
                        return this.base
                    }
                }
            } catch (mue: MalformedURLException) {
                Log.e(TAG, context.filesDir.toURI().toString(), mue)
                throw RuntimeException(mue)
            }
        }
    }
}
