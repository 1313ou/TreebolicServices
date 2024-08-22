/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.wordnet

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import org.treebolic.AppCompatCommonActivity
import java.io.FileInputStream
import java.io.IOException

/**
 * Convenience class for provider to acquire database data from provider
 *
 * @author Bernard Bou
 */
class QueryProviderActivity : AppCompatCommonActivity() {

    /**
     * Data deployer
     */
    private var deployer: Deployer? = null

    /**
     * Activity result launcher
     */
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view
        setContentView(R.layout.activity_query_file_provider)

        // deployer
        deployer = Deployer(filesDir)

        // activity result launcher
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val success = result.resultCode == RESULT_OK
            if (success) {
                // retrieve arguments
                val returnIntent = result.data
                if (returnIntent != null) {
                    val uri = returnIntent.data
                    if (uri != null) {
                        val path = uri.path
                        val asTarGz = path != null && path.endsWith(".tar.gz")
                        var fileDescriptor: ParcelFileDescriptor? = null
                        var fin: FileInputStream? = null

                        // try to open the file for "read" access using the returned URI. If the file isn't found, write to the error log and return.
                        try {
                            // get the content resolver instance for this context, and use it to get a ParcelFileDescriptor for the file.
                            fileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                            if (fileDescriptor != null) {
                                // get a regular file descriptor for the file
                                val fd = fileDescriptor.fileDescriptor

                                fin = FileInputStream(fd)
                                deployer!!.process(fin, asTarGz)

                                Toast.makeText(this, R.string.ok_data, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: IOException) {
                            Log.e(TAG, "provider data $uri", e)
                            Toast.makeText(this, R.string.fail_data, Toast.LENGTH_SHORT).show()
                        } finally {
                            if (fileDescriptor != null) {
                                try {
                                    fileDescriptor.close()
                                } catch (ignored: IOException) {
                                    //
                                }
                            }
                            if (fin != null) {
                                try {
                                    fin.close()
                                } catch (ignored: IOException) {
                                    //
                                }
                            }
                        }
                    }
                }
            }
            finish()
        }

        // button
        val closeButton = findViewById<Button>(R.id.button)
        closeButton.setOnClickListener {
            val requestFileIntent = Intent()
            requestFileIntent.setAction(Intent.ACTION_DEFAULT)
            requestFileIntent.setComponent(ComponentName(PROVIDER_PKG, PROVIDER_ACTIVITY))
            activityResultLauncher!!.launch(requestFileIntent)
        }
    }

    companion object {

        private const val TAG = "QueryProviderActivity"

        /**
         * Android provider package for wordnet data
         */
        private const val PROVIDER_PKG = "org.wordnet.provider"

        /**
         * Android provider name for wordnet data
         */
        private const val PROVIDER_NAME = "androidx.core.content.FileProvider"

        /**
         * Android provider activity for wordnet data
         */
        private const val PROVIDER_ACTIVITY = "org.wordnet.provider.FileProviderActivity"

        /**
         * Whether provider is available
         *
         * @param context context
         * @return true if provider is available
         */
        @JvmStatic
        fun isProviderAvailable(context: Context): Boolean {
            return getProvider(context) != null
        }

        /**
         * Provider info
         *
         * @param context context
         * @return provider info
         */
        private fun getProvider(context: Context): ProviderInfo? {
            try {
                val pm = context.packageManager
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) //
                    pm.getProviderInfo(ComponentName(PROVIDER_PKG, PROVIDER_NAME), PackageManager.ComponentInfoFlags.of(0)) else  //
                    pm.getProviderInfo(ComponentName(PROVIDER_PKG, PROVIDER_NAME), 0)
            } catch (ignored: PackageManager.NameNotFoundException) {
                //
            }
            return null
        }
    }
}
