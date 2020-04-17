package codes.stevobrock.androidtoolbox.googlecast

import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext

//----------------------------------------------------------------------------------------------------------------------
object GoogleCastManager {

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun play(googleCastItem :GoogleCastItem, castContext :CastContext) {
		// Setup
		val castSession = castContext.sessionManager.currentCastSession ?: return
		val remoteMediaClient = castSession.remoteMediaClient ?: return

		val mediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
		mediaMetadata.putString(MediaMetadata.KEY_TITLE, googleCastItem.title)
		mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, googleCastItem.subtitle)

		var streamType :Int
		if (googleCastItem.uri.path!!.endsWith("mp4"))
			// MP4
			streamType = MediaInfo.STREAM_TYPE_BUFFERED
		else if (googleCastItem.uri.path!!.endsWith("m3u8"))
			// HLS
			streamType = MediaInfo.STREAM_TYPE_LIVE
		else
			// ???
			streamType = MediaInfo.STREAM_TYPE_NONE

		val mediaInfo =
					MediaInfo.Builder(googleCastItem.uri.toString())
						.setStreamType(streamType)
						.setContentType("videos/mp4")
						.setMetadata(mediaMetadata)
						.build()

		val mediaLoadRequestData =
					MediaLoadRequestData.Builder()
						.setMediaInfo(mediaInfo)
						.setAutoplay(true)
						.build()

		// Load the request
		remoteMediaClient.load(mediaLoadRequestData)
	}
}
