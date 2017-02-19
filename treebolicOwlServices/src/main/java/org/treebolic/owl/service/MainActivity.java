package org.treebolic.owl.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import treebolic.model.Model;

/**
 * Treebolic Owl main activity. The activity obtains a model from data and requests Treebolic server to visualize it.
 *
 * @author Bernard Bou
 */
public class MainActivity extends Activity implements IConnectionListener, IModelListener
{
	/**
	 * Log tag
	 */
	static private final String TAG = "Treebolic Owl Activity"; //$NON-NLS-1$

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
	private ITreebolicClient client;

	// L I F E C Y C L E

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// init
		initialize();

		// view
		setContentView(R.layout.activity_main);

		// fragment
		if (savedInstanceState == null)
		{
			PlaceholderFragment fragment = new PlaceholderFragment();
			getFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume()
	{
		stop();
		start();
		updateButton();
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause()
	{
		stop();
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		// inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
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

		case R.id.action_choose:
			requestSource();
			return true;

		case R.id.action_download:
			startActivity(new Intent(this, DownloadActivity.class));
			return true;

		case R.id.action_app_settings:
			Settings.applicationSettings(this, "org.treebolic.owl.service"); //$NON-NLS-1$
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

		case R.id.action_kill:
			Process.killProcess(Process.myPid());
			return true;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Initialize
	 */
	@SuppressLint("CommitPrefEdits")
	private void initialize()
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		// test if initialized
		final boolean result = sharedPref.getBoolean(Settings.PREF_INITIALIZED, false);
		if (result)
			return;

		// default settings
		Settings.setDefaults(this);

		// deploy
		Storage.expandZipAssetFile(this, Settings.DEMOZIP);

		// flag as initialized
		sharedPref.edit().putBoolean(Settings.PREF_INITIALIZED, true).commit();
	}

	// C L I E N T O P E R A T I O N

	/**
	 * Start client
	 */
	protected void start()
	{
		// client
		final String serviceType = Settings.getStringPref(this, Settings.PREF_SERVICE);
		if ("IntentService".equals(serviceType)) //$NON-NLS-1$
		{
			this.client = new TreebolicOwlIntentClient(this, this, this);
		}
		else if ("Messenger".equals(serviceType)) //$NON-NLS-1$
		{
			this.client = new TreebolicOwlMessengerClient(this, this, this);
		}
		else if ("AIDLBound".equals(serviceType)) //$NON-NLS-1$
		{
			this.client = new TreebolicOwlAIDLBoundClient(this, this, this);
		}
		else if ("Bound".equals(serviceType)) //$NON-NLS-1$
		{
			this.client = new TreebolicOwlBoundClient(this, this, this);
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
	 * @param source
	 *            source
	 * @param base
	 *            doc base
	 * @param imageBase
	 *            image base
	 * @param settings
	 *            settings
	 * @return true if query was made
	 */
	protected boolean query(final String source, final String base, final String imageBase, final String settings)
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
		@SuppressWarnings("ConstantConditions") final Intent forward = MainActivity.FORWARD ? IntentFactory.makeTreebolicIntentSkeleton(new Intent(this, org.treebolic.owl.service.MainActivity.class),
				base, imageBase, settings) : null;
		MainActivity.this.client.requestModel(source, base, imageBase, settings, forward);
		return true;
	}

	/**
	 * Query request from zipped bundle file
	 *
	 * @param archiveUri
	 *            archive uri
	 */
	private void queryBundle(final Uri archiveUri)
	{
		try
		{
			// choose bundle entry
			EntryChooser.choose(this, new File(archiveUri.getPath()), new EntryChooser.Callback()
			{
				@Override
				public void onSelect(final String zipEntry)
				{
					final String base = "jar:" + archiveUri.toString() + "!/"; //$NON-NLS-1$ //$NON-NLS-2$
					query(zipEntry, base, Settings.getStringPref(MainActivity.this, TreebolicIface.PREF_IMAGEBASE),
							Settings.getStringPref(MainActivity.this, TreebolicIface.PREF_SETTINGS));
				}
			});
		}
		catch (final IOException e)
		{
			Log.d(MainActivity.TAG, "Failed to start treebolic from bundle uri " + archiveUri, e); //$NON-NLS-1$
		}
	}

	// C O N N E C T I O N L I S T E N E R

	/*
	 * (non-Javadoc)
	 *
	 * @see org.treebolic.services.iface.IConnectionListener#onConnected(boolean)
	 */
	@Override
	public void onConnected(final boolean flag)
	{
		// url hook
		String query = getIntent().getStringExtra(TreebolicIface.ARG_SOURCE);
		if (query != null)
		{
			if (query.startsWith("owl:")) //$NON-NLS-1$
			{
				query = query.substring(8);
				query(query);
			}
		}
	}

	// M O D E L L I S T E N E R

	/*
	 * (non-Javadoc)
	 *
	 * @see org.treebolic.clients.iface.IModelListener#onModel(treebolic.model.Model, java.lang.String)
	 */
	@Override
	public void onModel(final Model model, final String urlScheme0)
	{
		if (model != null)
		{
			final Intent intent = MainActivity.makeTreebolicIntent(this, model, null, null);

			Log.d(MainActivity.TAG, "Starting Treebolic"); //$NON-NLS-1$
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

	// S P E C I F I C R E T U R N S

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent returnIntent)
	{
		switch (requestCode)
		{
		case REQUEST_FILE_CODE:
			if (resultCode == Activity.RESULT_OK)
			{
				final Uri fileUri = returnIntent.getData();
				if (fileUri == null)
				{
					break;
				}

				Toast.makeText(this, fileUri.toString(), Toast.LENGTH_SHORT).show();
				final File file = new File(fileUri.getPath());
				final String parent = file.getParent();
				final File parentFile = new File(parent);
				final Uri parentUri = Uri.fromFile(parentFile);
				final String query = file.getName();
				String base = parentUri.toString();
				if (base != null && !base.endsWith("/")) //$NON-NLS-1$
				{
					base += '/';
				}
				Settings.save(this, query, base);
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, returnIntent);
	}

	/**
	 * Request source
	 */
	private void requestSource()
	{
		final Intent intent = new Intent(this, org.treebolic.filechooser.FileChooserActivity.class);
		intent.setType("application/rdf+xml"); //$NON-NLS-1$
		intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_INITIAL_DIR, Settings.getStringPref(this, TreebolicIface.PREF_BASE));
		intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_EXTENSION_FILTER, new String[] { "owl", "rdf" }); //$NON-NLS-1$ //$NON-NLS-2$
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, MainActivity.REQUEST_FILE_CODE);
	}

	// H E L P E R

	private void updateButton()
	{
		final Button button = (Button) findViewById(R.id.queryButton);
		button.setVisibility(sourceSet() ? View.VISIBLE : View.INVISIBLE);
	}

	/**
	 * Whether source is set
	 *
	 * @return true if source is set
	 */
	private boolean sourceSet()
	{
		final String source = Settings.getStringPref(this, TreebolicIface.PREF_SOURCE);
		final String base = Settings.getStringPref(this, TreebolicIface.PREF_BASE);
		if (source != null && !source.isEmpty())
		{
			final File baseFile = base == null ? null : new File(Uri.parse(base).getPath());
			final File file = new File(baseFile, source);
			Log.d(MainActivity.TAG, "file=" + file); //$NON-NLS-1$
			return file.exists();
		}
		return false;
	}

	// F R A G M E N T

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment
	{
		/**
		 * Progress bar
		 */
		ProgressBar progressBar;

		/**
		 * Query button
		 */
		private Button queryButton;

		/**
		 * Constructor
		 */
		public PlaceholderFragment()
		{
			//
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
		 */
		@Override
		public void onActivityCreated(final Bundle savedInstanceState)
		{
			super.onActivityCreated(savedInstanceState);

			final Activity activity = getActivity();

			// get handle to button
			this.queryButton = (Button) activity.findViewById(R.id.queryButton);
			this.queryButton.setOnClickListener(new View.OnClickListener()
			{
				/*
				 * (non-Javadoc)
				 *
				 * @see android.view.View.OnClickListener#onClick(android.view.View)
				 */
				@SuppressWarnings("synthetic-access")
				@Override
				public void onClick(final View v)
				{
					final MainActivity mainActivity = (MainActivity) activity;
					mainActivity.query();
				}
			});
			// get handle to progress bar, and button
			this.progressBar = (ProgressBar) activity.findViewById(R.id.progressBar);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
		 */
		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.fragment_main, container, false);
		}
	}
}
