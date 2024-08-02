/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.services.iface

/**
 * Service interface used in client/service interaction
 *
 * @author Bernard Bou
 */
interface ITreebolicService {

    /**
     * Get service url scheme
     *
     * @return service url scheme
     */
    val urlScheme: String

    companion object {

        // broadcast service
        const val ACTION_MAKEMODEL: String = "org.treebolic.service.action.MAKE_MODEL"

        // messaging service
        const val MSG_REGISTER_CLIENT: Int = 1

        const val MSG_UNREGISTER_CLIENT: Int = 2

        const val MSG_REQUEST_MODEL: Int = 3

        const val MSG_RESULT_MODEL: Int = 4

        // arguments
        const val EXTRA_SOURCE: String = "org.treebolic.service.extra.SOURCE"

        const val EXTRA_BASE: String = "org.treebolic.service.extra.BASE"

        const val EXTRA_IMAGEBASE: String = "org.treebolic.service.extra.IMAGEBASE"

        const val EXTRA_SETTINGS: String = "org.treebolic.service.extra.SETTINGS"

        const val EXTRA_RECEIVER: String = "org.treebolic.service.extra.RECEIVER"

        const val EXTRA_FORWARD_RESULT_TO: String = "org.treebolic.service.FORWARDTO"

        // result
        const val RESULT_MODEL: String = "org.treebolic.service.MODEL"

        const val RESULT_SERIALIZED: String = "org.treebolic.service.SERIALIZED"

        const val RESULT_URLSCHEME: String = "org.treebolic.service.URLSCHEME"

        // result
        const val TYPE_BROADCAST: String = "Broadcast"

        const val TYPE_AIDL_BOUND: String = "AIDLBound"

        const val TYPE_BOUND: String = "Bound"

        const val TYPE_MESSENGER: String = "Messenger"
    }
}
