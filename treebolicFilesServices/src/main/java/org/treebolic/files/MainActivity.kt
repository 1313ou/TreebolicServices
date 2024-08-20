/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.files

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.multidex.BuildConfig
import androidx.preference.PreferenceManager
import com.bbou.donate.DonateActivity
import com.bbou.others.OthersActivity
import com.bbou.rate.AppRate.invoke
import com.bbou.rate.AppRate.rate
import org.treebolic.AppCompatCommonActivity
import org.treebolic.AppCompatCommonPreferenceActivity
import org.treebolic.Models.set
import org.treebolic.ParcelableModel
import org.treebolic.TreebolicIface
import org.treebolic.clients.iface.IConnectionListener
import org.treebolic.clients.iface.IModelListener
import org.treebolic.clients.iface.ITreebolicClient
import org.treebolic.filechooser.FileChooserActivity
import org.treebolic.files.service.client.TreebolicFilesAIDLBoundClient
import org.treebolic.files.service.client.TreebolicFilesBoundClient
import org.treebolic.files.service.client.TreebolicFilesBroadcastClient
import org.treebolic.files.service.client.TreebolicFilesMessengerClient
import org.treebolic.services.IntentFactory.makeTreebolicIntentSkeleton
import org.treebolic.services.iface.ITreebolicService
import treebolic.model.Model
import java.io.File

/**
 * Treebolic Files main activity. The activity obtains a model from source and requests Treebolic server to visualize it.
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
        invoke(this)

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
        this.activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val success = result.resultCode == RESULT_OK
            if (success) {
                // handle selection of target by other activity which returns selected target
                val returnIntent = result.data
                if (returnIntent != null) {
                    val fileUri = returnIntent.data
                    if (fileUri != null) {
                        Toast.makeText(this, fileUri.toString(), Toast.LENGTH_SHORT).show()
                        Settings.putStringPref(this, TreebolicIface.PREF_SOURCE, fileUri.path)

                        updateButton()
                    }
                }
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
        if (R.id.action_places == id) {
            chooseAndSave()
            return true
        } else if (R.id.action_run == id) {
            query()
            return true
        } else if (R.id.action_source == id) {
            requestSource()
            return true
        } else if (R.id.action_demo == id) {
            chooseAndTryStartTreebolic()
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
            Settings.applicationSettings(this, BuildConfig.APPLICATION_ID)
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
        Permissions.check(this)

        // test if initialized
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val initialized = sharedPref.getBoolean(Settings.PREF_INITIALIZED, false)
        if (!initialized) {
            // default settings
            Settings.setDefaults(this)

            // flag as initialized
            sharedPref.edit().putBoolean(Settings.PREF_INITIALIZED, true).commit()
        }
    }

    // R E Q U E S T (choose source)

    /**
     * Request directory source
     */
    private fun requestSource() {
        val intent = Intent(this, FileChooserActivity::class.java)
        intent.setType("inode/directory")
        intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_INITIAL_DIR, StorageExplorer.discoverExternalStorage(this))
        intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_CHOOSE_DIR, true)
        intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_EXTENSION_FILTER, arrayOf<String>())
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        activityResultLauncher!!.launch(intent)
    }

    internal abstract class Runnable1 {

        abstract fun run(arg: String)
    }

    /**
     * Choose dir and scan
     */
    private fun chooseAndTryStartTreebolic() {
        choosePlace(object : Runnable1(
        ) {
            override fun run(arg: String) {
                query(arg + File.separatorChar)
            }
        })
    }

    /**
     * Choose dir and save
     */
    private fun chooseAndSave() {
        choosePlace(object : Runnable1(
        ) {
            override fun run(arg: String) {
                Settings.putStringPref(this@MainActivity, TreebolicIface.PREF_SOURCE, arg)
                updateButton()
            }
        })
    }

    /**
     * Choose dir to scan
     *
     * @param runnable1 what to do
     */
    private fun choosePlace(runnable1: Runnable1) {
        val result = StorageExplorer.getDirectoriesTypesValues(this)
        val types = result.first
        val values = result.second

        val alert = AlertDialog.Builder(this)
        alert.setTitle(R.string.title_choose)
        alert.setMessage(R.string.title_choose_directory)

        val input = RadioGroup(this)
        var i = 0
        while (i < types.size && i < values.size) {
            val type = types[i]
            val value = values[i]
            val dir = File(value.toString())
            if (dir.exists()) {
                val radioButton = RadioButton(this)
                val path = dir.absolutePath
                val str = "$path [$type]"
                radioButton.text = str
                radioButton.tag = path
                input.addView(radioButton)
            }
            i++
        }
        alert.setView(input)
        alert.setPositiveButton(R.string.action_ok) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            val childCount = input.childCount
            for (j in 0 until childCount) {
                val radioButton = input.getChildAt(j) as RadioButton
                if (radioButton.id == input.checkedRadioButtonId) {
                    val sourceFile = radioButton.tag.toString()
                    val sourceDir = File(sourceFile)
                    if (sourceDir.exists() && sourceDir.isDirectory) {
                        runnable1.run(sourceFile + File.separatorChar)
                    } else {
                        val alert2 = AlertDialog.Builder(this@MainActivity)
                        alert2.setTitle(sourceFile) //
                            .setMessage(getString(R.string.status_fail)) //
                            .show()
                    }
                }
            }
        }
        alert.setNegativeButton(R.string.action_cancel) { _: DialogInterface?, _: Int -> }
        alert.show()
    }

    // C L I E N T   O P E R A T I O N

    /**
     * Start client
     */
    private fun start() {
        // client
        var serviceType = Settings.getStringPref(this, Settings.PREF_SERVICE)
        if (serviceType == null) {
            serviceType = ITreebolicService.TYPE_BROADCAST
        }
        when (serviceType) {
            ITreebolicService.TYPE_BROADCAST -> this.client = TreebolicFilesBroadcastClient(this, this, this)
            ITreebolicService.TYPE_AIDL_BOUND -> this.client = TreebolicFilesAIDLBoundClient(this, this, this)
            ITreebolicService.TYPE_BOUND -> this.client = TreebolicFilesBoundClient(this, this, this)
            ITreebolicService.TYPE_MESSENGER -> this.client = TreebolicFilesMessengerClient(this, this, this)
        }
        // connect
        if (this.client == null) {
            return
        }
        client!!.connect()
    }

    /**
     * Stop client
     */
    private fun stop() {
        if (this.client != null) {
            client!!.disconnect()
            this.client = null
        }
    }

    // C O N N E C T I O N   L I S T E N E R

    override fun onConnected(success: Boolean) {
        // url hook
        var query = intent.getStringExtra(TreebolicIface.ARG_SOURCE)
        if (query != null) {
            if (query.startsWith("directory:")) {
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
        val query = Settings.getStringPref(this, TreebolicIface.PREF_SOURCE)
        query(query)
    }

    /**
     * Query request
     *
     * @param source source
     */
    private fun query(source: String?): Boolean {
        if (source.isNullOrEmpty()) {
            Toast.makeText(this@MainActivity, R.string.fail_nullquery, Toast.LENGTH_SHORT).show()
            return false
        } else if (this@MainActivity.client == null) {
            Toast.makeText(this@MainActivity, R.string.fail_nullclient, Toast.LENGTH_SHORT).show()
            return false
        }
        val forward = if (FORWARD) makeTreebolicIntentSkeleton(Intent(this, MainActivity::class.java), null, null, null) else null
        client!!.requestModel(source, null, null, null, forward)
        return true
    }

    // M O D E L   L I S T E N E R

    override fun onModel(model: Model?, urlScheme: String?) {
        if (model != null) {
            val intent = makeTreebolicIntent(this, model)

            Log.d(TAG, "Starting treebolic")
            this.startActivity(intent)
        }
    }

    // H E L P E R

    private fun updateButton() {
        val button = findViewById<ImageButton>(R.id.queryButton)
        button.setOnClickListener { view: View? -> this.onClick(view) }
        val sourceText = findViewById<TextView>(R.id.querySource)
        val source = Settings.getStringPref(this, TreebolicIface.PREF_SOURCE)
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
        if (!source.isNullOrEmpty()) {
            val file = File(source)
            Log.d(TAG, "file=$file")
            return file.exists() && file.isDirectory
        }
        return false
    }

    // C L I C K

    /**
     * Click listener
     *
     * @param view view
     */
    private fun onClick(view: View?) {
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

        private const val TAG = "ServiceFilesA"

        /**
         * Whether to forward model directly to activity
         */
        private const val FORWARD = true

        /**
         * Make Treebolic intent
         *
         * @param context content
         * @param model   model
         * @return intent
         */
        fun makeTreebolicIntent(context: Context, model: Model?): Intent {
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
            intent.putExtra(TreebolicIface.ARG_BASE, null as String?)
            intent.putExtra(TreebolicIface.ARG_IMAGEBASE, null as String?)

            // parent passing
            intent.putExtra(TreebolicIface.ARG_PARENTACTIVITY, parentIntent)

            return intent
        }
    }
}
