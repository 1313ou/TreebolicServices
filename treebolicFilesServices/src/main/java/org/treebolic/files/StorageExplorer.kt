/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.files

import android.content.Context
import android.os.Environment
import android.os.Process
import android.os.UserManager
import android.util.Pair
import java.io.File
import java.util.EnumMap
import java.util.TreeSet

object StorageExplorer {

    /**
     * Get external storage directories
     *
     * @param context context
     * @return map per type of of external storage directories
     */
    fun getStorageDirectories(context: Context): Map<StorageType, Array<String>> {
        // result set of paths
        val dirs: MutableMap<StorageType, Array<String>> = EnumMap(StorageType::class.java)

        // P R I M A R Y

        // primary emulated sdcard
        val emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
        if (emulatedStorageTarget != null && emulatedStorageTarget.isNotEmpty()) {
            // device has emulated extStorage; external extStorage paths should have userId burned into them.
            val userId = getUserId(context)

            // /extStorage/emulated/0[1,2,...]
            if (userId.isEmpty()) {
                dirs[StorageType.PRIMARY_PHYSICAL] = arrayOf(emulatedStorageTarget)
            } else {
                dirs[StorageType.PRIMARY_PHYSICAL] = arrayOf(emulatedStorageTarget + File.separatorChar + userId)
            }
        } else {
            // primary physical sdcard (not emulated)
            val externalStorage = System.getenv("EXTERNAL_STORAGE")

            // device has physical external extStorage; use plain paths
            if (externalStorage != null && externalStorage.isNotEmpty()) {
                dirs[StorageType.PRIMARY_EMULATED] = arrayOf(externalStorage)
            } else {
                // EXTERNAL_STORAGE undefined; falling back to default.
                dirs[StorageType.PRIMARY_EMULATED] = arrayOf("/extStorage/sdcard0")
            }
        }

        // S E C O N D A R Y

        // all secondary sdcards (all exclude primary) separated by ":"
        val secondaryStoragesStr = System.getenv("SECONDARY_STORAGE")

        // add all secondary storages
        if (secondaryStoragesStr != null && secondaryStoragesStr.isNotEmpty()) {
            // all secondary sdcards split into array
            val secondaryStorages = secondaryStoragesStr.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (secondaryStorages.isNotEmpty()) {
                dirs[StorageType.SECONDARY] = secondaryStorages
            }
        }

        return dirs
    }

    /**
     * Get directories as types and values
     *
     * @param context context
     * @return pair of types and values
     */
    fun getDirectoriesTypesValues(context: Context): Pair<Array<CharSequence>, Array<CharSequence>> {
        val types: MutableList<CharSequence> = ArrayList()
        val values: MutableList<CharSequence> = ArrayList()
        val dirs = getDirectories(context)
        for (dir in dirs) {
            // types
            types.add(dir.type.toDisplay())

            // value
            values.add(dir.file!!.absolutePath)
        }
        return Pair(types.toTypedArray<CharSequence>(), values.toTypedArray<CharSequence>())
    }

    /**
     * Get list of directories
     *
     * @param context context
     * @return list of storage directories
     */
    private fun getDirectories(context: Context): Collection<Directory> {
        val tags = arrayOf(
            Environment.DIRECTORY_PODCASTS,
            Environment.DIRECTORY_RINGTONES,
            Environment.DIRECTORY_ALARMS,
            Environment.DIRECTORY_NOTIFICATIONS,
            Environment.DIRECTORY_PICTURES,
            Environment.DIRECTORY_MOVIES,
            Environment.DIRECTORY_DOWNLOADS,
            Environment.DIRECTORY_DCIM
        )

        val result: MutableSet<Directory> = TreeSet()
        var dir: File?

        // P U B L I C

        // top-level public external storage directory
        for (tag in tags) {
            dir = Environment.getExternalStoragePublicDirectory(tag)
            if (dir.exists()) {
                result.add(Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY))
            }
        }
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (dir.exists()) {
            result.add(Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY))
        }

        // top-level public in external
        dir = Environment.getExternalStorageDirectory()
        if (dir != null) {
            if (dir.exists()) {
                result.add(Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY))
            }
        }

        // S E C O N D A R Y

        // all secondary sdcards split into array
        val secondaries = discoverSecondaryExternalStorage()
        if (secondaries != null) {
            for (secondary in secondaries) {
                dir = secondary
                if (dir.exists()) {
                    result.add(Directory(dir, DirType.PUBLIC_EXTERNAL_SECONDARY))
                }
            }
        }

        // P R I M A R Y

        // primary emulated sdcard
        dir = discoverPrimaryEmulatedExternalStorage(context)
        if (dir != null) {
            if (dir.exists()) {
                result.add(Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY))
            }
        }

        dir = discoverPrimaryPhysicalExternalStorage()
        if (dir != null) {
            if (dir.exists()) {
                result.add(Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY))
            }
        }

        result.add(Directory(File("/storage"), DirType.PUBLIC_EXTERNAL_PRIMARY))
        return result
    }

    /**
     * Discover external storage
     *
     * @param context context
     * @return (cached) external storage directory
     */
    fun discoverExternalStorage(context: Context): String? {

        // S E C O N D A R Y

        // all secondary sdcards (all exclude primary) separated by ":"

        val secondaryStoragesStr = System.getenv("SECONDARY_STORAGE")

        // add all secondary storages
        if (secondaryStoragesStr != null && secondaryStoragesStr.isNotEmpty()) {
            // all secondary sdcards split into array
            val secondaryStorages = secondaryStoragesStr.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (secondaryStorages.isNotEmpty()) {
                return secondaryStorages[0]
            }
        }

        // P R I M A R Y   E M U L A T E D

        // primary emulated sdcard
        val emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
        if (emulatedStorageTarget != null && emulatedStorageTarget.isNotEmpty()) {
            // device has emulated extStorage; external extStorage paths should have userId burned into them.
            val userId = getUserId(context)

            // /extStorage/emulated/0[1,2,...]
            if (userId.isNotEmpty()) {
                return emulatedStorageTarget + File.separatorChar + userId
            }
            return emulatedStorageTarget
        }

        // P R I M A R Y   N O N   E M U L A T E D

        // primary physical sdcard (not emulated)
        val externalStorage = System.getenv("EXTERNAL_STORAGE")

        // device has physical external extStorage; use plain paths.
        if (externalStorage != null && externalStorage.isNotEmpty()) {
            return externalStorage
        }
        return null
    }

    /**
     * Discover primary emulated external storage directory
     *
     * @param context context
     * @return primary emulated external storage directory
     */
    private fun discoverPrimaryEmulatedExternalStorage(context: Context): File? {
        // primary emulated sdcard
        val emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
        if (emulatedStorageTarget != null && emulatedStorageTarget.isNotEmpty()) {
            // device has emulated extStorage
            // external extStorage paths should have userId burned into them
            val userId = getUserId(context)

            // /extStorage/emulated/0[1,2,...]
            return if (userId.isEmpty()) {
                File(emulatedStorageTarget)
            } else {
                File(emulatedStorageTarget + File.separatorChar + userId)
            }
        }
        return null
    }

    /**
     * Discover primary physical external storage directory
     *
     * @return primary physical external storage directory
     */
    private fun discoverPrimaryPhysicalExternalStorage(): File? {
        val externalStorage = System.getenv("EXTERNAL_STORAGE")
        // device has physical external extStorage; use plain paths.
        if (externalStorage != null && externalStorage.isNotEmpty()) {
            return File(externalStorage)
        }

        return null
    }

    /**
     * Discover secondary external storage directories
     *
     * @return secondary external storage directories
     */
    private fun discoverSecondaryExternalStorage(): Array<File>? {
        // all secondary sdcards (all except primary) separated by ":"
        var secondaryStoragesEnv = System.getenv("SECONDARY_STORAGE")
        if ((secondaryStoragesEnv == null) || secondaryStoragesEnv.isEmpty()) {
            secondaryStoragesEnv = System.getenv("EXTERNAL_SDCARD_STORAGE")
        }

        // addItem all secondary storages
        if (secondaryStoragesEnv != null && secondaryStoragesEnv.isNotEmpty()) {
            // all secondary sdcards split into array
            val paths = secondaryStoragesEnv.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val dirs: MutableList<File> = ArrayList()
            for (path in paths) {
                val dir = File(path)
                if (dir.exists()) {
                    dirs.add(dir)
                }
            }
            return dirs.toTypedArray<File>()
        }
        return null
    }

    /**
     * User id
     *
     * @param context context
     * @return user id
     */
    private fun getUserId(context: Context): String {
        val manager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val user = Process.myUserHandle()
        val userSerialNumber = manager.getSerialNumberForUser(user)
        return userSerialNumber.toString()
    }

    /**
     * Storage types
     */
    enum class StorageType {

        PRIMARY_EMULATED, PRIMARY_PHYSICAL, SECONDARY
    }

    /**
     * Directory type
     *
     * @author [Bernard Bou](mailto:1313ou@gmail.com)
     */
    enum class DirType {

        AUTO, APP_EXTERNAL_SECONDARY, APP_EXTERNAL_PRIMARY, PUBLIC_EXTERNAL_SECONDARY, PUBLIC_EXTERNAL_PRIMARY, APP_INTERNAL;

        fun toDisplay(): String {
            return when (this) {
                AUTO -> "auto (internal or adopted)"
                APP_EXTERNAL_SECONDARY -> "secondary"
                APP_EXTERNAL_PRIMARY -> "primary"
                PUBLIC_EXTERNAL_PRIMARY -> "public primary"
                PUBLIC_EXTERNAL_SECONDARY -> "public secondary"
                APP_INTERNAL -> "internal"
            }
        }

        companion object {

            /**
             * Compare (sort by preference)
             *
             * @param type1 type 1
             * @param type2 type 2
             * @return order
             */
            fun compare(type1: DirType, type2: DirType): Int {
                val i1 = type1.ordinal
                val i2 = type2.ordinal
                return i1.compareTo(i2)
            }
        }
    }

    /**
     * Directory with type
     *
     * @author [Bernard Bou](mailto:1313ou@gmail.com)
     */
    class Directory internal constructor(val file: File?, val type: DirType) : Comparable<Directory> {

        private val value: CharSequence
            get() {
                if (DirType.AUTO == type) {
                    return DirType.AUTO.toString()
                }
                return file!!.absolutePath
            }

        override fun hashCode(): Int {
            return type.hashCode() * 7 + value.hashCode() * 13
        }

        override fun equals(other: Any?): Boolean {
            return (other is Directory) && type == other.type
        }

        override fun compareTo(other: Directory): Int {
            val t = DirType.compare(type, other.type)
            if (t != 0) {
                return t
            }
            return value.toString().compareTo(other.value.toString())
        }
    }
}
