package org.treebolic.parcel

import android.os.Build
import android.os.Bundle
import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.treebolic.ParcelableModel
import treebolic.glue.iface.Colors
import treebolic.model.INode
import treebolic.model.Model
import treebolic.model.ModelDump
import treebolic.model.Settings
import treebolic.model.Tree
import treebolic.model.TreeMutableNode
import java.io.IOException
import java.io.ObjectInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ParcelInstrumentedTest {

    @Test
    fun packageName_isCorrect() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("org.treebolic.parcel.test", appContext.packageName)
    }

    @Test
    fun parcelizationA1_isCorrect() {
        val root = makeDefaultTree()
        val modelA = Model(Tree(root, null), Settings())
        val strA = ModelDump.toString(modelA)

        val bundle = Bundle()
        putParceledModel(modelA, bundle, MODEL_KEYA)

        val parcel = writeParcel(bundle)
        val bundle2 = readParcel(parcel)
        parcel.recycle()

        checkNotNull(bundle2)
        val modelA2 = getParceledModel(bundle2, MODEL_KEYA)
        val strA2 = ModelDump.toString(modelA2)

        Assert.assertEquals(strA, strA2)
    }

    @Test
    fun parcelizationA2_isCorrect() {
        val modelA = getSerialized("test.ser")
        val strA = ModelDump.toString(modelA)

        val bundle = Bundle()
        putParceledModel(modelA, bundle, MODEL_KEYA)

        val parcel = writeParcel(bundle)
        val bundle2 = readParcel(parcel)
        parcel.recycle()

        checkNotNull(bundle2)
        val modelA2 = getParceledModel(bundle2, MODEL_KEYA)
        val strA2 = ModelDump.toString(modelA2)

        Assert.assertEquals(strA, strA2)
    }

    @Test
    fun parcelizationB2_isCorrect() {
        val modelB = getSerialized("monarchy.ser")
        val strB = ModelDump.toString(modelB)

        val bundle = Bundle()
        putParceledModel(modelB, bundle, MODEL_KEYA)

        val parcel = writeParcel(bundle)
        val bundle2 = readParcel(parcel)
        parcel.recycle()

        checkNotNull(bundle2)
        val modelB2 = getParceledModel(bundle2, MODEL_KEYA)
        val strB2 = ModelDump.toString(modelB2)

        Assert.assertEquals(strB, strB2)
    }

    @Test
    fun parcelization2_isCorrect() {
        val modelA = getSerialized("test.ser")
        val strA = ModelDump.toString(modelA)

        val modelB = getSerialized("monarchy.ser")
        val strB = ModelDump.toString(modelB)

        val bundle = Bundle()
        putParceledModel(modelA, bundle, MODEL_KEYA)
        putParceledModel(modelB, bundle, MODEL_KEYB)

        val parcel = writeParcel(bundle)
        val bundle2 = readParcel(parcel)
        parcel.recycle()

        checkNotNull(bundle2)
        val modelA2 = getParceledModel(bundle2, MODEL_KEYA)
        val modelB2 = getParceledModel(bundle2, MODEL_KEYB)
        val strA2 = ModelDump.toString(modelA2)
        val strB2 = ModelDump.toString(modelB2)

        Assert.assertEquals(strA, strA2)
        Assert.assertEquals(strB, strB2)
    }

    private fun getSerialized(asset: String): Model? {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val assets = appContext.assets
        try {
            assets.open(asset).use { input ->
                ZipInputStream(input).use { zipInput ->
                    return deserializeZip(zipInput, "model") as Model
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    companion object {

        private const val MODEL_KEYA = "modelA"
        private const val MODEL_KEYB = "modelB"

        fun writeParcel(bundle: Bundle): Parcel {
            // write bundle to parcel
            val parcel = Parcel.obtain()
            bundle.writeToParcel(parcel, 0)
            return parcel
        }

        fun readParcel(parcel: Parcel): Bundle? {
            // extract bundle from parcel
            parcel.setDataPosition(0)
            val bundle2 = parcel.readBundle(ParcelableModel::class.java.classLoader)
            if (bundle2 != null) {
                bundle2.classLoader = Model::class.java.classLoader
            }
            return bundle2
        }

        fun putParceledModel(model: Model?, bundle: Bundle, key: String?) {
            val parcelableModel = ParcelableModel(model)
            bundle.putParcelable(key, parcelableModel)
        }

        fun getParceledModel(bundle: Bundle, key: String?): Model? {
            // model2
            @Suppress("DEPRECATION") val pmodel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) //
                bundle.getParcelable(key, ParcelableModel::class.java) else  //
                bundle.getParcelable(key)
            return pmodel?.model
        }

        fun makeDefaultTree(): INode {
            val data = arrayOf( //
                arrayOf("id1", "one\n1", "id11", "eleven\n11", "id12", "twelve\n12", "id13", "thirteen\n13", "id14", "fourteen\n14"),  //
                arrayOf("id2", "two\n2", "id21", "twenty-one\n21", "id22", "twenty-two\n22", "id23", "twenty-three\n23"),  //
                arrayOf("id3", "three\n3", "id31", "thirty-one\n31", "id32", "thirty-two\n32"),  //
                arrayOf("id4", "four\n4", "id41", "forty-one\n41"),  //
                arrayOf("id5", "five\n5")
            ) //
            val root = TreeMutableNode(null, "root") //$NON-NLS-1$
            root.label = "root" //$NON-NLS-1$
            root.backColor = Colors.ORANGE
            root.foreColor = Colors.BLACK
            for (nodeData in data) {
                val node = TreeMutableNode(root, nodeData[0])
                node.label = nodeData[1]
                var i = 2
                while (i < nodeData.size) {
                    val childNode = TreeMutableNode(node, nodeData[i])
                    childNode.label = nodeData[i + 1]
                    i += 2
                }
            }
            return root
        }

        @Throws(IOException::class, ClassNotFoundException::class)
        fun deserializeZip(zis: ZipInputStream, targetEntry: String): Any {
            var entry: ZipEntry
            while ((zis.nextEntry.also { entry = it }) != null) {
                if (entry.name == targetEntry) {
                    ObjectInputStream(zis).use { input ->
                        return input.readObject()
                    }
                }
            }
            throw IOException("zip entry not found $targetEntry")
        }
    }
}
