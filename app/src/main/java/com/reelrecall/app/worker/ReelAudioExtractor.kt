package com.reelrecall.app.worker

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

/**
 * Copies the first audio track out of a downloaded reel into an MPEG-4 audio file
 * so cloud transcription uploads speech instead of the full video.
 */
object ReelAudioExtractor {
    fun extractM4a(videoFile: File, destination: File): Result<File> {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        var started = false
        try {
            extractor.setDataSource(videoFile.absolutePath)
            var audioIndex = -1
            var format: MediaFormat? = null
            for (index in 0 until extractor.trackCount) {
                val candidate = extractor.getTrackFormat(index)
                val mime = candidate.getString(MediaFormat.KEY_MIME).orEmpty()
                if (mime.startsWith("audio/")) {
                    audioIndex = index
                    format = candidate
                    break
                }
            }
            if (audioIndex < 0 || format == null) {
                return Result.failure(
                    IllegalStateException("The media asset contains no extractable audio tracks.")
                )
            }
            extractor.selectTrack(audioIndex)
            if (destination.exists()) destination.delete()

            val created = MediaMuxer(destination.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer = created
            val track = created.addTrack(format)
            created.start()
            started = true

            val buffer = ByteBuffer.allocate(256 * 1024)
            val info = MediaCodec.BufferInfo()
            var samples = 0
            while (true) {
                info.offset = 0
                info.size = extractor.readSampleData(buffer, 0)
                if (info.size < 0) break
                info.presentationTimeUs = extractor.sampleTime
                info.flags = extractor.sampleFlags
                created.writeSampleData(track, buffer, info)
                samples += 1
                extractor.advance()
            }
            if (samples == 0) {
                return Result.failure(
                    IllegalStateException("The media asset contains no extractable audio tracks.")
                )
            }
            created.stop()
            started = false
            if (!destination.exists() || destination.length() <= 0L) {
                return Result.failure(IllegalStateException("Audio export produced an empty file."))
            }
            return Result.success(destination)
        } catch (error: Exception) {
            destination.delete()
            return Result.failure(error)
        } finally {
            if (started) {
                try {
                    muxer?.stop()
                } catch (_: Exception) {
                }
            }
            try {
                muxer?.release()
            } catch (_: Exception) {
            }
            extractor.release()
        }
    }
}
