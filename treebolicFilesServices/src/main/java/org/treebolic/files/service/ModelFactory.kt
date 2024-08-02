/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.files.service

import android.content.Context
import org.treebolic.services.ModelFactory
import org.treebolic.services.Utils.makeLogProviderContext
import treebolic.ILocator
import treebolic.provider.files.Provider2
import java.net.URL

/**
 * Model factory
 *
 * @param context context
 *
 * @author Bernard Bou
 */
class ModelFactory(context: Context) : ModelFactory(Provider2(), makeLogProviderContext(TAG), makeLocator(context), context.applicationContext) {

    override val sourceAliases: Array<String>
        get() = arrayOf("files")

    companion object {

        private const val TAG = "FilesModelFactory"

        private fun makeLocator(context: Context): ILocator {
            // Not used
            return object : ILocator {
                override fun getBase(): URL? {
                    return null
                }

                override fun getImagesBase(): URL? {
                    return null
                }
            }
        }
    }
}
