package lubowicz.michal.faceclassify

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.metadata.schema.Content
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import kotlinx.serialization.decodeFromString
import lubowicz.michal.faceclassify.ml.EfficientNetB6Age
import lubowicz.michal.faceclassify.ml.EfficientNetB6Emo
import lubowicz.michal.faceclassify.ml.EfficientNetB6Gender
import lubowicz.michal.faceclassify.ml.EfficientNetB6Eth
import lubowicz.michal.faceclassify.ml.EfficientNetV2B3Age
import lubowicz.michal.faceclassify.ml.EfficientNetV2B3Emo
import lubowicz.michal.faceclassify.ml.EfficientNetV2B3Gender
import lubowicz.michal.faceclassify.ml.EfficientNetV2B3Eth
import lubowicz.michal.faceclassify.ml.MobileNetV3SmallAge
import lubowicz.michal.faceclassify.ml.MobileNetV3SmallEmo
import lubowicz.michal.faceclassify.ml.MobileNetV3SmallGender
import lubowicz.michal.faceclassify.ml.MobileNetV3SmallEth
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.task.gms.vision.classifier.ImageClassifier
import kotlin.reflect.typeOf

public class Logic {

    fun uriToFile(uri: Uri, contentResolver: ContentResolver, cacheDir: File): File? {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        try {
            inputStream?.let {
                val tempFile = createTempFile("temp_image", null, cacheDir)
                tempFile.outputStream().use { output ->
                    it.copyTo(output)
                }
                return tempFile
            }
        } finally {
            inputStream?.close()
        }
        return null
    }


    fun classify(bitmap: Bitmap, contentResolver: ContentResolver, cacheDir: File, applicationContext: Context, mode: String, classes: Array<Boolean>): Map<String,String> {
        var result = mutableMapOf<String, String>()
                val currentTimeMillis = System.currentTimeMillis()

                var bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
                val currentTimeMillis2 = System.currentTimeMillis()
                val image = TensorImage.fromBitmap(bitmap)

                if (mode == "wydajny") {
            if (classes[0]) {

                val currentTimeMillis = System.currentTimeMillis()
                val model = MobileNetV3SmallGender.newInstance(applicationContext)
                val currentTimeMillis2 = System.currentTimeMillis()
                Log.d("loading time", (currentTimeMillis2 - currentTimeMillis).toString())

                val currentTimeMillis3 = System.currentTimeMillis()
                        val outputs = model.process(image)
                val currentTimeMillis4 = System.currentTimeMillis()
                Log.d("classification time", (currentTimeMillis4 - currentTimeMillis3).toString())

                val probability = outputs.probabilityAsCategoryList
                val maxProbabilityIndex = probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }

                val label = probability[maxProbabilityIndex].label

                model.close()
                result["Gender"] = mapGender(label)

            }
            if (classes[1]) {
                val currentTimeMillis = System.currentTimeMillis()
                val model = MobileNetV3SmallAge.newInstance(applicationContext)
                val currentTimeMillis2 = System.currentTimeMillis()
                Log.d("loading time", (currentTimeMillis2 - currentTimeMillis).toString())

                val currentTimeMillis3 = System.currentTimeMillis()
                val outputs = model.process(image)
                val currentTimeMillis4 = System.currentTimeMillis()
                Log.d("classification time", (currentTimeMillis4 - currentTimeMillis3).toString())

                val probability = outputs.probabilityAsCategoryList
                val maxProbabilityIndex = probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }

                val label = probability[maxProbabilityIndex].label

                model.close()
                result["Age"] = mapAge(label)

            }
            if (classes[2]) {
                val currentTimeMillis = System.currentTimeMillis()
                val model = MobileNetV3SmallEmo.newInstance(applicationContext)
                val currentTimeMillis2 = System.currentTimeMillis()
                Log.d("loading time", (currentTimeMillis2 - currentTimeMillis).toString())

                val currentTimeMillis3 = System.currentTimeMillis()
                val outputs = model.process(image)
                val currentTimeMillis4 = System.currentTimeMillis()
                Log.d("classification time", (currentTimeMillis4 - currentTimeMillis3).toString())

                val probability = outputs.probabilityAsCategoryList
                val maxProbabilityIndex = probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }

                val label = probability[maxProbabilityIndex].label

                model.close()
                result["Emo"] = mapEmo(label)

            }
            if (classes[3]) {
                val currentTimeMillis = System.currentTimeMillis()
                val model = MobileNetV3SmallEth.newInstance(applicationContext)
                val currentTimeMillis2 = System.currentTimeMillis()
                Log.d("loading time", (currentTimeMillis2 - currentTimeMillis).toString())

                val currentTimeMillis3 = System.currentTimeMillis()
                val outputs = model.process(image)
                val currentTimeMillis4 = System.currentTimeMillis()
                Log.d("classification time", (currentTimeMillis4 - currentTimeMillis3).toString())

                val probability = outputs.probabilityAsCategoryList
                val maxProbabilityIndex = probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }

                val label = probability[maxProbabilityIndex].label

                model.close()
                result["Eth"] = mapEth(label)

            }
        } else if (mode == "optymalny") {
            if (classes[0]) {
                val currentTimeMillis = System.currentTimeMillis()
                val model = EfficientNetV2B3Gender.newInstance(applicationContext)
                val currentTimeMillis2 = System.currentTimeMillis()
                Log.d("loading time", (currentTimeMillis2 - currentTimeMillis).toString())

                val currentTimeMillis3 = System.currentTimeMillis()
                val outputs = model.process(image)
                val currentTimeMillis4 = System.currentTimeMillis()
                Log.d("classification time", (currentTimeMillis4 - currentTimeMillis3).toString())

                val probability = outputs.probabilityAsCategoryList
                val maxProbabilityIndex = probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }

                val label = probability[maxProbabilityIndex].label

                model.close()
                result["Gender"] = mapGender(label)

            }
            if (classes[1]) {
                val currentTimeMillis = System.currentTimeMillis()
                val model = EfficientNetV2B3Age.newInstance(applicationContext)
                val currentTimeMillis2 = System.currentTimeMillis()
                Log.d("loading time", (currentTimeMillis2 - currentTimeMillis).toString())

                val currentTimeMillis3 = System.currentTimeMillis()
                val outputs = model.process(image)
                val currentTimeMillis4 = System.currentTimeMillis()
                Log.d("classification time", (currentTimeMillis4 - currentTimeMillis3).toString())

                val probability = outputs.probabilityAsCategoryList
                val maxProbabilityIndex = probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }

                val label = probability[maxProbabilityIndex].label

                model.close()
                result["Age"] = mapAge(label)

            }
            if (classes[2]) {
                val currentTimeMillis = System.currentTimeMillis()
                val model = EfficientNetV2B3Emo.newInstance(applicationContext)
                val currentTimeMillis2 = System.currentTimeMillis()
                Log.d("loading time", (currentTimeMillis2 - currentTimeMillis).toString())
                val currentTimeMillis3 = System.currentTimeMillis()
                val outputs = model.process(image)
                val currentTimeMillis4 = System.currentTimeMillis()
                Log.d("classification time", (currentTimeMillis4 - currentTimeMillis3).toString())

                val probability = outputs.probabilityAsCategoryList
                val maxProbabilityIndex = probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }

                val label = probability[maxProbabilityIndex].label

                model.close()
                result["Emo"] = mapEmo(label)

            }
            if (classes[3]) {
                val currentTimeMillis = System.currentTimeMillis()
                val model = EfficientNetV2B3Eth.newInstance(applicationContext)
                val currentTimeMillis2 = System.currentTimeMillis()
                Log.d("loading time", (currentTimeMillis2 - currentTimeMillis).toString())

                val currentTimeMillis3 = System.currentTimeMillis()
                val outputs = model.process(image)
                val currentTimeMillis4 = System.currentTimeMillis()
                Log.d("classification time", (currentTimeMillis4 - currentTimeMillis3).toString())

                val probability = outputs.probabilityAsCategoryList
                val maxProbabilityIndex =
                    probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }

                val label = probability[maxProbabilityIndex].label

                model.close()
                result["Eth"] = mapEth(label)
            }
        } else if (mode == "dokładny") {
            if (classes[0]) {
                val currentTimeMillis = System.currentTimeMillis()
                val model = EfficientNetB6Gender.newInstance(applicationContext)
                val currentTimeMillis2 = System.currentTimeMillis()
                Log.d("loading time", (currentTimeMillis2 - currentTimeMillis).toString())
                val currentTimeMillis3 = System.currentTimeMillis()
                val outputs = model.process(image)
                val currentTimeMillis4 = System.currentTimeMillis()
                Log.d("classification time", (currentTimeMillis4 - currentTimeMillis3).toString())
                val probability = outputs.probabilityAsCategoryList
                val maxProbabilityIndex =
                    probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }

                val label = probability[maxProbabilityIndex].label

                model.close()
                result["Gender"] = mapGender(label)

            }
            if (classes[1]) {
                val currentTimeMillis = System.currentTimeMillis()
                val model = EfficientNetB6Age.newInstance(applicationContext)
                val currentTimeMillis2 = System.currentTimeMillis()
                Log.d("loading time", (currentTimeMillis2 - currentTimeMillis).toString())
                val currentTimeMillis3 = System.currentTimeMillis()
                val outputs = model.process(image)
                val currentTimeMillis4 = System.currentTimeMillis()
                Log.d("classification time", (currentTimeMillis4 - currentTimeMillis3).toString())

                val probability = outputs.probabilityAsCategoryList
                val maxProbabilityIndex =
                    probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }

                val label = probability[maxProbabilityIndex].label

                model.close()
                result["Age"] = mapAge(label)

            }
            if (classes[2]) {
                val currentTimeMillis = System.currentTimeMillis()
                val model = EfficientNetB6Emo.newInstance(applicationContext)
                val currentTimeMillis2 = System.currentTimeMillis()
                Log.d("loading time", (currentTimeMillis2 - currentTimeMillis).toString())


                val currentTimeMillis3 = System.currentTimeMillis()
                val outputs = model.process(image)
                val currentTimeMillis4 = System.currentTimeMillis()
                Log.d("classification time", (currentTimeMillis4 - currentTimeMillis3).toString())

                val probability = outputs.probabilityAsCategoryList
                val maxProbabilityIndex =
                    probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }

                val label = probability[maxProbabilityIndex].label

                model.close()
                result["Emo"] = mapEmo(label)

            }
            if (classes[3]) {
                val currentTimeMillis = System.currentTimeMillis()
                val model = EfficientNetB6Eth.newInstance(applicationContext)
                val currentTimeMillis2 = System.currentTimeMillis()
                Log.d("loading time", (currentTimeMillis2 - currentTimeMillis).toString())


                val currentTimeMillis3 = System.currentTimeMillis()
                val outputs = model.process(image)
                val currentTimeMillis4 = System.currentTimeMillis()
                Log.d("classification time", (currentTimeMillis4 - currentTimeMillis3).toString())
                val probability = outputs.probabilityAsCategoryList
                val maxProbabilityIndex =
                    probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }

                val label = probability[maxProbabilityIndex].label

                model.close()
                result["Eth"] = mapEth(label)

            }
        }
        return result
    }




    fun classifyTestMode(applicationContext: Context): String? {
        val model = MobileNetV3SmallGender.newInstance(applicationContext)

        val size = 100
        val r1 = this.test(GenderData().FEMALES_LIST.shuffled().take(size), applicationContext, model,"Females")
        var r2 = this.test(GenderData().MALES_LIST.shuffled().take(size), applicationContext, model,"Males")
        model.close()
        return (((r1.get(0)+r2.get(0))/(size*2).toFloat()).toString() + " " + (r1.get(1) + r2.get(1)).toString())

        return ""
    }

    fun test(array: List<String>,applicationContext: Context,model:MobileNetV3SmallGender, expected: String): Array<Float> {
        var corectAnswers = 0
        var time = 0L
        array.forEach {
            var bitmap = ImageLoader().loadImageFromFileName(applicationContext, it)
            val image = TensorImage.fromBitmap(bitmap)

            val startTime = System.currentTimeMillis()

            val outputs = model.process(image)
            val endTime = System.currentTimeMillis()
            val elapsedTime = endTime - startTime

            val probability = outputs.probabilityAsCategoryList
            val maxProbabilityIndex =
                probability.indexOfFirst { it.score == probability.maxByOrNull { it.score }?.score }

            val label = probability[maxProbabilityIndex].label
            if (label == expected) corectAnswers+=1
            time += elapsedTime
            Log.d("test result", probability.toString() + " " + elapsedTime.toString())
        }
        return arrayOf(corectAnswers.toFloat(), time.toFloat())
    }

    fun rotateBitmapIfNeeded(bitmap: Bitmap, file: File): Bitmap {
        val exifInterface = androidx.exifinterface.media.ExifInterface(file.absolutePath)
        val orientation = exifInterface.getAttributeInt(
            androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
            androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED
        )

        return when (orientation) {
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }
    fun mapGender(org: String): String {
        val gender = mutableMapOf<String, String>()
        gender["Males"] = "mężczyzna"
        gender["Females"] = "kobieta"
        return gender[org]!!
    }

    fun mapAge(org: String): String {
        val age = mutableMapOf<String, String>()
        age["folder0"] = "0-6"
        age["folder1"] = "7-18"
        age["folder2"] = "19-26"
        age["folder3"] = "27-40"
        age["folder4"] = "41-60"
        age["folder5"] = "60+"
        return age[org]!!
    }

    fun mapEth(org: String): String {
        val eth = mutableMapOf<String, String>()
        eth["Asian"] = "azjatyckie"
        eth["Black"] = "czarnoskórzy"
        eth["Indian"] = "indyjskie"
        eth["Others"] = "inne"
        eth["White"] = "biali"
        return eth[org]!!
    }

    fun mapEmo(org: String): String {
        val eth = mutableMapOf<String, String>()
        eth["angry"] = "złość"
        eth["disgust"] = "zdegustowanie"
        eth["fear"] = "strach"
        eth["happy"] = "radość"
        eth["neutral"] = "neutralny"
        eth["sad"] = "smutek"
        eth["surprise"] = "zdziwienie"
        return eth[org]!!
    }


    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }


}