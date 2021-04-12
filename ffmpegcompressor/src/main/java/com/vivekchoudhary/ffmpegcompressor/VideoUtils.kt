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


    fun getFileSizeInMb(path: String?): Int {
        val outputFile = File(path)
        val outputCompressVideosize = outputFile.length()
        val fileSizeInKB = outputCompressVideosize / 1024
        val fileSizeInMB = fileSizeInKB / 1024
        return fileSizeInMB.toInt()
    }



}