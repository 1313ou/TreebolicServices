/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.owl

import java.io.File
import java.io.InputStream

/**
 * Data deployer
 *
 * @param parentDir parent directory
 *
 * @author Bernard Bou
 */
class Deployer(parentDir: File?) {

    /**
     * Parent directory
     */
    private val dir: File = File(parentDir, PATH)

    /**
     * Process download stream (such as expanding download zipped stream, copy file, etc)
     *
     * @param fin file input stream
     * @return directory
     */
    fun process(fin: InputStream?): File {
        return dir
    }

    companion object {

        /**
         * Directory path
         */
        private const val PATH = "treebolic"
    }
}
