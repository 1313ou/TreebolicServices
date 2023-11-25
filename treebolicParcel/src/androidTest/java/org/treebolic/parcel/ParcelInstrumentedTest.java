package org.treebolic.parcel;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.treebolic.ParcelableModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import treebolic.glue.iface.Colors;
import treebolic.model.INode;
import treebolic.model.Model;
import treebolic.model.ModelDump;
import treebolic.model.Settings;
import treebolic.model.Tree;
import treebolic.model.TreeMutableNode;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ParcelInstrumentedTest
{
	static private final String MODEL_KEYA = "modelA";
	static private final String MODEL_KEYB = "modelB";

	@Test
	public void packageName_isCorrect()
	{
		// Context of the app under test.
		Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
		assertEquals("org.treebolic.parcel.test", appContext.getPackageName());
	}

	@Test
	public void parcelizationA1_isCorrect()
	{
		final INode root = makeDefaultTree();
		final Model modelA = new Model(new Tree(root, null), new Settings());
		final String strA = ModelDump.toString(modelA);

		final Bundle bundle = new Bundle();
		putParceledModel(modelA, bundle, MODEL_KEYA);

		final Parcel parcel = writeParcel(bundle);
		final Bundle bundle2 = readParcel(parcel);
		parcel.recycle();

		final Model modelA2 = getParceledModel(bundle2, MODEL_KEYA);
		final String strA2 = ModelDump.toString(modelA2);

		assertEquals(strA, strA2);
	}

	@Test
	public void parcelizationA2_isCorrect()
	{
		final Model modelA = getSerialized("test.ser");
		final String strA = ModelDump.toString(modelA);

		final Bundle bundle = new Bundle();
		putParceledModel(modelA, bundle, MODEL_KEYA);

		final Parcel parcel = writeParcel(bundle);
		final Bundle bundle2 = readParcel(parcel);
		parcel.recycle();

		final Model modelA2 = getParceledModel(bundle2, MODEL_KEYA);
		final String strA2 = ModelDump.toString(modelA2);

		assertEquals(strA, strA2);
	}

	@Test
	public void parcelizationB2_isCorrect()
	{
		final Model modelB = getSerialized("monarchy.ser");
		final String strB = ModelDump.toString(modelB);

		final Bundle bundle = new Bundle();
		putParceledModel(modelB, bundle, MODEL_KEYA);

		final Parcel parcel = writeParcel(bundle);
		final Bundle bundle2 = readParcel(parcel);
		parcel.recycle();

		final Model modelB2 = getParceledModel(bundle2, MODEL_KEYA);
		final String strB2 = ModelDump.toString(modelB2);

		assertEquals(strB, strB2);
	}

	@Test
	public void parcelization2_isCorrect()
	{
		final Model modelA = getSerialized("test.ser");
		final String strA = ModelDump.toString(modelA);

		final Model modelB = getSerialized("monarchy.ser");
		final String strB = ModelDump.toString(modelB);

		final Bundle bundle = new Bundle();
		putParceledModel(modelA, bundle, MODEL_KEYA);
		putParceledModel(modelB, bundle, MODEL_KEYB);

		final Parcel parcel = writeParcel(bundle);
		final Bundle bundle2 = readParcel(parcel);
		parcel.recycle();

		final Model modelA2 = getParceledModel(bundle2, MODEL_KEYA);
		final Model modelB2 = getParceledModel(bundle2, MODEL_KEYB);
		final String strA2 = ModelDump.toString(modelA2);
		final String strB2 = ModelDump.toString(modelB2);

		assertEquals(strA, strA2);
		assertEquals(strB, strB2);
	}

	public static Parcel writeParcel(@NonNull final Bundle bundle)
	{
		// write bundle to parcel
		final Parcel parcel = Parcel.obtain();
		bundle.writeToParcel(parcel, 0);
		return parcel;
	}

	public static Bundle readParcel(@NonNull final Parcel parcel)
	{
		// extract bundle from parcel
		parcel.setDataPosition(0);
		final Bundle bundle2 = parcel.readBundle(ParcelableModel.class.getClassLoader());
		if (bundle2 != null)
		{
			bundle2.setClassLoader(Model.class.getClassLoader());
		}
		return bundle2;
	}

	public static void putParceledModel(final Model model, @NonNull final Bundle bundle, final String key)
	{
		final ParcelableModel parcelableModel = new ParcelableModel(model);
		bundle.putParcelable(key, parcelableModel);
	}

	@Nullable
	public static Model getParceledModel(@NonNull final Bundle bundle, final String key)
	{
		// model2
		final ParcelableModel pmodel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? //
				bundle.getParcelable(key, ParcelableModel.class) : //
				bundle.getParcelable(key);
		return pmodel != null ? pmodel.getModel() : null;
	}

	@NonNull
	public static INode makeDefaultTree()
	{
		final String[][] data = { //
				{"id1", "one\n1", "id11", "eleven\n11", "id12", "twelve\n12", "id13", "thirteen\n13", "id14", "fourteen\n14"}, //
				{"id2", "two\n2", "id21", "twenty-one\n21", "id22", "twenty-two\n22", "id23", "twenty-three\n23"}, //
				{"id3", "three\n3", "id31", "thirty-one\n31", "id32", "thirty-two\n32"}, //
				{"id4", "four\n4", "id41", "forty-one\n41"}, //
				{"id5", "five\n5"}}; //
		final TreeMutableNode root = new TreeMutableNode(null, "root"); //$NON-NLS-1$
		root.setLabel("root"); //$NON-NLS-1$
		root.setBackColor(Colors.ORANGE);
		root.setForeColor(Colors.BLACK);
		for (final String[] nodeData : data)
		{
			final TreeMutableNode node = new TreeMutableNode(root, nodeData[0]);
			node.setLabel(nodeData[1]);
			for (int i = 2; i < nodeData.length; i += 2)
			{
				final TreeMutableNode childNode = new TreeMutableNode(node, nodeData[i]);
				childNode.setLabel(nodeData[i + 1]);
			}
		}
		return root;
	}

	@Nullable
	private Model getSerialized(@NonNull final String asset)
	{
		final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
		final AssetManager assets = appContext.getAssets();
		try (InputStream is = assets.open(asset); ZipInputStream zis = new ZipInputStream(is))
		{
			return (Model) deserializeZip(zis, "model");
		}
		catch (IOException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	static public Object deserializeZip(@NonNull final ZipInputStream zis, @SuppressWarnings("SameParameterValue") final String targetEntry) throws IOException, ClassNotFoundException
	{
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null)
		{
			if (entry.getName().equals(targetEntry))
			{
				try (ObjectInputStream objectInputStream = new ObjectInputStream(zis))
				{
					return objectInputStream.readObject();
				}
			}
		}
		throw new IOException("zip entry not found " + targetEntry);
	}
}
