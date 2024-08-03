/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.wordnet

import android.util.Log
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import treebolic.provider.wordnet.jwi.DataManager
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.regex.Pattern

/**
 * Source data deployer
 *
 * @author Bernard Bou
 */
class Deployer(dir0: File?) {

    /**
     * Dir to write data to
     */
    private val dir: File = File(dir0, PATH)

    /**
     * Data status
     *
     * @return data status
     */
    fun status(): Boolean {
        return DataManager.coreCheck(this.dir)
    }

    /**
     * Clean up data
     */
    fun cleanup() {
        val dirContent = dir.listFiles()
        if (dirContent != null) {
            for (file in dirContent) {
                file.delete()
            }
        }
    }

    /**
     * Process input stream
     *
     * @param fin     input stream
     * @param asTarGz process as tar.ge stream
     * @return File
     * @throws IOException io exception
     */
    @Throws(IOException::class)
    fun process(fin: InputStream, asTarGz: Boolean): File {
        if (asTarGz) {
            return extractTarGz(fin, this.dir, true, ".*/?dic/?.*", ".*/?dbfiles/?.*")
        }
        return DataManager.expand(fin, null, this.dir)
    }

    companion object {

        private const val TAG = "WnSDeployer"

        /**
         * Sub path in main directory
         */
        private const val PATH = "wordnet"

        /**
         * Extract tar.gz stream
         *
         * @param fin     input stream
         * @param destDir destination dir
         * @param flat    flatten
         * @param include include regexp filter
         * @param exclude exclude regexp filter
         * @throws IOException io exception
         */
        @Throws(IOException::class)
        private fun extractTarGz(fin: InputStream, destDir: File, flat: Boolean, include: String?, exclude: String?): File {
            val includePattern = if (include == null) null else Pattern.compile(include)
            val excludePattern = if (exclude == null) null else Pattern.compile(exclude)

            // prepare destination
            destDir.mkdirs()

            TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(fin))).use { tarIn ->

                // loop through entries
                var tarEntry = tarIn.nextEntry
                while (tarEntry != null) {
                    var entryName = tarEntry.name

                    // include
                    if (includePattern != null) {
                        if (!includePattern.matcher(entryName).matches()) {
                            tarEntry = tarIn.nextEntry
                            continue
                        }
                    }

                    // exclude
                    if (excludePattern != null) {
                        if (excludePattern.matcher(entryName).matches()) {
                            tarEntry = tarIn.nextEntry
                            continue
                        }
                    }

                    // switch as per type
                    if (tarEntry.isDirectory) {
                        // create dir if we don't flatten
                        if (!flat) {
                            File(destDir, entryName).mkdirs()
                        }
                    } else {
                        // create a file with the same name as the tarEntry
                        if (flat) {
                            val index = entryName.lastIndexOf('/')
                            if (index != -1) {
                                entryName = entryName.substring(index + 1)
                            }
                        }

                        val destFile = File(destDir, entryName)
                        Log.d(TAG, "Deploying in " + destFile.canonicalPath)

                        // create destination
                        destFile.createNewFile()

                        BufferedOutputStream(FileOutputStream(destFile)).use { bout ->
                            val buffer = ByteArray(1024)
                            var len = tarIn.read(buffer)
                            while (len != -1) {
                                bout.write(buffer, 0, len)
                                len = tarIn.read(buffer)
                            }
                        }
                    }
                    tarEntry = tarIn.nextEntry
                }
            }
            return destDir
        }
    }
}
