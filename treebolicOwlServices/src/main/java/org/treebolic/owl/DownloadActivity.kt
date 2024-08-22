/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.owl

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import org.treebolic.download.Deploy.copy
import org.treebolic.download.Deploy.expand
import org.treebolic.owl.Settings.getStringPref
import org.treebolic.storage.Storage.getTreebolicStorage
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * Owl download activity
 *
 * @author Bernard Bou
 */
class DownloadActivity : org.treebolic.download.DownloadActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        expandArchiveCheckbox!!.visibility = View.VISIBLE
        downloadUrl = getStringPref(this, Settings.PREF_DOWNLOAD)
        if (downloadUrl == null || downloadUrl!!.isEmpty()) {
            Toast.makeText(this, R.string.error_null_download_url, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    public override fun start() {
        start(R.string.owl)
    }

    // P O S T P R O C E S S I N G

    override fun doProcessing(): Boolean {
        return true
    }

    @Throws(IOException::class)
    override fun process(inputStream: InputStream): Boolean {
        val storage = getTreebolicStorage(this)

        if (expandArchive) {
            expand(inputStream, getTreebolicStorage(this), false)
            return true
        }
        val downloadUri = Uri.parse(downloadUrl)
        val lastSegment = downloadUri.lastPathSegment
        if (lastSegment != null) {
            val destFile = File(storage, lastSegment)
            copy(inputStream, destFile)
            return true
        }
        return false
    }
}
