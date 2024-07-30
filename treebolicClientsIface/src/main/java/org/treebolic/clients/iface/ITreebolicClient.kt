/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.clients.iface

import android.content.Intent

/**
 * Interface to client
 *
 * @author Bernard Bou
 */
interface ITreebolicClient {

    /**
     * Connect
     */
    fun connect()

    /**
     * Disconnect
     */
    fun disconnect()

    /**
     * Request model from source
     *
     * @param source    source
     * @param base      document base
     * @param imageBase base image base
     * @param settings  settings
     * @param forward   forward intent
     */
    fun requestModel(source: String, base: String?, imageBase: String?, settings: String?, forward: Intent?)
}
