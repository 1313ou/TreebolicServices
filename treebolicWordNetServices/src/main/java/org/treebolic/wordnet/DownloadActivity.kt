/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.wordnet

import android.os.Bundle
import android.widget.Toast
import org.treebolic.download.DownloadActivity
import java.io.IOException
import java.io.InputStream

/**
 * WordNet download activity
 *
 * @author Bernard Bou
 */
class DownloadActivity : DownloadActivity() {

    /**
     * Whether stream is tar.gz (zip otherwise)
     */
    private var asTarGz = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.downloadUrl = Settings.getStringPref(this, Settings.PREF_DOWNLOAD)
        if (this.downloadUrl == null || downloadUrl!!.isEmpty()) {
            Toast.makeText(this, R.string.error_null_download_url, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    public override fun start() {
        checkNotNull(this.downloadUrl)
        this.asTarGz = downloadUrl!!.endsWith(".tar.gz")
        super.start(R.string.wordnet)
    }

    override fun doProcessing(): Boolean {
        return true
    }

    @Throws(IOException::class)
    override fun process(inputStream: InputStream): Boolean {
        Deployer(this@DownloadActivity.filesDir).process(inputStream, this.asTarGz)
        return true
    }
}
