/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import treebolic.model.Model
import java.lang.ref.WeakReference

object Models {

    /**
     * Map of model references
     */
    @SuppressLint("UseSparseArrays")
    private val references = HashMap<Long, WeakReference<Model>>()

    // T O / F R O M   I N T E N T

    @JvmStatic
    fun set(model: Model, intent: Intent) {
        val key = set(model)
        intent.putExtra(TreebolicIface.ARG_MODEL_REFERENCE, key)
    }

    fun get(intent: Intent): Model {
        val key = intent.getLongExtra(TreebolicIface.ARG_MODEL_REFERENCE, -1)
        return get(key)
    }

    // T O / F R O M   B U N D L E

    fun set(model: Model, bundle: Bundle) {
        val key = set(model)
        bundle.putLong(TreebolicIface.ARG_MODEL_REFERENCE, key)
    }

    fun get(bundle: Bundle): Model {
        val key = bundle.getLong(TreebolicIface.ARG_MODEL_REFERENCE, -1)
        return get(key)
    }

    // T I M E S T A M P   K E Y

    fun set(model: Model): Long {
        val key = SystemClock.elapsedRealtime()
        set(key, model)
        return key
    }

    // F R O M   K E Y

    fun set(key: Long, model: Model) {
        val reference = WeakReference(model)
        references[key] = reference
    }

    private fun getUnguarded(key: Long): Model? {
        val reference = references[key]
        if (reference != null) {
            return reference.get()
        }
        return null
    }

    @JvmStatic
    @Throws(NoSuchElementException::class)
    fun get(key: Long): Model {
        val model = getUnguarded(key) ?: throw NoSuchElementException(key.toString())
        return model
    }
}
