/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.owl.service;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.treebolic.AppCompatCommonActivity;
import org.treebolic.Models;
import org.treebolic.ParcelableModel;
import org.treebolic.TreebolicIface;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.clients.iface.ITreebolicClient;
import org.treebolic.filechooser.EntryChooser;
import org.treebolic.filechooser.FileChooserActivity;
import org.treebolic.owl.service.client.TreebolicOwlAIDLBoundClient;
import org.treebolic.owl.service.client.TreebolicOwlBoundClient;
import org.treebolic.owl.service.client.TreebolicOwlIntentClient;
import org.treebolic.owl.service.client.TreebolicOwlMessengerClient;
import org.treebolic.services.IntentFactory;
import org.treebolic.storage.Storage;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import treebolic.model.Model;

/**
 * Treebolic Owl main activity. The activity obtains a model from data and requests Treebolic server to visualize it.
 *
 * @author Bernard Bou
 */
public class MainActivity extends AppCompatCommonActivity implements IConnectionListener, IModelListener
{
	/**
	 * Log tag
	 */
	static private final String TAG = "Treebolic Owl Activity";

	/**
	 * File request code
	 */
	private static final int REQUEST_FILE_CODE = 1;

	/**
	 * Whether to forward model directly to activity
	 */
	static private final boolean FORWARD = true;

	/**
	 * Client
	 */
	@Nullable
	private ITreebolicClient client;

	// L I F E C Y C L E

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// view
		setContentView(R.layout.activity_main);

		// toolbar
		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE);
		}

		// init
		initialize();

		// fragment
		if (savedInstanceState == null)
		{
			PlaceholderFragment fragment = new PlaceholderFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
		}
	}

	@Override
	protected void onResume()
	{
		stop();
		start();
		updateButton();
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		stop();
		super.onPause();
	}

	// M E N U

	@SuppressWarnings("SameReturnValue")
	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		// inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item)
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

			case R.id.action_source:
				requestSource();
				return true;

			case R.id.action_download:
				startActivity(new Intent(this, DownloadActivity.class));
				return true;

			case R.id.action_app_settings:
				Settings.applicationSettings(this, "org.treebolic.owl.service");
				return true;

			case R.id.action_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;

			case R.id.action_demo:
				final Uri archiveFileUri = Storage.copyAssetFile(this, Settings.DEMOZIP);
				if (archiveFileUri != null)
				{
					queryBundle(archiveFileUri);
				}
				return true;

			case R.id.action_finish:
				finish();
				return true;

			default:
				break;
		}
		return super.onOptionsItemSelected(item);
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

		// deploy
		final File dir = Storage.getTreebolicStorage(this);
		if (dir.isDirectory())
		{
			if (dir.list().length == 0)
			{
				// deploy
				Storage.expandZipAssetFile(this, Settings.DEMOZIP);
			}
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
		if ("IntentService".equals(serviceType))
		{
			this.client = new TreebolicOwlIntentClient(this, this, this);
		}
		else if ("Messenger".equals(serviceType))
		{
			this.client = new TreebolicOwlMessengerClient(this, this, this);
		}
		else if ("AIDLBound".equals(serviceType))
		{
			this.client = new TreebolicOwlAIDLBoundClient(this, this, this);
		}
		else if ("Bound".equals(serviceType))
		{
			this.client = new TreebolicOwlBoundClient(this, this, this);
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
			if (query.startsWith("owl:"))
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
		final String query = Settings.getStringPref(this, TreebolicIface.PREF_SOURCE);
		query(query);
	}

	/**
	 * Query request
	 *
	 * @param source source
	 */
	@SuppressWarnings("UnusedReturnValue")
	private boolean query(final String source)
	{
		return query(source, Settings.getStringPref(this, TreebolicIface.PREF_BASE), Settings.getStringPref(this, TreebolicIface.PREF_IMAGEBASE), Settings.getStringPref(this, TreebolicIface.PREF_SETTINGS));
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
	@SuppressWarnings("WeakerAccess")
	protected boolean query(@Nullable final String source, final String base, final String imageBase, final String settings)
	{
		if (source == null || source.isEmpty())
		{
			Toast.makeText(MainActivity.this, R.string.fail_nullquery, Toast.LENGTH_SHORT).show();
			return false;
		}
		else if (MainActivity.this.client == null)
		{
			Toast.makeText(MainActivity.this, R.string.fail_nullclient, Toast.LENGTH_SHORT).show();
			return false;
		}
		final Intent forward = MainActivity.FORWARD ? IntentFactory.makeTreebolicIntentSkeleton(new Intent(this, org.treebolic.owl.service.MainActivity.class), base, imageBase, settings) : null;
		MainActivity.this.client.requestModel(source, base, imageBase, settings, forward);
		return true;
	}

	/**
	 * Query request from zipped bundle file
	 *
	 * @param archiveUri archive uri
	 */
	private void queryBundle(@NonNull final Uri archiveUri)
	{
		try
		{
			// choose bundle entry
			EntryChooser.choose(this, new File(archiveUri.getPath()), zipEntry -> {
				final String base = "jar:" + archiveUri.toString() + "!/";
				query(zipEntry, base, Settings.getStringPref(MainActivity.this, TreebolicIface.PREF_IMAGEBASE), Settings.getStringPref(MainActivity.this, TreebolicIface.PREF_SETTINGS));
			});
		}
		catch (@NonNull final IOException e)
		{
			Log.d(MainActivity.TAG, "Failed to start treebolic from bundle uri " + archiveUri, e);
		}
	}

	// M O D E L   L I S T E N E R

	@Override
	public void onModel(@Nullable final Model model, final String urlScheme0)
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
		parentIntent.setClass(context, org.treebolic.owl.service.MainActivity.class);

		// intent
		final Intent intent = new Intent();
		intent.setComponent(new ComponentName(TreebolicIface.PKG_TREEBOLIC, TreebolicIface.ACTIVITY_MODEL));

		// model passing
		if (TreebolicIface.USE_MODEL_REFERENCES)
		{
			Models.set(model, intent);
		}
		else
		{
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
		}

		// other parameters passing
		intent.putExtra(TreebolicIface.ARG_BASE, base);
		intent.putExtra(TreebolicIface.ARG_IMAGEBASE, imageBase);

		// parent passing
		intent.putExtra(TreebolicIface.ARG_PARENTACTIVITY, parentIntent);

		return intent;
	}

	// R E Q U E S T (choose source)

	/**
	 * Request Owl source
	 */
	private void requestSource()
	{
		final Intent intent = new Intent(this, org.treebolic.filechooser.FileChooserActivity.class);
		intent.setType("application/rdf+xml");
		intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_INITIAL_DIR, Settings.getStringPref(this, TreebolicIface.PREF_BASE));
		intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_EXTENSION_FILTER, new String[]{"owl", "rdf"});
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, MainActivity.REQUEST_FILE_CODE);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent returnIntent)
	{
		if (requestCode == REQUEST_FILE_CODE)
		{
			if (resultCode == AppCompatActivity.RESULT_OK && returnIntent != null)
			{
				final Uri fileUri = returnIntent.getData();
				if (fileUri != null)
				{
					Toast.makeText(this, fileUri.toString(), Toast.LENGTH_SHORT).show();
					final File file = new File(fileUri.getPath());
					final String parent = file.getParent();
					final File parentFile = new File(parent);
					final Uri parentUri = Uri.fromFile(parentFile);
					final String query = file.getName();
					String base = parentUri.toString();
					if (!base.endsWith("/"))
					{
						base += '/';
					}
					Settings.save(this, query, base);
				}

			}

			updateButton();

			// query
			// query();
		}
		super.onActivityResult(requestCode, resultCode, returnIntent);
	}

	// H E L P E R

	private void updateButton()
	{
		final ImageButton button = findViewById(R.id.queryButton);
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
		final String base = Settings.getStringPref(this, TreebolicIface.PREF_BASE);
		if (source != null && !source.isEmpty())
		{
			final File baseFile = base == null ? null : new File(Uri.parse(base).getPath());
			final File file = new File(baseFile, source);
			Log.d(MainActivity.TAG, "file=" + file);
			return file.exists();
		}
		return false;
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
	public static class PlaceholderFragment extends Fragment
	{
		@Override
		public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.fragment_main, container, false);
		}
	}
}
