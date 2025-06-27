/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.services

import android.content.Context
import android.util.Log
import treebolic.ILocator
import treebolic.model.Model
import treebolic.model.ModelDump
import treebolic.provider.IProvider
import treebolic.provider.IProviderContext
import java.net.MalformedURLException
import java.net.URL
import java.util.Properties

/**
 * Model factory
 *
 * @param provider           provider
 * @param providerContext    provider context
 * @param locatorContext     locator context
 * @param applicationContext application context
 *
 * @author Bernard Bou
 */
abstract class ModelFactory(
    protected val provider: IProvider,
    private val providerContext: IProviderContext,
    private val locatorContext: ILocator,
    private val applicationContext: Context

) : IModelFactory {

    /**
     * Source aliases
     */
    protected open val sourceAliases: Array<String>?
        get() = null

    /**
     * What is returned by provider locatorContext getDataDir()
     *
     * @return true if locatorContext.getFilesDir, false if base
     */
    protected open fun useFilesDir(): Boolean {
        return false
    }

    override fun make(source: String, base: String?, imageBase: String?, settings: String?): Model? {
        // provider
        provider.setContext(providerContext)
        provider.setLocator(locatorContext)
        provider.setHandle(applicationContext)

        // model
        val baseUrl = makeBaseURL(base)
        val model = provider.makeModel(source, baseUrl, makeParameters(source, base, imageBase, settings))
        Log.d(TAG, "Made model" + (if (BuildConfig.DEBUG) "\n${ModelDump.toString(model)}\n" else " $model"))
        return model
    }

    /**
     * Make base URL
     *
     * @param base base
     * @return base URL
     */
    private fun makeBaseURL(base: String?): URL? {
        if (base == null) {
            return locatorContext.base
        } else {
            try {
                return URL(if (!base.endsWith("/")) "$base/" else base)
            } catch (ignored: MalformedURLException) {
                
            }
            return null
        }
    }

    /**
     * Make parameters
     *
     * @param source    source
     * @param base      base
     * @param imageBase image base
     * @param settings  settings
     * @return parameters
     */
    private fun makeParameters(source: String?, base: String?, imageBase: String?, settings: String?): Properties {
        val parameters = Properties()
        if (source != null) {
            parameters.setProperty("source", source)
        }
        if (base != null) {
            parameters.setProperty("base", base)
        }
        if (imageBase != null) {
            parameters.setProperty("imagebase", imageBase)
        }
        if (settings != null) {
            parameters.setProperty("settings", settings)
        }

        // source aliases
        val sourceAliases = sourceAliases
        if (source != null && sourceAliases != null) {
            for (sourceAlias in sourceAliases) {
                parameters.setProperty(sourceAlias, source)
            }
        }

        return parameters
    }

    companion object {

        private const val TAG = "AModelFactory"
    }
}
