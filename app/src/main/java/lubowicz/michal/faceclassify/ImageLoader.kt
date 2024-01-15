package lubowicz.michal.faceclassify

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class ImageLoader {
    fun loadImageFromFileName(context: Context, fileName: String): Bitmap {
        val options = BitmapFactory.Options()
        options.inScaled = false
        val resourceId = context.resources.getIdentifier(fileName, "drawable", context.packageName)
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

        return bitmap
    }
}