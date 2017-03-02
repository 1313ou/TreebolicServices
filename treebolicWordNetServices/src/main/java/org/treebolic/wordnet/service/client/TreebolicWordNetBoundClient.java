package org.treebolic.wordnet.service.client;

import org.treebolic.clients.TreebolicBoundClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;

import android.content.Context;

/**
 * Treebolic WordNet bound client
 *
 * @author Bernard Bou
 */
public class TreebolicWordNetBoundClient extends TreebolicBoundClient
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
	public TreebolicWordNetBoundClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, "org.treebolic.wordnet.service" + '/' + org.treebolic.wordnet.service.TreebolicWordNetBoundService.class.getName(), connectionListener,
				modelListener);
	}
}
