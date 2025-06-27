/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic

import android.graphics.Bitmap
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.Log
import treebolic.glue.iface.Image
import treebolic.model.IEdge
import treebolic.model.INode
import treebolic.model.MenuItem
import treebolic.model.Model
import treebolic.model.MountPoint
import treebolic.model.MountPoint.Mounting
import treebolic.model.MutableEdge
import treebolic.model.MutableNode
import treebolic.model.Settings
import treebolic.model.Tree
import treebolic.model.Types.MatchMode
import treebolic.model.Types.MatchScope

/**
 * Convenience class to parcel model
 */
class ParcelableModel : Parcelable {

    /**
     * Image marshaling types
     */
    private enum class ImageMarshaling {

        IMAGE_SERIALIZE, IMAGE_ASBYTEARRAY, IMAGE_PARCEL
    }

    /**
     * Wrapped model
     */
    @JvmField
    val model: Model?

    // C O N S T R U C T O R

    /**
     * Null constructor
     */
    constructor() {
        model = null
    }

    /**
     * Constructor
     */
    constructor(newModel: Model?) {
        model = newModel
    }

    /**
     * Constructor from parcel
     *
     * @param parcel parcel to build from
     */
    constructor(parcel: Parcel) {
        if (SERIALIZE) {
            @Suppress("DEPRECATION")
            model = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 
                parcel.readSerializable(null, Model::class.java) else  
                parcel.readSerializable() as Model?
        } else {
            model = readModel(parcel)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    @Synchronized
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        if (SERIALIZE) {
            parcel.writeSerializable(model)
        } else {
            writeToParcel(parcel, model)
        }
    }

    companion object CREATOR : Creator<ParcelableModel> {

        override fun createFromParcel(parcel: Parcel): ParcelableModel {
            return ParcelableModel(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableModel?> {
            return arrayOfNulls(size)
        }

        private const val TAG = "ModelParcel"

        /**
         * Whether model is marshaled with standard Java mechanism or android parcelization
         */
        const val SERIALIZE: Boolean = false

        /**
         * Image marshaling
         */
        private val IMAGEMETHOD = ImageMarshaling.IMAGE_ASBYTEARRAY

        // W R I T E H E L P E R S

        // MODEL

        /**
         * Write model to parcel through serialization
         *
         * @param parcel parcel to write to
         * @param model  model
         */
        fun writeSerializableToParcel(parcel: Parcel, model: Model?) {
            parcel.writeSerializable(model)
        }

        /**
         * Write model to parcel
         *
         * @param parcel parcel to write to
         * @param model  model
         */
        private fun writeToParcel(parcel: Parcel, model: Model?) {
            if (model == null) {
                parcel.writeInt(0)
                return
            }
            parcel.writeInt(1)
            writeToParcel(parcel, model.tree)
            writeToParcel(parcel, model.settings)
            writeToParcel(parcel, model.images)
            Log.d(TAG, "Parcel write size=" + parcel.dataSize() + " pos=" + parcel.dataPosition())
        }

        /**
         * Write node to parcel
         *
         * @param parcel parcel to write to
         * @param tree   tree
         */
        private fun writeToParcel(parcel: Parcel, tree: Tree) {
            writeToParcel(parcel, tree.root)
            writeEdgesToParcel(parcel, tree.edges)
        }

        // NODE
        /**
         * Write node to parcel
         *
         * @param parcel parcel to write to
         * @param node   node
         */
        private fun writeToParcel(parcel: Parcel, node: INode) {
            // id
            writeToParcel(parcel, node.id)

            // parent
            val parent = node.parent
            writeToParcel(parcel, parent?.id)

            // functional
            val label = node.label
            val edgeLabel = node.edgeLabel
            val content = node.content
            val backColor = node.backColor
            val foreColor = node.foreColor
            val edgeColor = node.edgeColor
            val edgeStyle = node.edgeStyle
            val link = node.link
            val target = node.target
            val imageFile = node.imageFile
            val imageIndex = node.imageIndex
            val edgeImageFile = node.edgeImageFile
            val edgeImageIndex = node.edgeImageIndex
            val mountPoint = node.mountPoint

            writeToParcel(parcel, label)
            writeToParcel(parcel, edgeLabel)
            writeToParcel(parcel, content)
            writeToParcel(parcel, backColor)
            writeToParcel(parcel, foreColor)
            writeToParcel(parcel, edgeColor)
            writeToParcel(parcel, edgeStyle)
            writeToParcel(parcel, link)
            writeToParcel(parcel, target)
            writeToParcel(parcel, imageFile)
            writeToParcel(parcel, imageIndex)
            writeToParcel(parcel, edgeImageFile)
            writeToParcel(parcel, edgeImageIndex)
            writeToParcel(parcel, mountPoint)

            // child recursion
            val children = node.children
            if (children == null) {
                parcel.writeInt(-1)
            } else {
                val n = children.size
                parcel.writeInt(n)
                for (i in 0 until n) {
                    writeToParcel(parcel, children[i])
                }
            }
        }

        /**
         * Write mount point to parcel
         *
         * @param parcel      parcel to write to
         * @param mountPoint0 mount point
         */
        private fun writeToParcel(parcel: Parcel, mountPoint0: MountPoint?) {
            if (mountPoint0 !is Mounting) {
                parcel.writeString("")
                return
            }
            parcel.writeString(mountPoint0.url)
            writeToParcel(parcel, mountPoint0.now)
        }

        /**
         * Write edge list to parcel
         *
         * @param parcel parcel to write to
         * @param edges  edge list
         */
        private fun writeEdgesToParcel(parcel: Parcel, edges: List<IEdge>?) {
            if (edges == null) {
                parcel.writeInt(-1)
                return
            }
            val n = edges.size
            parcel.writeInt(n)
            for (i in 0 until n) {
                writeToParcel(parcel, edges[i])
            }
        }

        // EDGE
        /**
         * Write edge to parcel
         *
         * @param parcel parcel
         * @param edge   edge
         */
        private fun writeToParcel(parcel: Parcel, edge: IEdge) {
            // structural
            val from = edge.from
            val to = edge.to
            writeToParcel(parcel, from.id)
            writeToParcel(parcel, to.id)

            // functional
            val label = edge.label
            val color = edge.color
            val style = edge.style
            val imageFile = edge.imageFile
            val imageIndex = edge.imageIndex

            writeToParcel(parcel, label)
            writeToParcel(parcel, color)
            writeToParcel(parcel, style)
            writeToParcel(parcel, imageFile)
            writeToParcel(parcel, imageIndex)
        }

        // SETTINGS
        /**
         * Write settings to parcel
         *
         * @param parcel   parcel to write to
         * @param settings settings
         */
        private fun writeToParcel(parcel: Parcel, settings: Settings) {
            writeToParcel(parcel, settings.backColor)
            writeToParcel(parcel, settings.foreColor)
            writeToParcel(parcel, settings.backgroundImageFile)
            writeToParcel(parcel, settings.fontFace)
            writeToParcel(parcel, settings.fontSize)
            writeToParcel(parcel, settings.downscaleFontsFlag)
            writeToParcel(parcel, settings.fontDownscaler)
            writeToParcel(parcel, settings.downscaleImagesFlag)
            writeToParcel(parcel, settings.imageDownscaler)
            writeToParcel(parcel, settings.orientation)
            writeToParcel(parcel, settings.expansion)
            writeToParcel(parcel, settings.sweep)
            writeToParcel(parcel, settings.preserveOrientationFlag)
            writeToParcel(parcel, settings.edgesAsArcsFlag)
            writeToParcel(parcel, settings.borderFlag)
            writeToParcel(parcel, settings.ellipsizeFlag)
            writeToParcel(parcel, settings.hasToolbarFlag)
            writeToParcel(parcel, settings.hasStatusbarFlag)
            writeToParcel(parcel, settings.hasPopUpMenuFlag)
            writeToParcel(parcel, settings.hasToolTipFlag)
            writeToParcel(parcel, settings.toolTipDisplaysContentFlag)
            writeToParcel(parcel, settings.focusOnHoverFlag)
            writeToParcel(parcel, settings.focus)
            writeToParcel(parcel, settings.xMoveTo)
            writeToParcel(parcel, settings.yMoveTo)
            writeToParcel(parcel, settings.xShift)
            writeToParcel(parcel, settings.yShift)
            writeToParcel(parcel, settings.nodeBackColor)
            writeToParcel(parcel, settings.nodeForeColor)
            writeToParcel(parcel, settings.defaultNodeImage)
            writeToParcel(parcel, settings.treeEdgeColor)
            writeToParcel(parcel, settings.treeEdgeStyle)
            writeToParcel(parcel, settings.defaultTreeEdgeImage)
            writeToParcel(parcel, settings.edgeColor)
            writeToParcel(parcel, settings.edgeStyle)
            writeToParcel(parcel, settings.defaultEdgeImage)
            writeMenuToParcel(parcel, settings.menu)
        }

        /**
         * Write menu to parcel
         *
         * @param parcel    parcel to write to
         * @param menuItems menu items
         */
        private fun writeMenuToParcel(parcel: Parcel, menuItems: List<MenuItem>?) {
            if (menuItems == null) {
                parcel.writeInt(-1)
                return
            }
            val n = menuItems.size
            parcel.writeInt(n)
            for (i in 0 until n) {
                writeToParcel(parcel, menuItems[i])
            }
        }

        /**
         * Write menu item to parcel
         *
         * @param parcel   parcel to write to
         * @param menuItem menu item
         */
        private fun writeToParcel(parcel: Parcel, menuItem: MenuItem) {
            writeToParcel(parcel, menuItem.action)
            writeToParcel(parcel, menuItem.label)
            writeToParcel(parcel, menuItem.link)
            writeToParcel(parcel, menuItem.target)
            writeToParcel(parcel, menuItem.matchTarget)
            writeToParcel(parcel, menuItem.matchScope)
            writeToParcel(parcel, menuItem.matchMode)
        }

        // IMAGES

        private fun writeToParcel(parcel: Parcel, images: Array<Image>?) {
            if (images == null) {
                parcel.writeInt(-1)
                return
            }
            val n = images.size
            parcel.writeInt(n)
            for (image in images) {
                writeToParcel(parcel, image)
            }
        }

        // SPECIFIC

        /**
         * Write string to parcel
         *
         * @param parcel parcel to write to
         * @param e      enum value
         */
        private fun writeToParcel(parcel: Parcel, e: Enum<*>?) {
            if (e == null) {
                parcel.writeInt(-1)
                return
            }
            parcel.writeInt(e.ordinal)
        }

        /**
         * Write string to parcel
         *
         * @param parcel parcel to write to
         * @param s      string
         */
        private fun writeToParcel(parcel: Parcel, s: String?) {
            if (s == null) {
                parcel.writeString("")
                return
            }
            parcel.writeString(s)
        }

        /**
         * Write integer to parcel
         *
         * @param parcel parcel to write to
         * @param n      integer
         */
        private fun writeToParcel(parcel: Parcel, n: Int?) {
            if (n == null) {
                parcel.writeInt(0)
                return
            }
            parcel.writeInt(1)
            parcel.writeInt(n)
        }

        /**
         * Write boolean to parcel
         *
         * @param parcel parcel to write to
         * @param b      boolean
         */
        private fun writeToParcel(parcel: Parcel, b: Boolean?) {
            if (b == null) {
                parcel.writeInt(-1)
                return
            }
            parcel.writeInt(if (b) 1 else 0)
        }

        /**
         * Write float to parcel
         *
         * @param parcel parcel to write to
         * @param f      float
         */
        private fun writeToParcel(parcel: Parcel, f: Float?) {
            if (f == null) {
                parcel.writeInt(0)
                return
            }
            parcel.writeInt(1)
            parcel.writeFloat(f)
        }

        /**
         * Write float array to parcel
         *
         * @param parcel parcel to write to
         * @param f      float array
         */
        private fun writeToParcel(parcel: Parcel, f: FloatArray?) {
            parcel.writeFloatArray(f)
        }

        /**
         * Write image to parcel
         *
         * @param parcel parcel to write to
         * @param image0 image
         */
        private fun writeToParcel(parcel: Parcel, image0: Image?) {
            if (image0 !is treebolic.glue.Image) {
                parcel.writeInt(0)
                return
            }

            if (image0.bitmap == null) {
                parcel.writeInt(0)
                return
            }

            when (IMAGEMETHOD) {
                ImageMarshaling.IMAGE_SERIALIZE -> {
                    parcel.writeInt(1)
                    parcel.writeSerializable(image0)
                }

                ImageMarshaling.IMAGE_ASBYTEARRAY -> {
                    val imageByteArray = image0.byteArray
                    parcel.writeInt(1)
                    parcel.writeByteArray(imageByteArray)
                }

                ImageMarshaling.IMAGE_PARCEL -> {
                    parcel.writeInt(1)
                    parcel.writeParcelable(image0.bitmap, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
                }
            }
        }

        // R E A D H E L P E R S

        // MODEL
        /**
         * Id to Node map
         */
        private val id2node: MutableMap<String?, MutableNode> = HashMap()

        /**
         * Read model from parcel (through serialization)
         *
         * @param parcel parcel to read from
         * @return model
         */
        fun readSerializableModel(parcel: Parcel): Model? {
            @Suppress("DEPRECATION")
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 
                parcel.readSerializable(null, Model::class.java) else  
                parcel.readSerializable() as Model?
        }

        /**
         * Read model from parcel
         *
         * @param parcel parcel to read from
         * @return model
         */
        private fun readModel(parcel: Parcel): Model? {
            Log.d(TAG, "Parcel read size=" + parcel.dataSize() + " pos=" + parcel.dataPosition())
            val isNotNull = parcel.readInt()
            if (isNotNull != 0) {
                val tree = readTree(parcel)
                val settings = readSettings(parcel)
                val images = readImages(parcel)
                return Model(tree, settings, images)
            }
            return null
        }

        /**
         * Read tree from parcel
         *
         * @param parcel parcel to read from
         * @return tree
         */
        private fun readTree(parcel: Parcel): Tree {
            id2node.clear()
            val root = readNode(parcel)
            val edges = readEdges(parcel)
            return Tree(root, edges)
        }

        // NODE

        /**
         * Read node from parcel
         *
         * @param parcel parcel to read from
         * @return node
         */
        private fun readNode(parcel: Parcel): INode {
            // structural
            val id = readString(parcel)!!
            val parentId = readString(parcel)
            val parent: INode? = if (parentId == null) null else id2node[parentId]
            val node = MutableNode(parent, id)
            id2node[id] = node

            // functional
            val label = readString(parcel)
            val edgeLabel = readString(parcel)
            val content = readString(parcel)
            val backColor = readInteger(parcel)
            val foreColor = readInteger(parcel)
            val edgeColor = readInteger(parcel)
            val edgeStyle = readInteger(parcel)
            val link = readString(parcel)
            val target = readString(parcel)
            val imageFile = readString(parcel)
            val imageIndex = readInteger(parcel)
            val edgeImageFile = readString(parcel)
            val edgeImageIndex = readInteger(parcel)
            val mountPoint = readMountPoint(parcel)

            if (label != null) {
                node.label = label
            }
            if (edgeLabel != null) {
                node.edgeLabel = edgeLabel
            }
            if (content != null) {
                node.content = content
            }
            if (backColor != null) {
                node.backColor = backColor
            }
            if (foreColor != null) {
                node.foreColor = foreColor
            }
            if (edgeColor != null) {
                node.edgeColor = edgeColor
            }
            if (edgeStyle != null) {
                node.edgeStyle = edgeStyle
            }
            if (link != null) {
                node.link = link
            }
            if (target != null) {
                node.target = target
            }
            if (imageFile != null) {
                node.imageFile = imageFile
            }
            if (imageIndex != null && imageIndex != -1) {
                node.imageIndex = imageIndex
            }
            if (edgeImageFile != null) {
                node.edgeImageFile = edgeImageFile
            }
            if (edgeImageIndex != null && edgeImageIndex != -1) {
                node.edgeImageIndex = edgeImageIndex
            }
            if (mountPoint != null) {
                node.mountPoint = mountPoint
            }

            // child recursion
            val n = parcel.readInt()
            if (n != -1) {
                val children = node.children
                for (i in 0 until n) {
                    readNode(parcel)
                    // added to parent by Constructor
                }
                require(children.size == n) {
                    //Log.e(TAG, "inconsistent child sizes expected=" + n + " real=" + children.size())
                    "inconsistent child sizes expected=" + n + " real=" + children.size
                }
            }
            return node
        }

        /**
         * Read mount point from parcel
         *
         * @param parcel parcel to read from
         * @return mount point
         */
        private fun readMountPoint(parcel: Parcel): MountPoint? {
            val url = parcel.readString()
            if (!url.isNullOrEmpty()) {
                val mountPoint = Mounting()
                mountPoint.url = url
                mountPoint.now = readBoolean(parcel)
                return mountPoint
            }
            return null
        }

        /**
         * Read edge list from parcel
         *
         * @param parcel parcel to read from
         * @return edge list
         */
        private fun readEdges(parcel: Parcel): List<IEdge>? {
            val n = parcel.readInt()
            if (n == -1) {
                return null
            }
            val edges: MutableList<IEdge> = ArrayList()
            for (i in 0 until n) {
                val edge = readEdge(parcel)
                edges.add(edge)
            }
            return edges
        }

        // EDGE
        /**
         * Read edge from parcel
         *
         * @param parcel parcel to read from
         * @return edge
         */
        private fun readEdge(parcel: Parcel): IEdge {
            // structural
            val fromId = readString(parcel)
            val from: INode? = id2node[fromId]
            val toId = readString(parcel)
            val to: INode? = id2node[toId]
            val edge = MutableEdge(from, to)

            // functional
            val label = readString(parcel)
            val color = readInteger(parcel)
            val style = readInteger(parcel)
            val imageFile = readString(parcel)
            val imageIndex = readInteger(parcel)

            if (!label.isNullOrEmpty()) {
                edge.label = label
            }
            if (color != null) {
                edge.color = color
            }
            if (style != null) {
                edge.style = style
            }
            if (!imageFile.isNullOrEmpty()) {
                edge.imageFile = imageFile
            }
            if (imageIndex != null && imageIndex != -1) {
                edge.imageIndex = imageIndex
            }
            return edge
        }

        // SETTINGS
        /**
         * Read settings from parcel
         *
         * @param parcel parcel to read from
         * @return settings
         */
        private fun readSettings(parcel: Parcel): Settings {
            val settings = Settings()
            settings.backColor = readInteger(parcel)
            settings.foreColor = readInteger(parcel)
            settings.backgroundImageFile = readString(parcel)
            settings.fontFace = readString(parcel)
            settings.fontSize = readInteger(parcel)
            settings.downscaleFontsFlag = readBoolean(parcel)
            settings.fontDownscaler = readFloats(parcel)
            settings.downscaleImagesFlag = readBoolean(parcel)
            settings.imageDownscaler = readFloats(parcel)
            settings.orientation = readString(parcel)
            settings.expansion = readFloat(parcel)
            settings.sweep = readFloat(parcel)
            settings.preserveOrientationFlag = readBoolean(parcel)
            settings.edgesAsArcsFlag = readBoolean(parcel)
            settings.borderFlag = readBoolean(parcel)
            settings.ellipsizeFlag = readBoolean(parcel)
            settings.hasToolbarFlag = readBoolean(parcel)
            settings.hasStatusbarFlag = readBoolean(parcel)
            settings.hasPopUpMenuFlag = readBoolean(parcel)
            settings.hasToolTipFlag = readBoolean(parcel)
            settings.toolTipDisplaysContentFlag = readBoolean(parcel)
            settings.focusOnHoverFlag = readBoolean(parcel)
            settings.focus = readString(parcel)
            settings.xMoveTo = readFloat(parcel)
            settings.yMoveTo = readFloat(parcel)
            settings.xShift = readFloat(parcel)
            settings.yShift = readFloat(parcel)
            settings.nodeBackColor = readInteger(parcel)
            settings.nodeForeColor = readInteger(parcel)
            settings.defaultNodeImage = readString(parcel)
            settings.treeEdgeColor = readInteger(parcel)
            settings.treeEdgeStyle = readInteger(parcel)
            settings.defaultTreeEdgeImage = readString(parcel)
            settings.edgeColor = readInteger(parcel)
            settings.edgeStyle = readInteger(parcel)
            settings.defaultEdgeImage = readString(parcel)
            settings.menu = readMenu(parcel)
            return settings
        }

        /**
         * Read menu from parcel
         *
         * @param parcel parcel to read from
         * @return menu
         */
        private fun readMenu(parcel: Parcel): List<MenuItem>? {
            val n = parcel.readInt()
            if (n == -1) {
                return null
            }
            val menu: MutableList<MenuItem> = ArrayList()
            for (i in 0 until n) {
                val menuItem = readMenuItem(parcel)
                menu.add(menuItem)
            }
            return menu
        }

        /**
         * Read menu item from parcel
         *
         * @param parcel parcel to read from
         * @return menu item
         */
        @OptIn(ExperimentalStdlibApi::class)
        private fun readMenuItem(parcel: Parcel): MenuItem {
            val menuItem = MenuItem()
            // action
            var ordinal = parcel.readInt()
            menuItem.action = if (ordinal == -1) null else MenuItem.Action.entries.toTypedArray()[ordinal]
            // label
            menuItem.label = readString(parcel)
            // link
            menuItem.link = readString(parcel)
            // target
            menuItem.target = readString(parcel)
            // match target
            menuItem.matchTarget = readString(parcel)
            // match mode
            ordinal = parcel.readInt()
            menuItem.matchScope = if (ordinal == -1) null else MatchScope.entries[ordinal]
            // match scope
            ordinal = parcel.readInt()
            menuItem.matchMode = if (ordinal == -1) null else MatchMode.entries[ordinal]
            return menuItem
        }

        // IMAGES
        private fun readImages(parcel: Parcel): Array<treebolic.glue.Image?>? {
            val n = parcel.readInt()
            if (n == -1) {
                return null
            }
            val images = arrayOfNulls<treebolic.glue.Image>(n)
            for (i in 0 until n) {
                images[i] = readImage(parcel)
            }
            return images
        }

        // SPECIFIC
        /**
         * Read string from parcel
         *
         * @param parcel parcel to read from
         * @return string
         */
        private fun readString(parcel: Parcel): String? {
            val s = parcel.readString()
            if (!s.isNullOrEmpty()) {
                return s
            }
            return null
        }

        /**
         * Read boolean from parcel
         *
         * @param parcel parcel to read from
         * @return boolean
         */
        private fun readBoolean(parcel: Parcel): Boolean? {
            val value = parcel.readInt()
            when (value) {
                1 -> return true
                0 -> return false
                else -> {}
            }
            return null
        }

        /**
         * Read integer from parcel
         *
         * @param parcel parcel to read from
         * @return integer
         */
        private fun readInteger(parcel: Parcel): Int? {
            val isNotNull = parcel.readInt()
            if (isNotNull != 0) {
                return parcel.readInt()
            }
            return null
        }

        /**
         * Read double from parcel
         *
         * @param parcel parcel to read from
         * @return double
         */
        private fun readFloat(parcel: Parcel): Float? {
            val isNotNull = parcel.readInt()
            if (isNotNull != 0) {
                return parcel.readFloat()
            }
            return null
        }

        /**
         * Read floats from parcel
         *
         * @param parcel parcel to read from
         * @return array of floats
         */
        private fun readFloats(parcel: Parcel): FloatArray? {
            return parcel.createFloatArray()
        }

        /**
         * Read image from parcel
         *
         * @param parcel parcel to read from
         * @return image
         */
        private fun readImage(parcel: Parcel): treebolic.glue.Image? {
            val isNotNull = parcel.readInt()
            if (isNotNull != 0) {
                @Suppress("DEPRECATION")
                when (IMAGEMETHOD) {
                    ImageMarshaling.IMAGE_SERIALIZE -> return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        parcel.readSerializable(null, treebolic.glue.Image::class.java) else
                        parcel.readSerializable() as treebolic.glue.Image?

                    ImageMarshaling.IMAGE_ASBYTEARRAY -> {
                        val imageByteArray = parcel.createByteArray()
                        if (imageByteArray != null) {
                            try {
                                val image = treebolic.glue.Image(null as Bitmap?)
                                image.setFromByteArray(imageByteArray)
                                return image
                            } catch (ignored: Exception) {
                                
                            }
                        }
                    }

                    ImageMarshaling.IMAGE_PARCEL -> {
                        val bitmap = Bitmap.CREATOR.createFromParcel(parcel)
                        return treebolic.glue.Image(bitmap)
                    }
                }
            }
            return null
        }
    }
}
