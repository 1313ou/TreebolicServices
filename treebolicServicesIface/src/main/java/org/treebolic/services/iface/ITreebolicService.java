package org.treebolic.services.iface;

import androidx.annotation.NonNull;

/**
 * Service interface used in client/service interaction
 *
 * @author Bernard Bou
 */
public interface ITreebolicService
{
	// intent service

	String ACTION_MAKEMODEL = "org.treebolic.service.action.MAKE_MODEL";

	// messaging service

	int MSG_REGISTER_CLIENT = 1;

	int MSG_UNREGISTER_CLIENT = 2;

	int MSG_REQUEST_MODEL = 3;

	int MSG_RESULT_MODEL = 4;

	// arguments

	String EXTRA_SOURCE = "org.treebolic.service.extra.SOURCE";

	String EXTRA_BASE = "org.treebolic.service.extra.BASE";

	String EXTRA_IMAGEBASE = "org.treebolic.service.extra.IMAGEBASE";

	String EXTRA_SETTINGS = "org.treebolic.service.extra.SETTINGS";

	String EXTRA_RECEIVER = "org.treebolic.service.extra.RECEIVER";

	String EXTRA_FORWARD_RESULT_TO = "org.treebolic.service.FORWARDTO";

	// result

	String RESULT_MODEL = "org.treebolic.service.MODEL";

	String RESULT_SERIALIZED = "org.treebolic.service.SERIALIZED";

	String RESULT_URLSCHEME = "org.treebolic.service.URLSCHEME";

	/**
	 * Get service url scheme
	 *
	 * @return service url scheme
	 */
	@NonNull
	String getUrlScheme();
}
