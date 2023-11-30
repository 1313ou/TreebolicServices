/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import treebolic.glue.Image;
import treebolic.model.IEdge;
import treebolic.model.INode;
import treebolic.model.MenuItem;
import treebolic.model.Model;
import treebolic.model.MountPoint;
import treebolic.model.MutableEdge;
import treebolic.model.MutableNode;
import treebolic.model.Settings;
import treebolic.model.Tree;
import treebolic.model.Types.MatchMode;
import treebolic.model.Types.MatchScope;

/**
 * Convenience class to parcel model
 */
public class ParcelableModel implements Parcelable
{
	/**
	 * Log tag
	 */
	static private final String TAG = "ModelParcel";

	/**
	 * Whether model is marshaled with standard Java mechanism or android parcelization
	 */
	static public final boolean SERIALIZE = false;

	/**
	 * Image marshaling types
	 */
	private enum ImageMarshaling
	{IMAGE_SERIALIZE, IMAGE_ASBYTEARRAY, IMAGE_PARCEL}

	/**
	 * Image marshaling
	 */
	private static final ImageMarshaling IMAGEMETHOD = ImageMarshaling.IMAGE_ASBYTEARRAY;

	/**
	 * Wrapped model
	 */
	@Nullable
	private final Model model;

	// C O N S T R U C T O R

	/**
	 * Null constructor
	 */
	public ParcelableModel()
	{
		this.model = null;
	}

	/**
	 * Constructor
	 */
	public ParcelableModel(@Nullable final Model model)
	{
		this.model = model;
	}

	/**
	 * Constructor from parcel
	 *
	 * @param parcel parcel to build from
	 */
	public ParcelableModel(@NonNull final Parcel parcel)
	{
		if (ParcelableModel.SERIALIZE)
		{
			this.model = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? //
					parcel.readSerializable(null, Model.class) : //
					(Model) parcel.readSerializable();
		}
		else
		{
			this.model = ParcelableModel.readModel(parcel);
		}
	}

	/**
	 * Get wrapped model
	 *
	 * @return model
	 */
	@Nullable
	public Model getModel()
	{
		return this.model;
	}

	@SuppressWarnings("SameReturnValue")
	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	synchronized public void writeToParcel(@NonNull final Parcel parcel, final int flags)
	{
		if (ParcelableModel.SERIALIZE)
		{
			parcel.writeSerializable(getModel());
		}
		else
		{
			ParcelableModel.writeToParcel(parcel, getModel());
		}
	}

	/**
	 * Creator
	 */
	public static final Parcelable.Creator<ParcelableModel> CREATOR = new Parcelable.Creator<ParcelableModel>()
	{
		@NonNull
		@Override
		public ParcelableModel createFromParcel(@NonNull final Parcel parcel)
		{
			return new ParcelableModel(parcel);
		}

		@NonNull
		@Override
		public ParcelableModel[] newArray(final int size)
		{
			return new ParcelableModel[size];
		}
	};

	// W R I T E H E L P E R S

	// MODEL

	/**
	 * Write model to parcel through serialization
	 *
	 * @param parcel parcel to write to
	 * @param model  model
	 */
	public static void writeSerializableToParcel(@NonNull final Parcel parcel, final Model model)
	{
		parcel.writeSerializable(model);
	}

	/**
	 * Write model to parcel
	 *
	 * @param parcel parcel to write to
	 * @param model  model
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, @Nullable final Model model)
	{
		if (model == null)
		{
			parcel.writeInt(0);
			return;
		}
		parcel.writeInt(1);
		ParcelableModel.writeToParcel(parcel, model.tree);
		ParcelableModel.writeToParcel(parcel, model.settings);
		ParcelableModel.writeToParcel(parcel, model.images);
		Log.d(ParcelableModel.TAG, "parcel write size=" + parcel.dataSize() + " pos=" + parcel.dataPosition());
	}

	/**
	 * Write node to parcel
	 *
	 * @param parcel parcel to write to
	 * @param tree   tree
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, @NonNull final Tree tree)
	{
		ParcelableModel.writeToParcel(parcel, tree.getRoot());
		ParcelableModel.writeEdgesToParcel(parcel, tree.getEdges());
	}

	// NODE

	/**
	 * Write node to parcel
	 *
	 * @param parcel parcel to write to
	 * @param node   node
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, @NonNull final INode node)
	{
		// volatile data:
		// public double getWeight();
		// public double getChildrenWeight();
		// public double getMinWeight();
		// public Location getLocation();

		// id
		ParcelableModel.writeToParcel(parcel, node.getId());

		// parent
		final INode parent = node.getParent();
		ParcelableModel.writeToParcel(parcel, parent == null ? null : parent.getId());

		// functional
		final String label = node.getLabel();
		final String edgeLabel = node.getEdgeLabel();
		final String content = node.getContent();
		final Integer backColor = node.getBackColor();
		final Integer foreColor = node.getForeColor();
		final Integer edgeColor = node.getEdgeColor();
		final Integer edgeStyle = node.getEdgeStyle();
		final String link = node.getLink();
		final String target = node.getTarget();
		final String imageFile = node.getImageFile();
		final int imageIndex = node.getImageIndex();
		final String edgeImageFile = node.getEdgeImageFile();
		final int edgeImageIndex = node.getEdgeImageIndex();
		final MountPoint mountPoint = node.getMountPoint();

		ParcelableModel.writeToParcel(parcel, label);
		ParcelableModel.writeToParcel(parcel, edgeLabel);
		ParcelableModel.writeToParcel(parcel, content);
		ParcelableModel.writeToParcel(parcel, backColor);
		ParcelableModel.writeToParcel(parcel, foreColor);
		ParcelableModel.writeToParcel(parcel, edgeColor);
		ParcelableModel.writeToParcel(parcel, edgeStyle);
		ParcelableModel.writeToParcel(parcel, link);
		ParcelableModel.writeToParcel(parcel, target);
		ParcelableModel.writeToParcel(parcel, imageFile);
		ParcelableModel.writeToParcel(parcel, Integer.valueOf(imageIndex));
		ParcelableModel.writeToParcel(parcel, edgeImageFile);
		ParcelableModel.writeToParcel(parcel, Integer.valueOf(edgeImageIndex));
		ParcelableModel.writeToParcel(parcel, mountPoint);

		// child recursion
		final List<INode> children = node.getChildren();
		if (children == null)
		{
			parcel.writeInt(-1);
		}
		else
		{
			final int n = children.size();
			parcel.writeInt(n);
			for (int i = 0; i < n; i++)
			{
				ParcelableModel.writeToParcel(parcel, children.get(i));
			}
		}
	}

	/**
	 * Write mount point to parcel
	 *
	 * @param parcel      parcel to write to
	 * @param mountPoint0 mount point
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, @Nullable final MountPoint mountPoint0)
	{
		if (!(mountPoint0 instanceof MountPoint.Mounting))
		{
			parcel.writeString("");
			return;
		}
		final MountPoint.Mounting mountPoint = (MountPoint.Mounting) mountPoint0;
		parcel.writeString(mountPoint.url);
		ParcelableModel.writeToParcel(parcel, mountPoint.now);
	}

	/**
	 * Write edge list to parcel
	 *
	 * @param parcel parcel to write to
	 * @param edges  edge list
	 */
	private static void writeEdgesToParcel(@NonNull final Parcel parcel, @Nullable final List<IEdge> edges)
	{
		if (edges == null)
		{
			parcel.writeInt(-1);
			return;
		}
		final int n = edges.size();
		parcel.writeInt(n);
		for (int i = 0; i < n; i++)
		{
			ParcelableModel.writeToParcel(parcel, edges.get(i));
		}
	}

	// EDGE

	/**
	 * Write edge to parcel
	 *
	 * @param parcel parcel
	 * @param edge   edge
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, @NonNull final IEdge edge)
	{
		// structural
		final INode from = edge.getFrom();
		final INode to = edge.getTo();
		ParcelableModel.writeToParcel(parcel, from.getId());
		ParcelableModel.writeToParcel(parcel, to.getId());

		// functional
		final String label = edge.getLabel();
		final Integer color = edge.getColor();
		final Integer style = edge.getStyle();
		final String imageFile = edge.getImageFile();
		final int imageIndex = edge.getImageIndex();

		ParcelableModel.writeToParcel(parcel, label);
		ParcelableModel.writeToParcel(parcel, color);
		ParcelableModel.writeToParcel(parcel, style);
		ParcelableModel.writeToParcel(parcel, imageFile);
		ParcelableModel.writeToParcel(parcel, Integer.valueOf(imageIndex));
	}

	// SETTINGS

	/**
	 * Write settings to parcel
	 *
	 * @param parcel   parcel to write to
	 * @param settings settings
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, @NonNull final Settings settings)
	{
		ParcelableModel.writeToParcel(parcel, settings.backColor);
		ParcelableModel.writeToParcel(parcel, settings.foreColor);
		ParcelableModel.writeToParcel(parcel, settings.backgroundImageFile);
		ParcelableModel.writeToParcel(parcel, settings.fontFace);
		ParcelableModel.writeToParcel(parcel, settings.fontSize);
		ParcelableModel.writeToParcel(parcel, settings.downscaleFontsFlag);
		ParcelableModel.writeToParcel(parcel, settings.fontDownscaler);
		ParcelableModel.writeToParcel(parcel, settings.downscaleImagesFlag);
		ParcelableModel.writeToParcel(parcel, settings.imageDownscaler);
		ParcelableModel.writeToParcel(parcel, settings.orientation);
		ParcelableModel.writeToParcel(parcel, settings.expansion);
		ParcelableModel.writeToParcel(parcel, settings.sweep);
		ParcelableModel.writeToParcel(parcel, settings.preserveOrientationFlag);
		ParcelableModel.writeToParcel(parcel, settings.edgesAsArcsFlag);
		ParcelableModel.writeToParcel(parcel, settings.borderFlag);
		ParcelableModel.writeToParcel(parcel, settings.ellipsizeFlag);
		ParcelableModel.writeToParcel(parcel, settings.hasToolbarFlag);
		ParcelableModel.writeToParcel(parcel, settings.hasStatusbarFlag);
		ParcelableModel.writeToParcel(parcel, settings.hasPopUpMenuFlag);
		ParcelableModel.writeToParcel(parcel, settings.hasToolTipFlag);
		ParcelableModel.writeToParcel(parcel, settings.toolTipDisplaysContentFlag);
		ParcelableModel.writeToParcel(parcel, settings.focusOnHoverFlag);
		ParcelableModel.writeToParcel(parcel, settings.focus);
		ParcelableModel.writeToParcel(parcel, settings.xMoveTo);
		ParcelableModel.writeToParcel(parcel, settings.yMoveTo);
		ParcelableModel.writeToParcel(parcel, settings.xShift);
		ParcelableModel.writeToParcel(parcel, settings.yShift);
		ParcelableModel.writeToParcel(parcel, settings.nodeBackColor);
		ParcelableModel.writeToParcel(parcel, settings.nodeForeColor);
		ParcelableModel.writeToParcel(parcel, settings.defaultNodeImage);
		ParcelableModel.writeToParcel(parcel, settings.treeEdgeColor);
		ParcelableModel.writeToParcel(parcel, settings.treeEdgeStyle);
		ParcelableModel.writeToParcel(parcel, settings.defaultTreeEdgeImage);
		ParcelableModel.writeToParcel(parcel, settings.edgeColor);
		ParcelableModel.writeToParcel(parcel, settings.edgeStyle);
		ParcelableModel.writeToParcel(parcel, settings.defaultEdgeImage);
		ParcelableModel.writeMenuToParcel(parcel, settings.menu);
	}

	/**
	 * Write menu to parcel
	 *
	 * @param parcel    parcel to write to
	 * @param menuItems menu items
	 */
	private static void writeMenuToParcel(@NonNull final Parcel parcel, @Nullable final List<MenuItem> menuItems)
	{
		if (menuItems == null)
		{
			parcel.writeInt(-1);
			return;
		}
		final int n = menuItems.size();
		parcel.writeInt(n);
		for (int i = 0; i < n; i++)
		{
			ParcelableModel.writeToParcel(parcel, menuItems.get(i), 0);
		}
	}

	/**
	 * Write menu item to parcel
	 *
	 * @param parcel   parcel to write to
	 * @param menuItem menu item
	 * @param flags    flags
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, @NonNull final MenuItem menuItem, @SuppressWarnings("SameParameterValue") final int flags)
	{
		ParcelableModel.writeToParcel(parcel, menuItem.action);
		ParcelableModel.writeToParcel(parcel, menuItem.label);
		ParcelableModel.writeToParcel(parcel, menuItem.link);
		ParcelableModel.writeToParcel(parcel, menuItem.target);
		ParcelableModel.writeToParcel(parcel, menuItem.matchTarget);
		ParcelableModel.writeToParcel(parcel, menuItem.matchScope);
		ParcelableModel.writeToParcel(parcel, menuItem.matchMode);
	}

	// IMAGES

	private static void writeToParcel(@NonNull final Parcel parcel, @Nullable final treebolic.glue.iface.Image[] images)
	{
		if (images == null)
		{
			parcel.writeInt(-1);
			return;
		}
		final int n = images.length;
		parcel.writeInt(n);
		for (treebolic.glue.iface.Image image : images)
		{
			ParcelableModel.writeToParcel(parcel, image);
		}
	}

	// SPECIFIC

	/**
	 * Write string to parcel
	 *
	 * @param parcel parcel to write to
	 * @param e      enum value
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, @Nullable final Enum<?> e)
	{
		if (e == null)
		{
			parcel.writeInt(-1);
			return;
		}
		parcel.writeInt(e.ordinal());
	}

	/**
	 * Write string to parcel
	 *
	 * @param parcel parcel to write to
	 * @param s      string
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, @Nullable final String s)
	{
		if (s == null)
		{
			parcel.writeString("");
			return;
		}
		parcel.writeString(s);
	}

	/**
	 * Write integer to parcel
	 *
	 * @param parcel parcel to write to
	 * @param n      integer
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, @Nullable final Integer n)
	{
		if (n == null)
		{
			parcel.writeInt(0);
			return;
		}
		parcel.writeInt(1);
		parcel.writeInt(n);
	}

	/**
	 * Write boolean to parcel
	 *
	 * @param parcel parcel to write to
	 * @param b      boolean
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, @Nullable final Boolean b)
	{
		if (b == null)
		{
			parcel.writeInt(-1);
			return;
		}
		parcel.writeInt(b ? 1 : 0);
	}

	/**
	 * Write float to parcel
	 *
	 * @param parcel parcel to write to
	 * @param f      float
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, @Nullable final Float f)
	{
		if (f == null)
		{
			parcel.writeInt(0);
			return;
		}
		parcel.writeInt(1);
		parcel.writeFloat(f);
	}

	// @formatter:off
	/*
	 * Write double to parcel
	 *
	 * @param parcel parcel to write to
	 * @param d      double
	 */
	/*
	@SuppressWarnings("boxing")
	private static void writeToParcel(final Parcel parcel, final Double d)
	{
		if (d == null)
		{
			parcel.writeInt(0);
			return;
		}
		parcel.writeInt(1);
		parcel.writeDouble(d);
	}
	*/
	// @formatter:on

	/**
	 * Write float array to parcel
	 *
	 * @param parcel parcel to write to
	 * @param f      float array
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, final float[] f)
	{
		parcel.writeFloatArray(f);
	}

	/**
	 * Write image to parcel
	 *
	 * @param parcel parcel to write to
	 * @param image0 image
	 */
	private static void writeToParcel(@NonNull final Parcel parcel, @Nullable final treebolic.glue.iface.Image image0)
	{
		if (image0 == null || !(image0 instanceof treebolic.glue.Image))
		{
			parcel.writeInt(0);
			return;
		}

		treebolic.glue.Image image = (treebolic.glue.Image) image0;
		if (image.bitmap == null)
		{
			parcel.writeInt(0);
			return;
		}

		switch (ParcelableModel.IMAGEMETHOD)
		{
			case IMAGE_SERIALIZE:
				parcel.writeInt(1);
				parcel.writeSerializable(image);
				break;
			case IMAGE_ASBYTEARRAY:
				final byte[] imageByteArray = image.getByteArray();
				parcel.writeInt(1);
				parcel.writeByteArray(imageByteArray);
				break;
			case IMAGE_PARCEL:
			default:
				parcel.writeInt(1);
				parcel.writeParcelable(image.bitmap, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
				break;
		}
	}

	// R E A D H E L P E R S

	// MODEL

	/**
	 * Id to Node map
	 */
	static private final Map<String, MutableNode> id2node = new HashMap<>();

	/**
	 * Read model from parcel (through serialization)
	 *
	 * @param parcel parcel to read from
	 * @return model
	 */
	@Nullable
	public static Model readSerializableModel(@NonNull final Parcel parcel)
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? //
				parcel.readSerializable(null, Model.class) : //
				(Model) parcel.readSerializable();
	}

	/**
	 * Read model from parcel
	 *
	 * @param parcel parcel to read from
	 * @return model
	 */
	@Nullable
	private static Model readModel(@NonNull final Parcel parcel)
	{
		Log.d(ParcelableModel.TAG, "parcel read size=" + parcel.dataSize() + " pos=" + parcel.dataPosition());
		final int isNotNull = parcel.readInt();
		if (isNotNull != 0)
		{
			final Tree tree = ParcelableModel.readTree(parcel);
			final Settings settings = ParcelableModel.readSettings(parcel);
			final Image[] images = ParcelableModel.readImages(parcel);
			return new Model(tree, settings, images);
		}
		return null;
	}

	/**
	 * Read tree from parcel
	 *
	 * @param parcel parcel to read from
	 * @return tree
	 */
	@NonNull
	private static Tree readTree(@NonNull final Parcel parcel)
	{
		ParcelableModel.id2node.clear();
		final INode root = ParcelableModel.readNode(parcel);
		final List<IEdge> edges = ParcelableModel.readEdges(parcel);
		return new Tree(root, edges);
	}

	// NODE

	/**
	 * Read node from parcel
	 *
	 * @param parcel parcel to read from
	 * @return node
	 */
	@NonNull
	private static INode readNode(@NonNull final Parcel parcel)
	{
		// structural
		final String id = ParcelableModel.readString(parcel);
		assert id != null;
		@Nullable final String parentId = ParcelableModel.readString(parcel);
		@Nullable final INode parent = parentId == null ? null : ParcelableModel.id2node.get(parentId);
		final MutableNode node = new MutableNode(parent, id);
		ParcelableModel.id2node.put(id, node);

		// functional
		final String label = ParcelableModel.readString(parcel);
		final String edgeLabel = ParcelableModel.readString(parcel);
		final String content = ParcelableModel.readString(parcel);
		final Integer backColor = ParcelableModel.readInteger(parcel);
		final Integer foreColor = ParcelableModel.readInteger(parcel);
		final Integer edgeColor = ParcelableModel.readInteger(parcel);
		final Integer edgeStyle = ParcelableModel.readInteger(parcel);
		final String link = ParcelableModel.readString(parcel);
		final String target = ParcelableModel.readString(parcel);
		final String imageFile = ParcelableModel.readString(parcel);
		final Integer imageIndex = ParcelableModel.readInteger(parcel);
		final String edgeImageFile = ParcelableModel.readString(parcel);
		final Integer edgeImageIndex = ParcelableModel.readInteger(parcel);
		final MountPoint mountPoint = ParcelableModel.readMountPoint(parcel);

		if (label != null)
		{
			node.setLabel(label);
		}
		if (edgeLabel != null)
		{
			node.setEdgeLabel(edgeLabel);
		}
		if (content != null)
		{
			node.setContent(content);
		}
		if (backColor != null)
		{
			node.setBackColor(backColor);
		}
		if (foreColor != null)
		{
			node.setForeColor(foreColor);
		}
		if (edgeColor != null)
		{
			node.setEdgeColor(edgeColor);
		}
		if (edgeStyle != null)
		{
			node.setEdgeStyle(edgeStyle);
		}
		if (link != null)
		{
			node.setLink(link);
		}
		if (target != null)
		{
			node.setTarget(target);
		}
		if (imageFile != null)
		{
			node.setImageFile(imageFile);
		}
		if (imageIndex != null && imageIndex != -1)
		{
			node.setImageIndex(imageIndex);
		}
		if (edgeImageFile != null)
		{
			node.setEdgeImageFile(edgeImageFile);
		}
		if (edgeImageIndex != null && edgeImageIndex != -1)
		{
			node.setEdgeImageIndex(edgeImageIndex);
		}
		if (mountPoint != null)
		{
			node.setMountPoint(mountPoint);
		}

		// child recursion
		final int n = parcel.readInt();
		if (n != -1)
		{
			final List<INode> children = node.getChildren();
			//if (children != null)
			//{
			for (int i = 0; i < n; i++)
			{
				/*final INode child =*/
				ParcelableModel.readNode(parcel);
				// added to parent by Constructor
				// children.add(child);
			}
			if (children.size() != n)
			{
				//Log.e(TAG, "inconsistent child sizes expected=" + n + " real=" + children.size());
				throw new IllegalArgumentException("inconsistent child sizes expected=" + n + " real=" + children.size());
			}
			//}
		}
		return node;
	}

	/**
	 * Read mount point from parcel
	 *
	 * @param parcel parcel to read from
	 * @return mount point
	 */
	@Nullable
	private static MountPoint readMountPoint(@NonNull final Parcel parcel)
	{
		final String url = parcel.readString();
		if (url != null && !url.isEmpty())
		{
			final MountPoint.Mounting mountPoint = new MountPoint.Mounting();
			mountPoint.url = url;
			mountPoint.now = ParcelableModel.readBoolean(parcel);
			return mountPoint;
		}
		return null;
	}

	/**
	 * Read edge list from parcel
	 *
	 * @param parcel parcel to read from
	 * @return edge list
	 */
	@Nullable
	private static List<IEdge> readEdges(@NonNull final Parcel parcel)
	{
		final int n = parcel.readInt();
		if (n == -1)
		{
			return null;
		}
		final List<IEdge> edges = new ArrayList<>();
		for (int i = 0; i < n; i++)
		{
			final IEdge edge = ParcelableModel.readEdge(parcel);
			edges.add(edge);
		}
		return edges;
	}

	// EDGE

	/**
	 * Read edge from parcel
	 *
	 * @param parcel parcel to read from
	 * @return edge
	 */
	@NonNull
	private static IEdge readEdge(@NonNull final Parcel parcel)
	{
		// structural
		final String fromId = ParcelableModel.readString(parcel);
		final INode from = ParcelableModel.id2node.get(fromId);
		final String toId = ParcelableModel.readString(parcel);
		final INode to = ParcelableModel.id2node.get(toId);
		final MutableEdge edge = new MutableEdge(from, to);

		// functional
		final String label = ParcelableModel.readString(parcel);
		final Integer color = ParcelableModel.readInteger(parcel);
		final Integer style = ParcelableModel.readInteger(parcel);
		final String imageFile = ParcelableModel.readString(parcel);
		final Integer imageIndex = ParcelableModel.readInteger(parcel);

		if (label != null && !label.isEmpty())
		{
			edge.setLabel(label);
		}
		if (color != null)
		{
			edge.setColor(color);
		}
		if (style != null)
		{
			edge.setStyle(style);
		}
		if (imageFile != null && !imageFile.isEmpty())
		{
			edge.setImageFile(imageFile);
		}
		if (imageIndex != null && imageIndex != -1)
		{
			edge.setImageIndex(imageIndex);
		}
		return edge;
	}

	// SETTINGS

	/**
	 * Read settings from parcel
	 *
	 * @param parcel parcel to read from
	 * @return settings
	 */
	@NonNull
	private static Settings readSettings(@NonNull final Parcel parcel)
	{
		final Settings settings = new Settings();
		settings.backColor = ParcelableModel.readInteger(parcel);
		settings.foreColor = ParcelableModel.readInteger(parcel);
		settings.backgroundImageFile = ParcelableModel.readString(parcel);
		settings.fontFace = ParcelableModel.readString(parcel);
		settings.fontSize = ParcelableModel.readInteger(parcel);
		settings.downscaleFontsFlag = ParcelableModel.readBoolean(parcel);
		settings.fontDownscaler = ParcelableModel.readFloats(parcel);
		settings.downscaleImagesFlag = ParcelableModel.readBoolean(parcel);
		settings.imageDownscaler = ParcelableModel.readFloats(parcel);
		settings.orientation = ParcelableModel.readString(parcel);
		settings.expansion = ParcelableModel.readFloat(parcel);
		settings.sweep = ParcelableModel.readFloat(parcel);
		settings.preserveOrientationFlag = ParcelableModel.readBoolean(parcel);
		settings.edgesAsArcsFlag = ParcelableModel.readBoolean(parcel);
		settings.borderFlag = ParcelableModel.readBoolean(parcel);
		settings.ellipsizeFlag = ParcelableModel.readBoolean(parcel);
		settings.hasToolbarFlag = ParcelableModel.readBoolean(parcel);
		settings.hasStatusbarFlag = ParcelableModel.readBoolean(parcel);
		settings.hasPopUpMenuFlag = ParcelableModel.readBoolean(parcel);
		settings.hasToolTipFlag = ParcelableModel.readBoolean(parcel);
		settings.toolTipDisplaysContentFlag = ParcelableModel.readBoolean(parcel);
		settings.focusOnHoverFlag = ParcelableModel.readBoolean(parcel);
		settings.focus = ParcelableModel.readString(parcel);
		settings.xMoveTo = ParcelableModel.readFloat(parcel);
		settings.yMoveTo = ParcelableModel.readFloat(parcel);
		settings.xShift = ParcelableModel.readFloat(parcel);
		settings.yShift = ParcelableModel.readFloat(parcel);
		settings.nodeBackColor = ParcelableModel.readInteger(parcel);
		settings.nodeForeColor = ParcelableModel.readInteger(parcel);
		settings.defaultNodeImage = ParcelableModel.readString(parcel);
		settings.treeEdgeColor = ParcelableModel.readInteger(parcel);
		settings.treeEdgeStyle = ParcelableModel.readInteger(parcel);
		settings.defaultTreeEdgeImage = ParcelableModel.readString(parcel);
		settings.edgeColor = ParcelableModel.readInteger(parcel);
		settings.edgeStyle = ParcelableModel.readInteger(parcel);
		settings.defaultEdgeImage = ParcelableModel.readString(parcel);
		settings.menu = ParcelableModel.readMenu(parcel);
		return settings;
	}

	/**
	 * Read menu from parcel
	 *
	 * @param parcel parcel to read from
	 * @return menu
	 */
	@Nullable
	private static List<MenuItem> readMenu(@NonNull final Parcel parcel)
	{
		final int n = parcel.readInt();
		if (n == -1)
		{
			return null;
		}
		final List<MenuItem> menu = new ArrayList<>();
		for (int i = 0; i < n; i++)
		{
			final MenuItem menuItem = ParcelableModel.readMenuItem(parcel);
			menu.add(menuItem);
		}
		return menu;
	}

	/**
	 * Read menu item from parcel
	 *
	 * @param parcel parcel to read from
	 * @return menu item
	 */
	@NonNull
	private static MenuItem readMenuItem(@NonNull final Parcel parcel)
	{
		final MenuItem menuItem = new MenuItem();
		int ordinal;
		// action
		ordinal = parcel.readInt();
		menuItem.action = ordinal == -1 ? null : MenuItem.Action.values()[ordinal];
		// label
		menuItem.label = ParcelableModel.readString(parcel);
		// link
		menuItem.link = ParcelableModel.readString(parcel);
		// target
		menuItem.target = ParcelableModel.readString(parcel);
		// match target
		menuItem.matchTarget = ParcelableModel.readString(parcel);
		// match mode
		ordinal = parcel.readInt();
		menuItem.matchScope = ordinal == -1 ? null : MatchScope.values()[ordinal];
		// match scope
		ordinal = parcel.readInt();
		menuItem.matchMode = ordinal == -1 ? null : MatchMode.values()[ordinal];
		return menuItem;
	}

	// IMAGES

	@Nullable
	private static Image[] readImages(@NonNull final Parcel parcel)
	{
		final int n = parcel.readInt();
		if (n == -1)
		{
			return null;
		}
		final Image[] images = new Image[n];
		for (int i = 0; i < n; i++)
		{
			images[i] = ParcelableModel.readImage(parcel);
		}
		return images;
	}

	// SPECIFIC

	/**
	 * Read string from parcel
	 *
	 * @param parcel parcel to read from
	 * @return string
	 */
	@Nullable
	private static String readString(@NonNull final Parcel parcel)
	{
		final String s = parcel.readString();
		if (s != null && !s.isEmpty())
		{
			return s;
		}
		return null;
	}

	/**
	 * Read boolean from parcel
	 *
	 * @param parcel parcel to read from
	 * @return boolean
	 */
	@Nullable
	@SuppressWarnings("boxing")
	private static Boolean readBoolean(@NonNull final Parcel parcel)
	{
		final int value = parcel.readInt();
		switch (value)
		{
			case 1:
				return true;
			case 0:
				return false;
			default:
				break;
		}
		return null;
	}

	/**
	 * Read integer from parcel
	 *
	 * @param parcel parcel to read from
	 * @return integer
	 */
	@Nullable
	private static Integer readInteger(@NonNull final Parcel parcel)
	{
		final int isNotNull = parcel.readInt();
		if (isNotNull != 0)
		{
			return parcel.readInt();
		}
		return null;
	}

	// /**
	// * Read double from parcel
	// *
	// * @param parcel
	// * parcel to read from
	// * @return double
	// */
	// private static Double readDouble(final Parcel parcel)
	// {
	// final int isNotNull = parcel.readInt();
	// if (isNotNull != 0)
	// {
	// final double d = parcel.readDouble();
	// return Double.valueOf(d);
	// }
	// return null;
	// }

	/**
	 * Read double from parcel
	 *
	 * @param parcel parcel to read from
	 * @return double
	 */
	@Nullable
	private static Float readFloat(@NonNull final Parcel parcel)
	{
		final int isNotNull = parcel.readInt();
		if (isNotNull != 0)
		{
			return parcel.readFloat();
		}
		return null;
	}

	/**
	 * Read floats from parcel
	 *
	 * @param parcel parcel to read from
	 * @return array of floats
	 */
	@Nullable
	private static float[] readFloats(@NonNull final Parcel parcel)
	{
		return parcel.createFloatArray();
	}

	/**
	 * Read image from parcel
	 *
	 * @param parcel parcel to read from
	 * @return image
	 */
	@Nullable
	private static Image readImage(@NonNull final Parcel parcel)
	{
		final int isNotNull = parcel.readInt();
		if (isNotNull != 0)
		{
			switch (ParcelableModel.IMAGEMETHOD)
			{
				case IMAGE_SERIALIZE:
					return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? //
							parcel.readSerializable(null, Image.class) : //
							(Image) parcel.readSerializable();

				case IMAGE_ASBYTEARRAY:
					final byte[] imageByteArray = parcel.createByteArray();
					if (imageByteArray != null)
					{
						try
						{
							final Image image = new Image((Bitmap) null);
							image.setFromByteArray(imageByteArray);
							return image;
						}
						catch (@NonNull final Exception ignored)
						{
							//
						}
					}
					break;

				case IMAGE_PARCEL:
				default:
					final Bitmap bitmap = Bitmap.CREATOR.createFromParcel(parcel);
					return new Image(bitmap);
			}

		}
		return null;
	}

	// T E S T

	/*
	public static boolean parcelTest(final Bundle bundle, final String key)
	{
		// model1
		final ParcelableModel model1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? //
				bundle.getParcelable(key, ParcelableModel.class) : //
				bundle.getParcelable(key);

		// write bundle to parcel
		final Parcel parcel = Parcel.obtain();
		bundle.writeToParcel(parcel, 0);

		// extract bundle from parcel
		parcel.setDataPosition(0);
		final Bundle bundle2 = parcel.readBundle(ParcelableModel.class.getClassLoader());
		bundle2.setClassLoader(Model.class.getClassLoader());

		// model2
		final ParcelableModel model2 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? //
				bundle2.getParcelable(key, ParcelableModel.class) : //
				bundle2.getParcelable(key);

		parcel.recycle();

		if (model1 != null && model2 != null)
		{
			return model1.getModel().equals(model2.getModel());
		}
		return model1 == model2;
	}
	*/
}
