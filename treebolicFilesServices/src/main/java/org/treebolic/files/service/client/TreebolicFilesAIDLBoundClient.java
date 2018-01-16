package org.treebolic.files.service.client;

import android.content.Context;

import org.treebolic.clients.TreebolicAIDLBoundClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;

/**
 * Treebolic Files bound client
 *
 * @author Bernard Bou
 */
public class TreebolicFilesAIDLBoundClient extends TreebolicAIDLBoundClient
{
	/**
	 * Constructor
	 *
	 * @param context            context
	 * @param connectionListener connection listener
	 * @param modelListener      model listener
	 */
	public TreebolicFilesAIDLBoundClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, "org.treebolic.files.service" + '/' + org.treebolic.files.service.TreebolicFilesAIDLBoundService.class.getName(), connectionListener, modelListener);
	}
}
