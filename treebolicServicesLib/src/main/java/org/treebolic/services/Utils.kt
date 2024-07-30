/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.services

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import treebolic.provider.IProviderContext

object Utils {

    /**
     * Make provider locatorContext
     *
     * @return provider locatorContext
     */
    @JvmStatic
    fun makeLogProviderContext(tag: String?): IProviderContext {
        return object : IProviderContext {
            override fun message(text: String) {
                Log.d(tag, "Message:$text")
            }

            override fun progress(text: String, fail: Boolean) {
                Log.d(tag, "Progress:$text")
            }

            override fun warn(text: String) {
                Log.d(tag, "Warn:$text")
            }
        }
    }

    /**
     * Warning to run on UI thread
     *
     * @param context   context
     * @param messageId massage resource id
     */
    @JvmStatic
    fun warn(context: Context, @StringRes messageId: Int) {
        Handler(Looper.getMainLooper()).post { Toast.makeText(context, messageId, Toast.LENGTH_LONG).show() }
    }
}
