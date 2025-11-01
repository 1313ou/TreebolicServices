/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.wordnet

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.bbou.donate.DonateActivity
import com.bbou.others.OthersActivity
import com.bbou.rate.AppRate.promptRate
import com.bbou.rate.AppRate.rate
import org.treebolic.AppCompatCommonActivity
import org.treebolic.AppCompatCommonPreferenceActivity
import org.treebolic.ParcelableModel
import org.treebolic.TreebolicIface
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.clients.iface.ITreebolicClient
import org.treebolic.services.IntentFactory.makeTreebolicIntentSkeleton
import org.treebolic.services.iface.ITreebolicService
import org.treebolic.wordnet.Permissions.check
import org.treebolic.wordnet.QueryProviderActivity.Companion.isProviderAvailable
import org.treebolic.wordnet.Settings.applicationSettings
import org.treebolic.wordnet.Settings.getStringPref
import org.treebolic.wordnet.Settings.putStringPref
import org.treebolic.wordnet.Settings.setDefaults
import org.treebolic.wordnet.service.client.TreebolicWordNetAIDLBoundClient
import org.treebolic.wordnet.service.client.TreebolicWordNetBoundClient
import org.treebolic.wordnet.service.client.TreebolicWordNetBroadcastClient
import org.treebolic.wordnet.service.client.TreebolicWordNetMessengerClient
import treebolic.model.Model
import androidx.core.content.edit

/**
 * Treebolic WordNet main activity. The activity obtains a model from data and requests Treebolic server to visualize it.
 *
 * @author Bernard Bou
 */
class MainActivity : AppCompatCommonActivity(), IConnectionListener, IModelListener {

    /**
     * Client
     */
    private var client: ITreebolicClient? = null

    /**
     * Data deployer
     */
    private var deployer: Deployer? = null

    /**
     * Search view on action bar
     */
    private var searchView: SearchView? = null

    /**
     * Data button
     */
    private lateinit var dataButton: MenuItem

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

        // activity result launcher
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val success = result.resultCode == RESULT_OK
            if (success) {
                // handle selection of target by other activity which returns selected target
                val returnIntent = result.data
                if (returnIntent != null) {
                    var downloadDataAvailable = returnIntent.getBooleanExtra(org.treebolic.download.DownloadActivity.RESULT_DOWNLOAD_DATA_AVAILABLE, false)
                    if (downloadDataAvailable) {
                        downloadDataAvailable = deployer!!.status()
                    }
                    dataButton.setIcon(if (downloadDataAvailable) R.drawable.ic_action_done else R.drawable.ic_action_error)
                }
            }
        }

        // set up the action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE
        }

        // deployer
        deployer = Deployer(filesDir)

        // saved instance
        if (savedInstanceState == null) {
            val fragment = PlaceholderFragment()
            supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
        }
    }

    override fun onResume() {
        val ok = deployer!!.status()
        dataButton.setIcon(if (ok) R.drawable.ic_action_done else R.drawable.ic_action_error)
        updateButton()

        stop()
        start()
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

        // search view
        val menuItem = menu.findItem(R.id.action_search)
        searchView = menuItem.actionView as SearchView
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView!!.clearFocus()
                searchView!!.setQuery("", false)
                return query(query)
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        // data status
        dataButton = menu.findItem(R.id.action_status_data)
        val ok = deployer!!.status()
        dataButton.setIcon(if (ok) R.drawable.ic_action_done else R.drawable.ic_action_error)
        dataButton.setOnMenuItemClickListener {
            val ok2 = deployer!!.status()
            Toast.makeText(this@MainActivity, if (ok2) R.string.ok_data else R.string.fail_data, Toast.LENGTH_SHORT).show()
            true
        }
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
            query("love")
            return true
        } else if (R.id.action_query_file_provider == id) {
            isProviderAvailable(this)
            startActivity(Intent(this, QueryProviderActivity::class.java))
            return true
        } else if (R.id.action_download == id) {
            requestDownload()
            return true
        } else if (R.id.action_cleanup == id) {
            deployer!!.cleanup()
            dataButton.setIcon(if (deployer!!.status()) R.drawable.ic_action_done else R.drawable.ic_action_error)
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
        } else if (R.id.action_app_settings == id) {
            applicationSettings(this, BuildConfig.APPLICATION_ID)
            return true
        } else if (R.id.action_settings_service == id) {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra(AppCompatCommonPreferenceActivity.INITIAL_ARG, SettingsActivity.ServicePreferenceFragment::class.java.name)
            startActivity(intent)
            return true
        } else if (R.id.action_settings == id) {
            startActivity(Intent(this, SettingsActivity::class.java))
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

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val providerAvailable = isProviderAvailable(this)
        menu.setGroupEnabled(R.id.provider_available, providerAvailable)
        val dataAvailable = deployer!!.status()
        menu.setGroupEnabled(R.id.data_available, dataAvailable)
        return true
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

            // flag as initialized
            sharedPref.edit(commit = true) { putBoolean(Settings.PREF_INITIALIZED, true) }

            // deploy
            // auto from provider
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
            ITreebolicService.TYPE_BROADCAST -> client = TreebolicWordNetBroadcastClient(this, this, this)
            ITreebolicService.TYPE_AIDL_BOUND -> client = TreebolicWordNetAIDLBoundClient(this, this, this)
            ITreebolicService.TYPE_BOUND -> client = TreebolicWordNetBoundClient(this, this, this)
            ITreebolicService.TYPE_MESSENGER -> client = TreebolicWordNetMessengerClient(this, this, this)
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
            if (query.startsWith("wordnet:")) {
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
        var query = if (searchView!!.query == null) null else searchView!!.query.toString()
        if (query.isNullOrEmpty()) {
            query = getStringPref(this, TreebolicIface.PREF_SOURCE)
        }

        // query
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
     * @param query     query
     * @param base      doc base
     * @param imageBase image base
     * @param settings  settings
     * @return true if query was made
     */
    private fun query(query: String?, base: String?, imageBase: String?, settings: String?): Boolean {
        if (query.isNullOrEmpty()) {
            Toast.makeText(this@MainActivity, R.string.fail_nullquery, Toast.LENGTH_SHORT).show()
            return false
        } else if (this@MainActivity.client == null) {
            Toast.makeText(this@MainActivity, R.string.fail_nullclient, Toast.LENGTH_SHORT).show()
            return false
        }
        val forward = if (FORWARD) makeTreebolicIntentSkeleton(Intent(this, MainActivity::class.java), base, imageBase, settings) else null
        client!!.requestModel(query, base, imageBase, settings, forward)
        return true
    }

    /**
     * Request source
     */
    private fun requestSource() {
        val alert = AlertDialog.Builder(this)

        alert.setTitle(R.string.title_choose)
        alert.setMessage(R.string.title_choose_source)
        val input = EditText(this)
        input.maxLines = 1
        input.inputType = InputType.TYPE_CLASS_TEXT
        alert.setView(input)

        alert.setPositiveButton(R.string.action_ok) { _: DialogInterface?, _: Int ->
            val value = input.text.toString()
            putStringPref(this@MainActivity, TreebolicIface.PREF_SOURCE, value)
            updateButton()
        }

        alert.setNegativeButton(R.string.action_cancel) { _: DialogInterface?, _: Int -> }

        val dialog = alert.create()
        input.setOnEditorActionListener { _: TextView?, _: Int, event: KeyEvent ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
                return@setOnEditorActionListener true
            }
            false
        }
        dialog.show()
    }

    // M O D E L   L I S T E N E R

    override fun onModel(model: Model?, modelUrlScheme: String?) {
        if (model != null) {
            val intent = makeTreebolicIntent(this, model, null, null)

            Log.d(TAG, "Starting Treebolic")
            startActivity(intent)
        }
    }

    // D O W N L O A D

    private fun requestDownload() {
        activityResultLauncher!!.launch(Intent(this, DownloadActivity::class.java))
    }

    // H E L P E R

    private fun updateButton() {
        val button = findViewById<ImageButton>(R.id.queryButton)
        button.setOnClickListener { view: View? -> onClick(view) }
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
        return !source.isNullOrEmpty()
    }

    // C L I C K

    /**
     * Click listener
     *
     * @param view view
     */
    private fun onClick(@Suppress("unused") view: View?) {
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

        private const val TAG = "WnSMainA"

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
            intent.component = ComponentName(TreebolicIface.PKG_TREEBOLIC, TreebolicIface.ACTIVITY_MODEL)
            if (ParcelableModel.SERIALIZE) {
                intent.putExtra(TreebolicIface.ARG_SERIALIZED, true)
                intent.putExtra(TreebolicIface.ARG_MODEL, model)
            } else {
                intent.putExtra(TreebolicIface.ARG_SERIALIZED, false)
                intent.putExtra(TreebolicIface.ARG_MODEL, ParcelableModel(model))
            }
            intent.putExtra(TreebolicIface.ARG_BASE, base)
            intent.putExtra(TreebolicIface.ARG_IMAGEBASE, imageBase)
            intent.putExtra(TreebolicIface.ARG_PARENTACTIVITY, parentIntent)
            intent.putExtra(TreebolicIface.ARG_URLSCHEME, "wordnet:")

            return intent
        }
    }
}
