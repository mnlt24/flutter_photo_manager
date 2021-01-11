package top.kikt.imagescanner.thumb

import android.content.Context
import android.util.Size
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.SystemClock
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.transition.Transition
import io.flutter.plugin.common.MethodChannel
import top.kikt.imagescanner.util.ResultHandler
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Created by debuggerx on 18-9-27 下午2:08
 */
object ThumbnailUtil {

  fun getThumbnailByGlide(ctx: Context, path: String, width: Int, height: Int, format: Int, quality: Int, result: MethodChannel.Result?) {
    val resultHandler = ResultHandler(result)

    Glide.with(ctx)
      .asBitmap()
      .load(File(path))
      .priority(Priority.IMMEDIATE)
      .into(object : BitmapTarget(width, height) {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
          super.onResourceReady(resource, transition)
          val bos = ByteArrayOutputStream()

          val compressFormat =
            if (format == 1) {
              Bitmap.CompressFormat.PNG
            } else {
              Bitmap.CompressFormat.JPEG
            }

          resource.compress(compressFormat, quality, bos)
          resultHandler.reply(bos.toByteArray())
        }

        override fun onLoadCleared(placeholder: Drawable?) {
          resultHandler.reply(null)
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
          resultHandler.reply(null)
        }
      })
  }


  fun getThumbOfUri(context: Context, uri: Uri, width: Int, height: Int, format: Int, quality: Int, callback: (ByteArray?) -> Unit) {
    //println("uri: " + uri + " width: " + width + " height: " + height);

    val compressFormat =
      if (format == 1) {
        Bitmap.CompressFormat.PNG
      } else {
        Bitmap.CompressFormat.JPEG
      }

    if(width <= 200) {
      try {
        val bitmap = context.contentResolver.loadThumbnail(uri, Size(width, height), null)
        val bos = ByteArrayOutputStream()
        bitmap.compress(compressFormat, quality, bos)
        callback(bos.toByteArray())

        return
      }
      catch(e: Exception) {
        println(e)
      }
    }

    val futureTarget = Glide.with(context)
      .asBitmap()
      .load(uri)
      .priority(Priority.IMMEDIATE)
      .submit(width, height)

    val bitmap = futureTarget.get()
    val bos = ByteArrayOutputStream()
    bitmap.compress(compressFormat, quality, bos)
    callback(bos.toByteArray())

    Glide.with(context).clear(futureTarget);

    /*Glide.with(context)
      .asBitmap()
      .load(uri)
      .into(object : BitmapTarget(width, height) {
        val startTime = SystemClock.uptimeMillis()
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
          super.onResourceReady(resource, transition)
          val bos = ByteArrayOutputStream()

          resource.compress(compressFormat, quality, bos)
          callback(bos.toByteArray())
          println("elapsed: " + (SystemClock.uptimeMillis() - startTime))
        }

        override fun onLoadCleared(placeholder: Drawable?) {
          callback(null)
        }
      })*/
  }


}
