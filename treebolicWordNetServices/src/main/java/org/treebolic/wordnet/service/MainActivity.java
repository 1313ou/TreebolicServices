package org.treebolic.wordnet.service;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.treebolic.ParcelableModel;
import org.treebolic.TreebolicIface;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.clients.iface.ITreebolicClient;
import org.treebolic.services.IntentFactory;
import org.treebolic.wordnet.service.client.TreebolicWordNetAIDLBoundClient;
import org.treebolic.wordnet.service.client.TreebolicWordNetBoundClient;
import org.treebolic.wordnet.service.client.TreebolicWordNetIntentClient;
import org.treebolic.wordnet.service.client.TreebolicWordNetMessengerClient;

import treebolic.model.Model;

/**
 * Treebolic WordNet main activity. The activity obtains a model from data and requests Treebolic server to visualize it.
 *
 * @author Bernard Bou
 */
public class MainActivity extends AppCompatActivity implements IConnectionListener, IModelListener
{
	/**
	 * Log tag
	 */
	static private final String TAG = "TreebolicWordNetA";

	/**
	 * Whether to forward model directly to activity
	 */
	static private final boolean FORWARD = true;

	/**
	 * Client
	 */
	private ITreebolicClient client;

	/**
	 * Request data code
	 */
	static private final int REQUEST_DATA_FROM_DOWNLOADER = 0x7777;

	/**
	 * Data deployer
	 */
	private Deployer deployer;

	/**
	 * Search view on action bar
	 */
	private SearchView searchView;

	/**
	 * Data button
	 */
	private MenuItem dataButton;

	// L I F E C Y C L E

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// view
		setContentView(R.layout.activity_main);

		// toolbar
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE);
		}

		// init
		initialize();

		// deployer
		this.deployer = new Deployer(getFilesDir());

		// saved instance
		if (savedInstanceState == null)
		{
			PlaceholderFragment fragment = new PlaceholderFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
		}
	}

	@Override
	protected void onResume()
	{
		final boolean ok = MainActivity.this.deployer.status();
		if (this.dataButton != null)
		{
			this.dataButton.setIcon(ok ? R.drawable.ic_action_done : R.drawable.ic_action_error);
		}

		stop();
		start();
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		stop();
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		// inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// search view
		final MenuItem menuItem = menu.findItem(R.id.action_search);
		this.searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
		this.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public boolean onQueryTextSubmit(final String query)
			{
				MainActivity.this.searchView.clearFocus();
				MainActivity.this.searchView.setQuery("", false);
				return query(query);
			}

			@Override
			public boolean onQueryTextChange(final String newText)
			{
				return false;
			}
		});

		// data status
		this.dataButton = menu.findItem(R.id.action_status_data);
		final boolean ok = MainActivity.this.deployer.status();
		this.dataButton.setIcon(ok ? R.drawable.ic_action_done : R.drawable.ic_action_error);
		this.dataButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public boolean onMenuItemClick(final MenuItem item)
			{
				final boolean ok2 = MainActivity.this.deployer.status();
				Toast.makeText(MainActivity.this, ok2 ? R.string.ok_data : R.string.fail_data, Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		final int id = item.getItemId();
		switch (id)
		{
		case R.id.action_query:
			query();
			return true;

		case R.id.action_query_file_provider:
			QueryProviderActivity.isProviderAvailable(this);
			startActivity(new Intent(this, QueryProviderActivity.class));
			return true;

		case R.id.action_download:
			startActivityForResult(new Intent(this, DownloadActivity.class), MainActivity.REQUEST_DATA_FROM_DOWNLOADER);
			return true;

		case R.id.action_cleanup:
			this.deployer.cleanup();
			this.dataButton.setIcon(MainActivity.this.deployer.status() ? R.drawable.ic_action_done : R.drawable.ic_action_error);
			return true;

		case R.id.action_demo:
			query("love");
			return true;

		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;

		case R.id.action_app_settings:
			Settings.applicationSettings(this, "org.treebolic.wordnet.service");
			return true;

		case R.id.action_finish:
			finish();
			return true;

		case R.id.action_kill:
			Process.killProcess(Process.myPid());
			return true;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu)
	{
		final boolean providerAvailable = QueryProviderActivity.isProviderAvailable(this);
		menu.setGroupEnabled(R.id.provider_available, providerAvailable);
		final boolean dataAvailable = MainActivity.this.deployer.status();
		menu.setGroupEnabled(R.id.data_available, dataAvailable);
		return true;
	}

	/**
	 * Initialize
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	private void initialize()
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		// test if initialized
		final boolean result = sharedPref.getBoolean(Settings.PREF_INITIALIZED, false);
		if (result)
			return;

		// default settings
		Settings.setDefaults(this);

		// flag as initialized
		sharedPref.edit().putBoolean(Settings.PREF_INITIALIZED, true).commit();
	}

	// R E Q U E S T D A T A R E S U L T

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent returnIntent)
	{
		// handle selection of target by other activity which returns selected target
		if (resultCode == AppCompatActivity.RESULT_OK)
		{
			switch (requestCode)
			{
			case REQUEST_DATA_FROM_DOWNLOADER:
				boolean downloadDataAvailable = returnIntent.getBooleanExtra(org.treebolic.download.DownloadActivity.RESULT_DOWNLOAD_DATA_AVAILABLE, false);
				if (downloadDataAvailable)
				{
					downloadDataAvailable = this.deployer.status();
				}
				this.dataButton.setIcon(downloadDataAvailable ? R.drawable.ic_action_done : R.drawable.ic_action_error);
				break;
			default:
				break;
			}
			super.onActivityResult(requestCode, resultCode, returnIntent);
		}
	}

	// C L I E N T O P E R A T I O N

	/**
	 * Start client
	 */
	protected void start()
	{
		// client
		final String serviceType = Settings.getStringPref(this, Settings.PREF_SERVICE);
		if ("IntentService".equals(serviceType))
		{
			this.client = new TreebolicWordNetIntentClient(this, this, this);
		}
		else if ("Messenger".equals(serviceType))
		{
			this.client = new TreebolicWordNetMessengerClient(this, this, this);
		}
		else if ("AIDLBound".equals(serviceType))
		{
			this.client = new TreebolicWordNetAIDLBoundClient(this, this, this);
		}
		else if ("Bound".equals(serviceType))
		{
			this.client = new TreebolicWordNetBoundClient(this, this, this);
		}

		// connect
		this.client.connect();
	}

	/**
	 * Stop client
	 */
	protected void stop()
	{
		if (this.client != null)
		{
			this.client.disconnect();
			this.client = null;
		}
	}

	/**
	 * Query request
	 */
	private void query()
	{
		// get query
		String query = this.searchView.getQuery() == null ? null : this.searchView.getQuery().toString();
		if (query == null || query.isEmpty())
		{
			query = Settings.getStringPref(this, TreebolicIface.PREF_SOURCE);
		}

		// query
		query(query);
	}

	/**
	 * Query request
	 *
	 * @param source
	 *            source
	 */
	private boolean query(final String source)
	{
		return query(source, Settings.getStringPref(this, TreebolicIface.PREF_BASE), Settings.getStringPref(this, TreebolicIface.PREF_IMAGEBASE),
				Settings.getStringPref(this, TreebolicIface.PREF_SETTINGS));
	}

	/**
	 * Query request
	 *
	 * @param query
	 *            query
	 * @param base
	 *            doc base
	 * @param imageBase
	 *            image base
	 * @param settings
	 *            settings
	 * @return true if query was made
	 */
	protected boolean query(final String query, final String base, final String imageBase, final String settings)
	{
		if (query == null || query.isEmpty())
		{
			Toast.makeText(MainActivity.this, R.string.fail_nullquery, Toast.LENGTH_SHORT).show();
			return false;
		}
		else if (MainActivity.this.client == null)
		{
			Toast.makeText(MainActivity.this, R.string.fail_nullclient, Toast.LENGTH_SHORT).show();
			return false;
		}
		@SuppressWarnings("ConstantConditions") final Intent forward = MainActivity.FORWARD ? IntentFactory.makeTreebolicIntentSkeleton(new Intent(this,
				org.treebolic.wordnet.service.MainActivity.class), base, imageBase, settings) : null;
		MainActivity.this.client.requestModel(query, base, imageBase, settings, forward);
		return true;
	}

	// M O D E L C O N S U M E R

	@Override
	public void onConnected(final boolean flag)
	{
		// url hook
		String query = getIntent().getStringExtra(TreebolicIface.ARG_SOURCE);
		if (query != null)
		{
			if (query.startsWith("wordnet:"))
			{
				query = query.substring(8);
				query(query);
			}
		}
	}

	// M O D E L L I S T E N E R

	@Override
	public void onModel(final Model model, final String urlScheme0)
	{
		if (model != null)
		{
			final Intent intent = MainActivity.makeTreebolicIntent(this, model, null, null);

			Log.d(MainActivity.TAG, "Starting Treebolic");
			this.startActivity(intent);
		}
	}

	/**
	 * Make Treebolic intent
	 *
	 * @param context
	 *            content
	 * @param model
	 *            model
	 * @param base
	 *            base
	 * @param imageBase
	 *            image base
	 * @return intent
	 */
	static public Intent makeTreebolicIntent(final Context context, final Model model, final String base, final String imageBase)
	{
		// parent activity to return to
		final Intent parentIntent = new Intent();
		parentIntent.setClass(context, org.treebolic.wordnet.service.MainActivity.class);

		// intent
		final Intent intent = new Intent();
		intent.setComponent(new ComponentName(TreebolicIface.PKG_TREEBOLIC, TreebolicIface.ACTIVITY_MODEL));
		if (ParcelableModel.SERIALIZE)
		{
			intent.putExtra(TreebolicIface.ARG_SERIALIZED, true);
			intent.putExtra(TreebolicIface.ARG_MODEL, model);
		}
		else
		{
			intent.putExtra(TreebolicIface.ARG_SERIALIZED, false);
			intent.putExtra(TreebolicIface.ARG_MODEL, new ParcelableModel(model));
		}
		intent.putExtra(TreebolicIface.ARG_BASE, base);
		intent.putExtra(TreebolicIface.ARG_IMAGEBASE, imageBase);
		intent.putExtra(TreebolicIface.ARG_PARENTACTIVITY, parentIntent);
		intent.putExtra(TreebolicIface.ARG_URLSCHEME, "wordnet:");

		return intent;
	}

	// F R A G M E N T

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment
	{
		/**
		 * Constructor
		 */
		public PlaceholderFragment()
		{
			//
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.fragment_main, container, false);
		}
	}
}
