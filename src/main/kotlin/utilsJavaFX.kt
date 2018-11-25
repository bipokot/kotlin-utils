import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.google.common.io.BaseEncoding
import com.google.common.io.Resources
import com.sun.javafx.collections.ObservableListWrapper
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Slider
import javafx.scene.image.WritableImage
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO

val EMPTY_IMAGE = SwingFXUtils.toFXImage(arrayToImage(arrayOf(intArrayOf(0))), null)

private fun bytesToImage(bytes: ByteArray): BufferedImage {
    val stream = ByteArrayInputStream(bytes)
    val image = ImageIO.read(stream)
    stream.close()
    return image
}

private fun imageToBytes(bufferedImage: BufferedImage): ByteArray {
    val stream = ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "png", stream)
    val bytes = stream.toByteArray()
    stream.close()
    return bytes
}

fun scaleImage(image: BufferedImage, factor: Int = 1): WritableImage {
    return if (factor != 1) {
        val scaledImage: Image = image.getScaledInstance(factor * image.width, factor * image.height, Image.SCALE_DEFAULT);
        SwingFXUtils.toFXImage(scaledImage as BufferedImage, null)
    } else {
        SwingFXUtils.toFXImage(image, null)
    }
}

fun loadResourceImage(resourceName: String): WritableImage {
    return SwingFXUtils.toFXImage(ImageIO.read(Resources.getResource(resourceName)), null)
}

class ImageSerializer : JsonSerializer<BufferedImage>() {

    @Throws(IOException::class)
    override fun serialize(bufferedImage: BufferedImage, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
        jsonGenerator.writeString(BaseEncoding.base64().encode(imageToBytes(bufferedImage)))
    }
}

class ImageDeserializer : JsonDeserializer<BufferedImage>() {

    @Throws(IOException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): BufferedImage {
        return bytesToImage(BaseEncoding.base64().decode(jsonParser.valueAsString))
    }
}

abstract class MyStage(fxmlFile: String) : Stage() {

    init {
        tryOrFail {
            val fxmlLoader = FXMLLoader()
            fxmlLoader.setController(this)
            fxmlLoader.location = javaClass.getResource(fxmlFile)
            val content: Parent? = fxmlLoader.load<Parent>()
            scene = Scene(content)
            scene.stylesheets.add("css/style.css")
        }
    }
}

fun showFXMLStage(title: String, fxmlFile: String, width: Int, height: Int, style: StageStyle) {
    try {
        val root = FXMLLoader.load<Parent>(Any::class.java.getResource(fxmlFile))
        val stage = Stage(style)
        stage.title = title
        val scene = Scene(root, width.toDouble(), height.toDouble())
        stage.scene = scene
        stage.show()
    } catch (e: IOException) {
        e.printStackTrace()
        showAndWaitError(e.message, e)
    }
}

fun <T> ChoiceBox<T>.itemsFrom(list: List<T>) {
    items = ObservableListWrapper(list)
}

fun <T> ChoiceBox<T>.itemsFrom(vararg items: T) {
    this.items = ObservableListWrapper(listOf(*items))
}

fun <T> ChoiceBox<T>.onSelectionChanged(action: (T) -> Unit) {
    selectionModel.selectedItemProperty().addListener { _, _, newValue -> action(newValue) }
}

fun Slider.onSliderChanged(action: (Double) -> Unit) {
    valueProperty().addListener { _, _, _ -> action(value) }
}

fun showAndWaitAlert(title: String, headerText: String?, contentText: String, type: Alert.AlertType) {
    val alert = Alert(type)
    alert.title = title
    alert.headerText = headerText
    alert.contentText = contentText
    alert.showAndWait()
}

fun showAndWaitError(headerText: String?, vararg e: Throwable) {
    showAndWaitAlert("Ошибка", headerText, if (e.size != 0) e[0].message ?: "" else "", Alert.AlertType.ERROR)
}

fun showAndWaitWarning(headerText: String?, vararg e: Throwable) {
    showAndWaitAlert("Внимание", headerText, if (e.size != 0) e[0].message ?: "" else "", Alert.AlertType.WARNING)
}

fun showAndWaitInfo(headerText: String?, contentText: String) {
    showAndWaitAlert("Информация", headerText, contentText, Alert.AlertType.INFORMATION)
}
