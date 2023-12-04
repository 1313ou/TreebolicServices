/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.wordnet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bbou.donate.DonateActivity;
import com.bbou.others.OthersActivity;
import com.bbou.rate.AppRate;

import org.treebolic.AppCompatCommonActivity;
import org.treebolic.ParcelableModel;
import org.treebolic.TreebolicIface;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.clients.iface.ITreebolicClient;
import org.treebolic.services.IntentFactory;
import org.treebolic.wordnet.service.client.TreebolicWordNetAIDLBoundClient;
import org.treebolic.wordnet.service.client.TreebolicWordNetBoundClient;
import org.treebolic.wordnet.service.client.TreebolicWordNetBroadcastClient;
import org.treebolic.wordnet.service.client.TreebolicWordNetMessengerClient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import treebolic.model.Model;

/**
 * Treebolic WordNet main activity. The activity obtains a model from data and requests Treebolic server to visualize it.
 *
 * @author Bernard Bou
 */
public class MainActivity extends AppCompatCommonActivity implements IConnectionListener, IModelListener
{
	/**
	 * Log tag
	 */
	static private final String TAG = "WnSMainA";

	/**
	 * Whether to forward model directly to activity
	 */
	static private final boolean FORWARD = true;

	/**
	 * Client
	 */
	@Nullable
	private ITreebolicClient client;

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

	/**
	 * Activity result launcher
	 */
	protected ActivityResultLauncher<Intent> activityResultLauncher;

	// L I F E C Y C L E

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// rate
		AppRate.invoke(this);

		// view
		setContentView(R.layout.activity_main);

		// toolbar
		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// activity result launcher
		this.activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			boolean success = result.getResultCode() == Activity.RESULT_OK;
			if (success)
			{
				// handle selection of target by other activity which returns selected target
				Intent returnIntent = result.getData();
				if (returnIntent != null)
				{
					boolean downloadDataAvailable = returnIntent.getBooleanExtra(org.treebolic.download.DownloadActivity.RESULT_DOWNLOAD_DATA_AVAILABLE, false);
					if (downloadDataAvailable)
					{
						downloadDataAvailable = this.deployer.status();
					}
					this.dataButton.setIcon(downloadDataAvailable ? R.drawable.ic_action_done : R.drawable.ic_action_error);
				}
			}
		});

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
		updateButton();

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

	// M E N U

	@Override
	public boolean onCreateOptionsMenu(@NonNull final Menu menu)
	{
		// inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// search view
		final MenuItem menuItem = menu.findItem(R.id.action_search);
		this.searchView = (SearchView) menuItem.getActionView();
		assert this.searchView != null;
		this.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(@NonNull final String query)
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
		this.dataButton.setOnMenuItemClickListener(item -> {
			final boolean ok2 = MainActivity.this.deployer.status();
			Toast.makeText(MainActivity.this, ok2 ? R.string.ok_data : R.string.fail_data, Toast.LENGTH_SHORT).show();
			return true;
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		final int id = item.getItemId();
		if (R.id.action_query == id)
		{
			query();
			return true;
		}
		else if (R.id.action_source == id)
		{
			requestSource();
			return true;
		}
		else if (R.id.action_demo == id)
		{
			query("love");
			return true;
		}
		else if (R.id.action_query_file_provider == id)
		{
			QueryProviderActivity.isProviderAvailable(this);
			startActivity(new Intent(this, QueryProviderActivity.class));
			return true;
		}
		else if (R.id.action_download == id)
		{
			requestDownload();
			return true;
		}
		else if (R.id.action_cleanup == id)
		{
			this.deployer.cleanup();
			this.dataButton.setIcon(MainActivity.this.deployer.status() ? R.drawable.ic_action_done : R.drawable.ic_action_error);
			return true;
		}
		else if (R.id.action_others == id)
		{
			startActivity(new Intent(this, OthersActivity.class));
			return true;
		}
		else if (R.id.action_donate == id)
		{
			startActivity(new Intent(this, DonateActivity.class));
			return true;
		}
		else if (R.id.action_rate == id)
		{
			AppRate.rate(this);
			return true;
		}
		else if (R.id.action_app_settings == id)
		{
			Settings.applicationSettings(this, BuildConfig.APPLICATION_ID);
			return true;
		}
		else if (R.id.action_settings_service == id)
		{
			Intent intent = new Intent(this, SettingsActivity.class);
			intent.putExtra(SettingsActivity.INITIAL_ARG, SettingsActivity.ServicePreferenceFragment.class.getName());
			startActivity(intent);
			return true;
		}
		else if (R.id.action_settings == id)
		{
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		else if (R.id.action_finish == id)
		{
			finish();
			return true;
		}
		else if (R.id.action_kill == id)
		{
			Process.killProcess(Process.myPid());
			return true;
		}
		else
		{
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressWarnings("SameReturnValue")
	@Override
	public boolean onPrepareOptionsMenu(@NonNull final Menu menu)
	{
		final boolean providerAvailable = QueryProviderActivity.isProviderAvailable(this);
		menu.setGroupEnabled(R.id.provider_available, providerAvailable);
		final boolean dataAvailable = MainActivity.this.deployer.status();
		menu.setGroupEnabled(R.id.data_available, dataAvailable);
		return true;
	}

	// I N I T I A L I Z E

	/**
	 * Initialize
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	private void initialize()
	{
		// permissions
		Permissions.check(this);

		// test if initialized
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean initialized = sharedPref.getBoolean(Settings.PREF_INITIALIZED, false);
		if (!initialized)
		{
			// default settings
			Settings.setDefaults(this);

			// flag as initialized
			sharedPref.edit().putBoolean(Settings.PREF_INITIALIZED, true).commit();
		}
	}

	// C L I E N T   O P E R A T I O N

	/**
	 * Start client
	 */
	@SuppressWarnings("WeakerAccess")
	protected void start()
	{
		// client
		final String serviceType = Settings.getStringPref(this, Settings.PREF_SERVICE);
		if ("BroadcastService".equals(serviceType))
		{
			this.client = new TreebolicWordNetBroadcastClient(this, this, this);
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
		assert this.client != null;
		this.client.connect();
	}

	/**
	 * Stop client
	 */
	@SuppressWarnings("WeakerAccess")
	protected void stop()
	{
		if (this.client != null)
		{
			this.client.disconnect();
			this.client = null;
		}
	}

	// C O N N E C T I O N   L I S T E N E R

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

	// Q U E R Y

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
	 * @param source source
	 */
	private boolean query(final String source)
	{
		return query(source, Settings.getStringPref(this, TreebolicIface.PREF_BASE), Settings.getStringPref(this, TreebolicIface.PREF_IMAGEBASE), Settings.getStringPref(this, TreebolicIface.PREF_SETTINGS));
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
	@SuppressWarnings("WeakerAccess")
	protected boolean query(@Nullable final String query, final String base, final String imageBase, final String settings)
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
		final Intent forward = MainActivity.FORWARD ? IntentFactory.makeTreebolicIntentSkeleton(new Intent(this, MainActivity.class), base, imageBase, settings) : null;
		MainActivity.this.client.requestModel(query, base, imageBase, settings, forward);
		return true;
	}

	/**
	 * Request source
	 */
	private void requestSource()
	{
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.title_choose);
		alert.setMessage(R.string.title_choose_source);
		final EditText input = new EditText(this);
		input.setMaxLines(1);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		alert.setView(input);

		alert.setPositiveButton(R.string.action_ok, (dialog, whichButton) -> {
			String value = input.getText().toString();
			Settings.putStringPref(MainActivity.this, TreebolicIface.PREF_SOURCE, value);

			updateButton();

			// query
			// query();
		});

		alert.setNegativeButton(R.string.action_cancel, (dialog, whichButton) -> {
			// canceled.
		});

		final AlertDialog dialog = alert.create();
		input.setOnEditorActionListener((view, actionId, event) -> {
			if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
			{
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
				return true;
			}
			return false;
		});
		dialog.show();
	}

	// M O D E L   L I S T E N E R

	@Override
	public void onModel(@Nullable final Model model, final String urlScheme0)
	{
		if (model != null)
		{
			final Intent intent = MainActivity.makeTreebolicIntent(this, model, null, null);

			Log.d(TAG, "Starting Treebolic");
			this.startActivity(intent);
		}
	}

	// D O W N L O A D

	private void requestDownload()
	{
		this.activityResultLauncher.launch(new Intent(this, DownloadActivity.class));
	}

	/**
	 * Make Treebolic intent
	 *
	 * @param context   content
	 * @param model     model
	 * @param base      base
	 * @param imageBase image base
	 * @return intent
	 */
	@NonNull
	@SuppressWarnings("WeakerAccess")
	static public Intent makeTreebolicIntent(@NonNull final Context context, final Model model, @SuppressWarnings("SameParameterValue") final String base, @SuppressWarnings("SameParameterValue") final String imageBase)
	{
		// parent activity to return to
		final Intent parentIntent = new Intent();
		parentIntent.setClass(context, MainActivity.class);

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

	// H E L P E R

	private void updateButton()
	{
		final ImageButton button = findViewById(R.id.queryButton);
		button.setOnClickListener(this::onClick);
		final TextView sourceText = findViewById(R.id.querySource);
		final String source = Settings.getStringPref(this, TreebolicIface.PREF_SOURCE);
		final boolean qualifies = sourceQualifies(source);
		button.setVisibility(qualifies ? View.VISIBLE : View.INVISIBLE);
		sourceText.setVisibility(qualifies ? View.VISIBLE : View.INVISIBLE);
		if (qualifies)
		{
			sourceText.setText(source);
		}
	}

	/**
	 * Whether source qualifies
	 *
	 * @return true if source qualifies
	 */
	private boolean sourceQualifies(@Nullable final String source)
	{
		return source != null && !source.isEmpty();
	}

	// C L I C K

	/**
	 * Click listener
	 *
	 * @param view view
	 */
	public void onClick(final View view)
	{
		query();
	}

	// F R A G M E N T

	/**
	 * A placeholder fragment containing a simple view.
	 */
	@SuppressWarnings("WeakerAccess")
	public static class PlaceholderFragment extends Fragment
	{
		@Override
		public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.fragment_main, container, false);
		}
	}
}
