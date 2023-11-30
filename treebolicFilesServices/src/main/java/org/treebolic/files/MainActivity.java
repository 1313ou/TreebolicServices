/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.files;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bbou.donate.DonateActivity;
import com.bbou.others.OthersActivity;
import com.bbou.rate.AppRate;

import org.treebolic.AppCompatCommonActivity;
import org.treebolic.Models;
import org.treebolic.ParcelableModel;
import org.treebolic.TreebolicIface;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;
import org.treebolic.clients.iface.ITreebolicClient;
import org.treebolic.filechooser.FileChooserActivity;
import org.treebolic.files.service.R;
import org.treebolic.files.service.client.TreebolicFilesAIDLBoundClient;
import org.treebolic.files.service.client.TreebolicFilesBoundClient;
import org.treebolic.files.service.client.TreebolicFilesBroadcastClient;
import org.treebolic.files.service.client.TreebolicFilesMessengerClient;
import org.treebolic.services.IntentFactory;

import java.io.File;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import treebolic.model.Model;

/**
 * Treebolic Files main activity. The activity obtains a model from source and requests Treebolic server to visualize it.
 *
 * @author Bernard Bou
 */
public class MainActivity extends AppCompatCommonActivity implements IConnectionListener, IModelListener
{
	/**
	 * Log tag
	 */
	static private final String TAG = "ServiceFilesA";

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

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE);
		}

		// init
		initialize();

		// activity result launcher
		this.activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			boolean success = result.getResultCode() == Activity.RESULT_OK;
			if (success)
			{
				// handle selection of target by other activity which returns selected target
				Intent returnIntent = result.getData();
				if (returnIntent != null)
				{
					final Uri fileUri = returnIntent.getData();
					if (fileUri != null)
					{
						Toast.makeText(this, fileUri.toString(), Toast.LENGTH_SHORT).show();
						Settings.putStringPref(this, TreebolicIface.PREF_SOURCE, fileUri.getPath());

						updateButton();

						// query
						// query());
					}
				}
			}
		});

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
		if (R.id.action_places == id)
		{
			chooseAndSave();
			return true;
		}
		else if (R.id.action_run == id)
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
			chooseAndTryStartTreebolic();
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
			Settings.applicationSettings(this, "org.treebolic.files.service");
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

	// R E Q U E S T (choose source)

	/**
	 * Request directory source
	 */
	private void requestSource()
	{
		final Intent intent = new Intent(this, org.treebolic.filechooser.FileChooserActivity.class);
		intent.setType("inode/directory");
		intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_INITIAL_DIR, StorageExplorer.discoverExternalStorage(this));
		intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_CHOOSE_DIR, true);
		intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_EXTENSION_FILTER, new String[]{});
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		this.activityResultLauncher.launch(intent);
	}

	abstract static class Runnable1
	{
		@SuppressWarnings("WeakerAccess")
		abstract public void run(final String arg);
	}

	/**
	 * Choose dir and scan
	 */
	private void chooseAndTryStartTreebolic()
	{
		choosePlace(new Runnable1()
		{
			@Override
			public void run(final String arg)
			{
				query(arg + File.separatorChar);
			}
		});
	}

	/**
	 * Choose dir and save
	 */
	private void chooseAndSave()
	{
		choosePlace(new Runnable1()
		{
			@Override
			public void run(final String arg)
			{
				Settings.putStringPref(MainActivity.this, TreebolicIface.PREF_SOURCE, arg);
				updateButton();
			}
		});
	}

	/**
	 * Choose dir to scan
	 *
	 * @param runnable1 what to do
	 */

	private void choosePlace(@NonNull final Runnable1 runnable1)
	{
		final Pair<CharSequence[], CharSequence[]> result = StorageExplorer.getDirectoriesTypesValues(this);
		final CharSequence[] types = result.first;
		final CharSequence[] values = result.second;

		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.title_choose);
		alert.setMessage(R.string.title_choose_directory);

		final RadioGroup input = new RadioGroup(this);
		for (int i = 0; i < types.length && i < values.length; i++)
		{
			final CharSequence type = types[i];
			final CharSequence value = values[i];
			final File dir = new File(value.toString());
			if (dir.exists())
			{
				final RadioButton radioButton = new RadioButton(this);
				final String path = dir.getAbsolutePath();
				final String str = path + ' ' + '[' + type + ']';
				radioButton.setText(str);
				radioButton.setTag(path);
				input.addView(radioButton);
			}
		}
		alert.setView(input);
		alert.setPositiveButton(R.string.action_ok, (dialog, whichButton) -> {
			dialog.dismiss();

			int childCount = input.getChildCount();
			for (int i = 0; i < childCount; i++)
			{
				final RadioButton radioButton = (RadioButton) input.getChildAt(i);
				if (radioButton.getId() == input.getCheckedRadioButtonId())
				{
					final String sourceFile = radioButton.getTag().toString();
					final File sourceDir = new File(sourceFile);
					if (sourceDir.exists() && sourceDir.isDirectory())
					{
						runnable1.run(sourceFile + File.separatorChar);
					}
					else
					{
						final AlertDialog.Builder alert2 = new AlertDialog.Builder(MainActivity.this);
						alert2.setTitle(sourceFile) //
								.setMessage(getString(R.string.status_fail)) //
								.show();
					}
				}
			}
		});
		alert.setNegativeButton(R.string.action_cancel, (dialog, whichButton) -> {
			// canceled.
		});
		alert.show();
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
			this.client = new TreebolicFilesBroadcastClient(this, this, this);
		}
		else if ("Messenger".equals(serviceType))
		{
			this.client = new TreebolicFilesMessengerClient(this, this, this);
		}
		else if ("AIDLBound".equals(serviceType))
		{
			this.client = new TreebolicFilesAIDLBoundClient(this, this, this);
		}
		else if ("Bound".equals(serviceType))
		{
			this.client = new TreebolicFilesBoundClient(this, this, this);
		}

		// connect
		if (this.client == null)
		{
			return;
		}
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
			if (query.startsWith("directory:"))
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
	private boolean query(@Nullable final String source)
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
		final Intent forward = MainActivity.FORWARD ? IntentFactory.makeTreebolicIntentSkeleton(new Intent(this, MainActivity.class), null, null, null) : null;
		MainActivity.this.client.requestModel(source, null, null, null, forward);
		return true;
	}

	// M O D E L   L I S T E N E R

	@Override
	public void onModel(@Nullable final Model model, final String urlScheme0)
	{
		if (model != null)
		{
			final Intent intent = MainActivity.makeTreebolicIntent(this, model);

			Log.d(MainActivity.TAG, "Starting treebolic");
			this.startActivity(intent);
		}
	}

	/**
	 * Make Treebolic intent
	 *
	 * @param context content
	 * @param model   model
	 * @return intent
	 */
	@NonNull
	@SuppressWarnings("WeakerAccess")
	static public Intent makeTreebolicIntent(@NonNull final Context context, final Model model)
	{
		// parent activity to return to
		final Intent parentIntent = new Intent();
		parentIntent.setClass(context, MainActivity.class);

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
		intent.putExtra(TreebolicIface.ARG_BASE, (String) null);
		intent.putExtra(TreebolicIface.ARG_IMAGEBASE, (String) null);

		// parent passing
		intent.putExtra(TreebolicIface.ARG_PARENTACTIVITY, parentIntent);

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
		if (source != null && !source.isEmpty())
		{
			final File file = new File(source);
			Log.d(MainActivity.TAG, "file=" + file);
			return file.exists() && file.isDirectory();
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
