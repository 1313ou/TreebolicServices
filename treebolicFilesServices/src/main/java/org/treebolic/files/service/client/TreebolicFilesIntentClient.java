package org.treebolic.files.service.client;

import org.treebolic.clients.TreebolicIntentClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;

import android.content.Context;

/**
 * Treebolic Files intent service client
 *
 * @author Bernard Bou
 */
public class TreebolicFilesIntentClient extends TreebolicIntentClient
{
	/**
	 * Constructor
	 *
	 * @param context
	 *            context
	 * @param connectionListener
	 *            connection listener
	 * @param modelListener
	 *            model listener
	 */
	public TreebolicFilesIntentClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, "org.treebolic.files.service" + '/' + org.treebolic.files.service.TreebolicFilesIntentService.class.getName(), connectionListener, //$NON-NLS-1$
				modelListener);
	}
}
