/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.owl

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.bbou.donate.DonateActivity
import com.bbou.others.OthersActivity
import com.bbou.rate.AppRate.promptRate
import com.bbou.rate.AppRate.rate
import org.treebolic.AppCompatCommonActivity
import org.treebolic.AppCompatCommonPreferenceActivity
import org.treebolic.Models.set
import org.treebolic.ParcelableModel
import org.treebolic.TreebolicIface
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.clients.iface.ITreebolicClient
import org.treebolic.filechooser.EntryChooser.Companion.choose
import org.treebolic.filechooser.FileChooserActivity
import org.treebolic.owl.Permissions.check
import org.treebolic.owl.Settings.applicationSettings
import org.treebolic.owl.Settings.getStringPref
import org.treebolic.owl.Settings.save
import org.treebolic.owl.Settings.setDefaults
import org.treebolic.owl.service.client.TreebolicOwlAIDLBoundClient
import org.treebolic.owl.service.client.TreebolicOwlBoundClient
import org.treebolic.owl.service.client.TreebolicOwlBroadcastClient
import org.treebolic.owl.service.client.TreebolicOwlMessengerClient
import org.treebolic.services.IntentFactory.makeTreebolicIntentSkeleton
import org.treebolic.services.iface.ITreebolicService
import org.treebolic.storage.Deployer.copyAssetFile
import org.treebolic.storage.Deployer.expandZipAssetFile
import org.treebolic.storage.Storage.getTreebolicStorage
import treebolic.model.Model
import java.io.File
import java.io.IOException

/**
 * Treebolic Owl main activity. The activity obtains a model from data and requests Treebolic server to visualize it.
 *
 * @author Bernard Bou
 */
class MainActivity : AppCompatCommonActivity(), IConnectionListener, IModelListener {

    /**
     * Client
     */
    private var client: ITreebolicClient? = null

    /**
     * Activity result launcher
     */
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    // L I F E C Y C L E

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // rate
        promptRate(this)

        // init
        initialize()

        // view
        setContentView(R.layout.activity_main)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE
        }

        // activity result launcher
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val success = result.resultCode == RESULT_OK
            if (success) {
                val returnIntent = result.data
                if (returnIntent != null) {
                    val fileUri = returnIntent.data
                    if (fileUri != null) {
                        Toast.makeText(this, fileUri.toString(), Toast.LENGTH_SHORT).show()
                        val path = fileUri.path
                        if (path != null) {
                            val file = File(path)
                            val parent = file.parent
                            if (parent != null) {
                                val parentFile = File(parent)
                                val parentUri = Uri.fromFile(parentFile)
                                val query = file.name
                                var base = parentUri.toString()
                                if (!base.endsWith("/")) {
                                    base += '/'
                                }
                                save(this, query, base)
                            }
                        }
                    }
                }
                updateButton()
            }
        }

        // fragment
        if (savedInstanceState == null) {
            val fragment = PlaceholderFragment()
            supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
        }
    }

    override fun onResume() {
        stop()
        start()
        updateButton()
        super.onResume()
    }

    override fun onPause() {
        stop()
        super.onPause()
    }

    // M E N U

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (R.id.action_query == id) {
            query()
            return true
        } else if (R.id.action_source == id) {
            requestSource()
            return true
        } else if (R.id.action_demo == id) {
            val archiveFileUri = copyAssetFile(this, Settings.DEMOZIP)
            if (archiveFileUri != null) {
                queryBundle(archiveFileUri)
            }
            return true
        } else if (R.id.action_others == id) {
            startActivity(Intent(this, OthersActivity::class.java))
            return true
        } else if (R.id.action_donate == id) {
            startActivity(Intent(this, DonateActivity::class.java))
            return true
        } else if (R.id.action_rate == id) {
            rate(this)
            return true
        } else if (R.id.action_download == id) {
            startActivity(Intent(this, DownloadActivity::class.java))
            return true
        } else if (R.id.action_app_settings == id) {
            applicationSettings(this, BuildConfig.APPLICATION_ID)
            return true
        } else if (R.id.action_settings == id) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        } else if (R.id.action_settings_service == id) {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra(AppCompatCommonPreferenceActivity.INITIAL_ARG, SettingsActivity.ServicePreferenceFragment::class.java.name)
            startActivity(intent)
            return true
        } else if (R.id.action_finish == id) {
            finish()
            return true
        } else if (R.id.action_kill == id) {
            Process.killProcess(Process.myPid())
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    // P R E F E R E N C E S   A N D   D A T A

    /**
     * Initialize
     */
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    private fun initialize() {
        // permissions
        check(this)

        // test if initialized
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val initialized = sharedPref.getBoolean(Settings.PREF_INITIALIZED, false)
        if (!initialized) {
            // default settings
            setDefaults(this)

            // deploy
            val dir = getTreebolicStorage(this)
            if (dir.isDirectory) {
                val dirContent = dir.list()
                if (dirContent == null || dirContent.isEmpty()) {
                    // deploy
                    expandZipAssetFile(this, "owl.zip")
                }
            }

            // flag as initialized
            sharedPref.edit().putBoolean(Settings.PREF_INITIALIZED, true).commit()
        }
    }

    // C L I E N T   O P E R A T I O N

    /**
     * Start client
     */
    private fun start() {
        // client
        var serviceType = getStringPref(this, Settings.PREF_SERVICE)
        if (serviceType == null) {
            serviceType = ITreebolicService.TYPE_BROADCAST
        }
        when (serviceType) {
            ITreebolicService.TYPE_BROADCAST -> client = TreebolicOwlBroadcastClient(this, this, this)
            ITreebolicService.TYPE_AIDL_BOUND -> client = TreebolicOwlAIDLBoundClient(this, this, this)
            ITreebolicService.TYPE_BOUND -> client = TreebolicOwlBoundClient(this, this, this)
            ITreebolicService.TYPE_MESSENGER -> client = TreebolicOwlMessengerClient(this, this, this)
        }
        // connect
        client!!.connect()
    }

    /**
     * Stop client
     */
    private fun stop() {
        if (client != null) {
            client!!.disconnect()
            client = null
        }
    }

    // C O N N E C T I O N   L I S T E N E R

    override fun onConnected(success: Boolean) {
        // url hook
        var query = intent.getStringExtra(TreebolicIface.ARG_SOURCE)
        if (query != null) {
            if (query.startsWith("owl:")) {
                query = query.substring(8)
                query(query)
            }
        }
    }

    // Q U E R Y

    /**
     * Query request
     */
    private fun query() {
        // get query
        val query = getStringPref(this, TreebolicIface.PREF_SOURCE)
        query(query)
    }

    /**
     * Query request
     *
     * @param source source
     */
    private fun query(source: String?): Boolean {
        return query(source, getStringPref(this, TreebolicIface.PREF_BASE), getStringPref(this, TreebolicIface.PREF_IMAGEBASE), getStringPref(this, TreebolicIface.PREF_SETTINGS))
    }

    /**
     * Query request
     *
     * @param source    source
     * @param base      doc base
     * @param imageBase image base
     * @param settings  settings
     * @return true if query was made
     */
    private fun query(source: String?, base: String?, imageBase: String?, settings: String?): Boolean {
        if (source.isNullOrEmpty()) {
            Toast.makeText(this@MainActivity, R.string.fail_nullquery, Toast.LENGTH_SHORT).show()
            return false
        } else if (this@MainActivity.client == null) {
            Toast.makeText(this@MainActivity, R.string.fail_nullclient, Toast.LENGTH_SHORT).show()
            return false
        }
        val forward = if (FORWARD) makeTreebolicIntentSkeleton(Intent(this, MainActivity::class.java), base, imageBase, settings) else null
        client!!.requestModel(source, base, imageBase, settings, forward)
        return true
    }

    /**
     * Query request from zipped bundle file
     *
     * @param archiveUri archive uri
     */
    private fun queryBundle(archiveUri: Uri) {
        try {
            val path = archiveUri.path
            if (path != null) {
                // choose bundle entry
                choose(this, File(archiveUri.path!!)) { zipEntry: String? ->
                    val base = "jar:$archiveUri!/"
                    query(zipEntry, base, getStringPref(this@MainActivity, TreebolicIface.PREF_IMAGEBASE), getStringPref(this@MainActivity, TreebolicIface.PREF_SETTINGS))
                }
            }
        } catch (e: IOException) {
            Log.d(TAG, "Failed to start treebolic from bundle uri $archiveUri", e)
        }
    }

    // M O D E L   L I S T E N E R

    override fun onModel(model: Model?, modelUrlScheme: String?) {
        if (model != null) {
            val intent = makeTreebolicIntent(this, model, null, null)

            Log.d(TAG, "Starting Treebolic")
            startActivity(intent)
        }
    }

    // R E Q U E S T (choose source)

    /**
     * Request Owl source
     */
    private fun requestSource() {
        val intent = Intent(this, FileChooserActivity::class.java)
        intent.setType("application/rdf+xml")
        intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_INITIAL_DIR, getStringPref(this, TreebolicIface.PREF_BASE))
        intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_EXTENSION_FILTER, arrayOf("owl", "rdf"))
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        activityResultLauncher!!.launch(intent)
    }

    // H E L P E R

    private fun updateButton() {
        val button = findViewById<ImageButton>(R.id.queryButton)
        button.setOnClickListener { view: View? -> onClick() }
        val sourceText = findViewById<TextView>(R.id.querySource)
        val source = getStringPref(this, TreebolicIface.PREF_SOURCE)
        val qualifies = sourceQualifies(source)
        button.visibility = if (qualifies) View.VISIBLE else View.INVISIBLE
        sourceText.visibility = if (qualifies) View.VISIBLE else View.INVISIBLE
        if (qualifies) {
            sourceText.text = source
        }
    }

    /**
     * Whether source qualifies
     *
     * @return true if source qualifies
     */
    private fun sourceQualifies(source: String?): Boolean {
        val base = getStringPref(this, TreebolicIface.PREF_BASE)
        if (!source.isNullOrEmpty()) {
            val baseUri = Uri.parse(base)
            val path = baseUri.path
            if (path != null) {
                val baseFile = if (base == null) null else File(path)
                val file = File(baseFile, source)
                Log.d(TAG, "file=$file")
                return file.exists()
            }
        }
        return false
    }

    // C L I C K

    /**
     * Click listener
     */
    private fun onClick() {
        query()
    }

    // F R A G M E N T

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_main, container, false)
        }
    }

    companion object {

        private const val TAG = "ServiceOwlA"

        /**
         * Whether to forward model directly to activity
         */
        private const val FORWARD = true

        /**
         * Make Treebolic intent
         *
         * @param context   content
         * @param model     model
         * @param base      base
         * @param imageBase image base
         * @return intent
         */
        fun makeTreebolicIntent(context: Context, model: Model?, base: String?, imageBase: String?): Intent {
            // parent activity to return to
            val parentIntent = Intent()
            parentIntent.setClass(context, MainActivity::class.java)

            // intent
            val intent = Intent()
            intent.setComponent(ComponentName(TreebolicIface.PKG_TREEBOLIC, TreebolicIface.ACTIVITY_MODEL))

            // model passing
            if (TreebolicIface.USE_MODEL_REFERENCES) {
                set(model!!, intent)
            } else {
                if (ParcelableModel.SERIALIZE) {
                    intent.putExtra(TreebolicIface.ARG_SERIALIZED, true)
                    intent.putExtra(TreebolicIface.ARG_MODEL, model)
                } else {
                    intent.putExtra(TreebolicIface.ARG_SERIALIZED, false)
                    intent.putExtra(TreebolicIface.ARG_MODEL, ParcelableModel(model))
                }
            }

            // other parameters passing
            intent.putExtra(TreebolicIface.ARG_BASE, base)
            intent.putExtra(TreebolicIface.ARG_IMAGEBASE, imageBase)

            // parent passing
            intent.putExtra(TreebolicIface.ARG_PARENTACTIVITY, parentIntent)

            return intent
        }
    }
}
