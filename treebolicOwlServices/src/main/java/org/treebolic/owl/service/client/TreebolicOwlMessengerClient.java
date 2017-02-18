package org.treebolic.owl.service.client;

import org.treebolic.clients.TreebolicMessengerClient;
import org.treebolic.clients.iface.IConnectionListener;
import org.treebolic.clients.iface.IModelListener;

import android.content.Context;

/**
 * Treebolic Owl messenger bound client
 *
 * @author Bernard Bou
 */
public class TreebolicOwlMessengerClient extends TreebolicMessengerClient
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
	public TreebolicOwlMessengerClient(final Context context, final IConnectionListener connectionListener, final IModelListener modelListener)
	{
		super(context, "org.treebolic.owl.service" + '/' + org.treebolic.owl.service.TreebolicOwlMessengerService.class.getName(), connectionListener, //$NON-NLS-1$
				modelListener);
	}
}
