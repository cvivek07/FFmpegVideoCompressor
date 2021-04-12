package com.vivekchoudhary.ffmpegcompressor

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.io.*
import java.nio.channels.FileChannel
import java.util.concurrent.TimeUnit


object VideoUtils {
    private val TAG: String = VideoUtils.javaClass.simpleName

    const val MaxVideoWidth = 540
    const val MaxVideoHeight = 960
    private const val ClearOldRecordings = true
    private const val DurationSeconds = "%d:%02d"
    private const val DurationHours = "%02d:%02d"

    private const val RecordingFileType = ".mp4"
    private const val RecordingThumbType = ".jpg"
    private const val MergeVideoKey = "vide"
    private const val MergeAudioKey = "soun"
    private const val FileAccessMode = "rw"
    val MillisInHour = TimeUnit.HOURS.toMillis(1)
    val MillisInMinute = TimeUnit.MINUTES.toMillis(1)


    fun deletePath(path: String) {
        val dir = File(path)
        dir.deleteRecursively()
    }

    fun getDurationString(duration: Long, isHour: Boolean = false): String {
        val formatter = if (isHour) DurationHours else DurationSeconds
        val minutes = duration / MillisInMinute
        val seconds = (TimeUnit.MILLISECONDS.toSeconds(duration % MillisInMinute))
        return String.format(formatter, minutes, seconds)
    }

    fun getDurationText(duration: Long): String {
        return if (duration < MillisInHour) {
            getDurationString(duration)
        } else {
            val hours = duration / MillisInHour
            val remaining = duration % MillisInHour
            String.format("%d:%s", hours, getDurationString(remaining, true))
        }
    }


    fun getVideoDuration(videoPath: String): Long {
        var duration = 0L
        try {


            getMediaRetriever(videoPath).run {
                duration =
                    extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
                release()
            }
        } catch (e: java.lang.Exception) {

        }
        return duration

    }


    private fun getVideoResolution(videoPath: String): Pair<Int, Int> {
        var height: Int
        var width: Int
        getMediaRetriever(videoPath).run {
            height = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
            width = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
            release()
        }
        return Pair(width, height)
    }

    public fun getTargetSize(videoPath: String): Pair<Int, Int> {
        var width: Int
        var height: Int
        getMediaRetriever(videoPath).run {
            width =
                Integer.valueOf(extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
            height =
                Integer.valueOf(extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
            release()
        }
        return if (width > height) {
            //Landscape video
            Pair(MaxVideoWidth, MaxVideoHeight)
        } else {
            //Portrait video
            Pair(MaxVideoHeight, MaxVideoWidth)
        }
    }

    public fun getMediaRetriever(videoPath: String): MediaMetadataRetriever {
        return MediaMetadataRetriever().apply {
            try {
                if (videoPath != null && !videoPath.isEmpty())
                    setDataSource(videoPath)
            } catch (e: RuntimeException) {
//                e.printException()
            }

        }
    }

    fun getFileSizeInMb(file: File?): Long {
        file?.run {
            val fileSizeInBytes = file.length()
            val fileSizeInKB = fileSizeInBytes / 1024
            //file size in mb
            return fileSizeInKB / 1024
        }
        return 0
    }

    private fun storeImage(context: Context, image: File) {
        val pictureFile: File = getOutputMediaFile(context) ?: return
        try {
            val output = FileOutputStream(pictureFile)
            val input = FileInputStream(image)
            val inputChannel: FileChannel = input.channel
            val outputChannel: FileChannel = output.channel
            inputChannel.transferTo(0, inputChannel.size(), outputChannel)
            output.close()
            input.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getOutputMediaFile(context: Context): File? {


        val dirPath: String =
            context.getExternalFilesDir(null)?.absolutePath.toString() + "/urfeed/"

        val dir = File(dirPath)

        if (!dir.exists()) {
            dir.mkdir()
        }

        val filePath = dirPath + System.currentTimeMillis() + ".gif";

        return File(filePath)
    }

    fun getFileSizeInMb(path: String?): Int {
        val outputFile = File(path)
        val outputCompressVideosize = outputFile.length()
        val fileSizeInKB = outputCompressVideosize / 1024
        val fileSizeInMB = fileSizeInKB / 1024
        return fileSizeInMB.toInt()
    }

    fun saveImage(image: Bitmap, storageDir: File, imageFileName: String): String? {
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }

        val imageFile = File(storageDir, imageFileName)
        val savedImagePath = imageFile.absolutePath
        try {
            val fOut: OutputStream = FileOutputStream(imageFile)
            image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.close()


            return savedImagePath;
//                Toast.makeText(this@MainActivity, "Image Saved!", Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.Exception) {
//                Toast.makeText(this@MainActivity, "Error while saving image!", Toast.LENGTH_SHORT)
//                    .show()
//            e.logException()
            e.printStackTrace()
        }

        return null

    }

}