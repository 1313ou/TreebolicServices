package org.treebolic.wordnet.service.client;

import org.treebolic.clients.TreebolicMessengerClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;

import android.content.Context;

/**
 * Treebolic WordNet messenger bound client
 *
 * @author Bernard Bou
 */
public class TreebolicWordNetMessengerClient extends TreebolicMessengerClient
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
	public TreebolicWordNetMessengerClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, "org.treebolic.wordnet.service" + '/' + org.treebolic.wordnet.service.TreebolicWordNetMessengerService.class.getName(), //$NON-NLS-1$
				connectionListener, modelListener);
	}
}