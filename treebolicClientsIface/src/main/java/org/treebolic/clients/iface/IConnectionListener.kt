/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.clients.iface

/**
 * Model consumer interface
 *
 * @author Bernard Bou
 */
interface IConnectionListener {

    /**
     * Connected callback
     *
     * @param success whether connection is successful
     */
    fun onConnected(success: Boolean)
}
