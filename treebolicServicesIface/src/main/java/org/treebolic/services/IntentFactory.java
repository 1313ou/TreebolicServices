/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.services;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import org.treebolic.ParcelableModel;
import org.treebolic.TreebolicIface;
import org.treebolic.services.iface.ITreebolicService;

import androidx.annotation.NonNull;
import treebolic.model.Model;

@SuppressWarnings("WeakerAccess")
public class IntentFactory
{
	// M O D E L

	/**
	 * Make intent
	 *
	 * @param model        model
	 * @param parentIntent parent activity to return to
	 * @param base         base
	 * @param imageBase    image base
	 * @param settings     settings
	 * @return intent
	 */
	@NonNull
	static public Intent makeTreebolicIntent(final Model model, final Intent parentIntent, final String base, final String imageBase, final String settings)
	{
		final Intent intent = IntentFactory.makeTreebolicIntentSkeleton(parentIntent, base, imageBase, settings);
		intent.putExtra(TreebolicIface.ARG_MODEL, new ParcelableModel(model));
		return intent;
	}

	/**
	 * Treebolic skeleton intent without model)
	 *
	 * @param parentIntent parent activity to return to
	 * @param base         base
	 * @param imageBase    image base
	 * @param settings     settings
	 * @return treebolic model activity intent
	 */
	@NonNull
	@SuppressWarnings("WeakerAccess")
	static public Intent makeTreebolicIntentSkeleton(final Intent parentIntent, final String base, final String imageBase, final String settings)
	{
		final Intent intent = new Intent();
		intent.setComponent(new ComponentName(TreebolicIface.PKG_TREEBOLIC, TreebolicIface.ACTIVITY_MODEL));
		intent.putExtra(TreebolicIface.ARG_BASE, base);
		intent.putExtra(TreebolicIface.ARG_IMAGEBASE, imageBase);
		intent.putExtra(TreebolicIface.ARG_SETTINGS, settings);
		intent.putExtra(TreebolicIface.ARG_PARENTACTIVITY, parentIntent);
		return intent;
	}

	/**
	 * Put model in activity intent
	 *
	 * @param bundle    bundle
	 * @param model     model to forward
	 * @param urlScheme url scheme
	 */
	static public void putModelResult(@NonNull final Bundle bundle, final Model model, final String urlScheme)
	{
		if (ParcelableModel.SERIALIZE)
		{
			bundle.putBoolean(ITreebolicService.RESULT_SERIALIZED, true);
			bundle.putSerializable(ITreebolicService.RESULT_MODEL, model);
		}
		else
		{
			bundle.putBoolean(ITreebolicService.RESULT_SERIALIZED, false);
			bundle.putParcelable(ITreebolicService.RESULT_MODEL, new ParcelableModel(model));
		}
		bundle.putString(ITreebolicService.RESULT_URLSCHEME, urlScheme);
	}

	/**
	 * Put model in activity intent
	 *
	 * @param bundle    bundle
	 * @param model     model to forward
	 * @param urlScheme url scheme
	 */
	@SuppressWarnings("WeakerAccess")
	static public void putModelArg(@NonNull final Bundle bundle, final Model model, final String urlScheme)
	{
		if (ParcelableModel.SERIALIZE)
		{
			bundle.putBoolean(TreebolicIface.ARG_SERIALIZED, true);
			bundle.putSerializable(TreebolicIface.ARG_MODEL, model);
		}
		else
		{
			bundle.putBoolean(TreebolicIface.ARG_SERIALIZED, false);
			bundle.putParcelable(TreebolicIface.ARG_MODEL, new ParcelableModel(model));
		}
		bundle.putString(TreebolicIface.ARG_URLSCHEME, urlScheme);
	}

	/**
	 * Put model in activity intent as argument
	 *
	 * @param forwardIntent forward intent
	 * @param model         model to forward
	 */
	static public void putModelArg(@NonNull final Intent forwardIntent, final Model model, final String urlScheme)
	{
		final Bundle forwardBundle = new Bundle();
		IntentFactory.putModelArg(forwardBundle, model, urlScheme);
		forwardIntent.putExtras(forwardBundle);
	}
}
