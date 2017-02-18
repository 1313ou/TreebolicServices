package org.treebolic.services.iface;

/**
 * Service interface used in client/service interaction
 *
 * @author Bernard Bou
 */
public interface ITreebolicService
{
	// intent service

	public static final String ACTION_MAKEMODEL = "org.treebolic.service.action.MAKE_MODEL";

	// messenging service

	static public final int MSG_REGISTER_CLIENT = 1;

	static public final int MSG_UNREGISTER_CLIENT = 2;

	static public final int MSG_REQUEST_MODEL = 3;

	static public final int MSG_RESULT_MODEL = 4;

	// arguments

	public static final String EXTRA_SOURCE = "org.treebolic.service.extra.SOURCE";

	public static final String EXTRA_BASE = "org.treebolic.service.extra.BASE";

	public static final String EXTRA_IMAGEBASE = "org.treebolic.service.extra.IMAGEBASE";

	public static final String EXTRA_SETTINGS = "org.treebolic.service.extra.SETTINGS";

	public static final String EXTRA_RECEIVER = "org.treebolic.service.extra.RECEIVER";

	public static final String EXTRA_FORWARD_RESULT_TO = "org.treebolic.service.FORWARDTO";

	// result

	public static final String RESULT_MODEL = "org.treebolic.service.MODEL";

	public static final String RESULT_SERIALIZED = "org.treebolic.service.SERIALIZED";

	public static final String RESULT_URLSCHEME = "org.treebolic.service.URLSCHEME";

	/**
	 * Get service url scheme
	 *
	 * @return service url scheme
	 */
	public String getUrlScheme();
}
