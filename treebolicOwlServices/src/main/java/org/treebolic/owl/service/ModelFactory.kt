/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.owl.service

import android.content.Context
import android.util.Log
import org.treebolic.services.ModelFactory
import org.treebolic.services.Utils.makeLogProviderContext
import org.treebolic.storage.Storage.getTreebolicStorage
import treebolic.ILocator
import treebolic.provider.owl.Provider2
import java.net.MalformedURLException
import java.net.URL

/**
 * Owl Model factory
 *
 * @param context context
 *
 * @author Bernard Bou
 */
class ModelFactory(context: Context) : ModelFactory(Provider2(), makeLogProviderContext(TAG), makeLocator(context), context.applicationContext) {

    override val sourceAliases: Array<String>
        get() = arrayOf("owl")

    companion object {

        private const val TAG = "OwlModelFactory"

        private fun makeLocator(context: Context): ILocator {
            try {
                return object : ILocator {
                    // Used when the base url is null
                    private val base: URL = getTreebolicStorage(context.applicationContext).toURI().toURL()

                    override fun getBase(): URL {
                        return base
                    }

                    override fun getImagesBase(): URL {
                        return base
                    }
                }
            } catch (mue: MalformedURLException) {
                Log.e(TAG, context.filesDir.toURI().toString(), mue)
                throw RuntimeException(mue)
            }
        }
    }
}
