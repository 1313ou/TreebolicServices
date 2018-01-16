package org.treebolic.files.service.client;

import org.treebolic.clients.TreebolicMessengerClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;

import android.content.Context;

/**
 * Treebolic Files messenger bound client
 *
 * @author Bernard Bou
 */
public class TreebolicFilesMessengerClient extends TreebolicMessengerClient
{
	/**
	 * Constructor
	 *
	 * @param context            context
	 * @param connectionListener connection listener
	 * @param modelListener      model listener
	 */
	public TreebolicFilesMessengerClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, "org.treebolic.files.service" + '/' + org.treebolic.files.service.TreebolicFilesMessengerService.class.getName(), connectionListener, modelListener);
	}
}