package com.vivekchoudhary.ffmpegcompressor

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import java.io.File
import java.util.*


class VideoCompression(private val context: Context)  {

    private var status = NONE
    private var errorMessage = "Compression Failed!"


    fun startCompressing(inputPath: String?, listener: CompressionListener?) {
        if (inputPath == null || inputPath.isEmpty()) {
            status = NONE
            listener?.compressionFinished(NONE, false, null)
            return
        }

        Log.d(TAG, "startCompressing: input path : $inputPath")
        var outputPath = ""
        outputPath = "$appDir/video_compressed_" + System.currentTimeMillis() + ".mp4"

        compressVideo(inputPath, outputPath, listener)
    }

    val appDir: String
        get() {
            var outputPath =
                Environment.getExternalStorageDirectory().absolutePath
            outputPath += "/" + "VideoCompressor"
            var file = File(outputPath)
            if (!file.exists()) {
                file.mkdir()
            }
            outputPath += "/" + "videocompress"
            file = File(outputPath)
            if (!file.exists()) {
                file.mkdir()
            }

            Log.d("mOutputFile", outputPath)
            return outputPath
        }

    private fun compressVideo(
        inputPath: String,
        outputFilePath: String,
        listener: CompressionListener?
    ) {
        try {
            val mRetriever = MediaMetadataRetriever()
            mRetriever.setDataSource(inputPath)
            val frame = mRetriever.frameAtTime

            val width = frame?.width
            val height = frame?.height


            val crf = getCRF(inputPath, width!!, height!!)
            Log.d(TAG, "compressVideo: CRF -> $crf")

            val scalefactor = getScale(width)
            val scale =
                "scale=$scalefactor:-2:force_original_aspect_ratio=decrease,pad='iw+mod(iw\\,2)':'ih+mod(ih\\,2)'"

            if (scalefactor > 480) {
                Log.d(TAG, "compressVideo: scale factor more than 480 so scaling to 640...")
                val command = arrayOf(
                    "-y",
                    "-i",
                    inputPath!!,
                    "-max_muxing_queue_size",
                    "9999",
                    "-vf",
                    scale,
                    "-c:v",
                    "libx264",
                    "-crf",
                    crf.toString(),
                    "-b:v",
                    "800k",
                    "-c:a",
                    "aac",
                    "-preset",
                    "ultrafast",
                    outputFilePath
                )
                Log.d(TAG, "compressVideo: ${command.toString()}")
                execFFmpegBinary(command, inputPath, listener, outputFilePath)
            } else {
                Log.d(TAG, "compressVideo: scale factor less than 480 so no scaling...")

                val scale =
                    "scale=w=${width}:h=${height}:force_original_aspect_ratio=decrease,pad='iw+mod(iw\\,2)':'ih+mod(ih\\,2)'"

                val command = arrayOf(
                    "-y",
                    "-i",
                    inputPath,
                    "-max_muxing_queue_size",
                    "9999",
                    "-vf",
                    scale,
                    "-c:v",
                    "libx264",
                    "-crf",
                    crf.toString(),
                    "-b:v",
                    "800k",
                    "-c:a",
                    "aac",
                    "-preset",
                    "ultrafast",
                    outputFilePath
                )
                execFFmpegBinary(command, inputPath, listener, outputFilePath)
            }


        } catch (e: Exception) {
            e.printStackTrace()
            status = FAILED
            listener?.onFailure("Error : " + e.message)
        }
    }

    private fun getScale(width: Int): Int {
        if (width > 2000) {
            // 4k
            Log.d(TAG, "getCRF: Scale : 640")
            return 640
        } else if (width > 1000) {
            Log.d(TAG, "getCRF:  Scale : 640")
            return 640
        } else if (width > 700) {
            Log.d(TAG, "getCRF:  Scale : 640")
            return 640
        } else if (width > 400) {
            Log.d(TAG, "getCRF:  Scale : 480")
            return 480
        } else if (width > 300) {
            Log.d(TAG, "getCRF:  Scale : 320")
            return 320
        } else if (width > 200) {
            Log.d(TAG, "getCRF:  Scale : 240")
            return 240
        } else if (width > 100) {
            Log.d(TAG, "getCRF:  Scale : 160")
            return 160
        }

        return 640
    }

    private fun getCRF(inputPath: String?, width: Int, height: Int): Int {
        val filesize = VideoUtils.getFileSizeInMb(inputPath)
        Log.d(TAG, "getCRF: size : $filesize ")
        //Toast.makeText(context, "Original size: $filesize MB", Toast.LENGTH_SHORT).show()

        var videomode: Int = 0
        if (width > 2000) {
            // 4k
            Log.d(TAG, "getCRF: 4k video")
            videomode = 1
        } else if (width > 1000) {
            Log.d(TAG, "getCRF: 1080p video")
            videomode = 2
        } else if (width > 700) {
            Log.d(TAG, "getCRF: 720p video ")
            videomode = 3
        } else if (width > 400) {
            Log.d(TAG, "getCRF: 480p video")
            videomode = 4
        }
        when (filesize) {
            in 0..1100 -> {
                if (videomode == 1) return 20 else return 28
            }

            in 1100..1300 -> {
                if (videomode == 1) return 27 else return 28
            }

            in 1300..1700 -> {
                if (videomode == 1) return 30 else return 28
            }

            else -> {
                if (videomode == 1) return 32 else return 28
            }
        }
    }

    /**
     * Executing ffmpeg binary
     */
    private fun execFFmpegBinary(
        command: Array<String>,
        inputPath: String?,
        listener: CompressionListener?,
        outputFilePath: String
    ) {
        Config.enableLogCallback { message -> Log.e(Config.TAG, message.text) }
        Config.enableStatisticsCallback { newStatistics ->
            val videoLength = inputPath?.let { VideoUtils.getVideoDuration(it) }
            val progress: Int = ((
                    java.lang.String.valueOf(newStatistics.time)
                        .toFloat() / videoLength!!) * 100).toInt()


            Log.d(TAG, "execFFmpegBinary: ${newStatistics.time}  | $videoLength | ${progress}")
            var progressFinal = progress
            listener?.onProgress(progressFinal)

        }
        try {
            val executionId = com.arthenica.mobileffmpeg.FFmpeg.executeAsync(
                command
            ) { executionId1: Long, returnCode: Int ->
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d(
                        TAG,
                        "Finished command : ffmpeg " + Arrays.toString(command)
                    )

                    listener?.compressionFinished(SUCCESS, true, fileOutputPath = outputFilePath)

                } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                    Log.e(
                        TAG,
                        "Async command execution cancelled by user."
                    )
                    listener?.onFailure(
                        String.format(
                            "Async command execution cancelled by user."
                        )
                    )
                } else {
                    Log.e(
                        TAG,
                        String.format(
                            "Async command execution failed with returnCode=%d.",
                            returnCode
                        )
                    )
                    listener?.onFailure(
                        String.format(
                            "Async command execution failed with returnCode=%d.",
                            returnCode
                        )
                    )

                }
            }
            Log.e(TAG, "execFFmpegMergeVideo executionId-$executionId")
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "execFFmpegBinary: ${e.printStackTrace()}")
            e.printStackTrace()
        }
    }

    interface CompressionListener {
        fun compressionFinished(
            status: Int,
            isVideo: Boolean,
            fileOutputPath: String?
        )

        fun onFailure(message: String?)
        fun onProgress(progress: Int)
    }

    val isDone: Boolean
        get() = status == SUCCESS || status == NONE

    companion object {
        private const val TAG = "VideoCompressor"
        const val SUCCESS = 1
        const val FAILED = 2
        const val NONE = 3
        const val RUNNING = 4
    }

}