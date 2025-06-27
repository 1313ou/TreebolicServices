/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.services

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import org.treebolic.ParcelableModel
import org.treebolic.TreebolicIface
import org.treebolic.services.iface.ITreebolicService
import treebolic.model.Model

object IntentFactory {

    /**
     * Make intent
     *
     * @param model        model
     * @param parentIntent parent activity to return to
     * @param base         base
     * @param imageBase    image base
     * @param settings     settings
     * @return intent
     */
    fun makeTreebolicIntent(model: Model?, parentIntent: Intent?, base: String?, imageBase: String?, settings: String?): Intent {
        val intent = makeTreebolicIntentSkeleton(parentIntent, base, imageBase, settings)
        intent.putExtra(TreebolicIface.ARG_MODEL, ParcelableModel(model))
        return intent
    }

    /**
     * Treebolic skeleton intent without model)
     *
     * @param parentIntent parent activity to return to
     * @param base         base
     * @param imageBase    image base
     * @param settings     settings
     * @return treebolic model activity intent
     */
    @JvmStatic
    fun makeTreebolicIntentSkeleton(parentIntent: Intent?, base: String?, imageBase: String?, settings: String?): Intent {
        val intent = Intent()
        intent.component = ComponentName(TreebolicIface.PKG_TREEBOLIC, TreebolicIface.ACTIVITY_MODEL)
        intent.putExtra(TreebolicIface.ARG_BASE, base)
        intent.putExtra(TreebolicIface.ARG_IMAGEBASE, imageBase)
        intent.putExtra(TreebolicIface.ARG_SETTINGS, settings)
        intent.putExtra(TreebolicIface.ARG_PARENTACTIVITY, parentIntent)
        return intent
    }

    /**
     * Put model in activity intent
     *
     * @param bundle    bundle
     * @param model     model to forward
     * @param urlScheme url scheme
     */
    fun putModelResult(bundle: Bundle, model: Model?, urlScheme: String?) {
        if (ParcelableModel.SERIALIZE) {
            bundle.putBoolean(ITreebolicService.RESULT_SERIALIZED, true)
            bundle.putSerializable(ITreebolicService.RESULT_MODEL, model)
        } else {
            bundle.putBoolean(ITreebolicService.RESULT_SERIALIZED, false)
            bundle.putParcelable(ITreebolicService.RESULT_MODEL, ParcelableModel(model))
        }
        bundle.putString(ITreebolicService.RESULT_URLSCHEME, urlScheme)
    }

    /**
     * Put model in activity intent
     *
     * @param bundle    bundle
     * @param model     model to forward
     * @param urlScheme url scheme
     */
    private fun putModelArg(bundle: Bundle, model: Model?, urlScheme: String?) {
        if (ParcelableModel.SERIALIZE) {
            bundle.putBoolean(TreebolicIface.ARG_SERIALIZED, true)
            bundle.putSerializable(TreebolicIface.ARG_MODEL, model)
        } else {
            bundle.putBoolean(TreebolicIface.ARG_SERIALIZED, false)
            bundle.putParcelable(TreebolicIface.ARG_MODEL, ParcelableModel(model))
        }
        bundle.putString(TreebolicIface.ARG_URLSCHEME, urlScheme)
    }

    /**
     * Put model in activity intent as argument
     *
     * @param forwardIntent forward intent
     * @param model         model to forward
     */
    fun putModelArg(forwardIntent: Intent, model: Model?, urlScheme: String?) {
        val forwardBundle = Bundle()
        putModelArg(forwardBundle, model, urlScheme)
        forwardIntent.putExtras(forwardBundle)
    }
}
