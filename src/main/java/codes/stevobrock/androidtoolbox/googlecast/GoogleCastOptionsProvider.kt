package codes.stevobrock.androidtoolbox.googlecast

import android.content.Context
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.NotificationOptions

//----------------------------------------------------------------------------------------------------------------------
class GoogleCastOptionsProvider : OptionsProvider {

	// OptionsProvider methods
	//------------------------------------------------------------------------------------------------------------------
	override fun getCastOptions(context :Context) :CastOptions {
		// Return cast options
		val notificationOptions =
					NotificationOptions.Builder()
						.build()
		val mediaOptions =
					CastMediaOptions.Builder()
						.setNotificationOptions(notificationOptions)
						.build()
		val castOptions =
					CastOptions.Builder()
						.setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
						.setCastMediaOptions(mediaOptions)
						.build()

		return castOptions
	}

	//------------------------------------------------------------------------------------------------------------------
	override fun getAdditionalSessionProviders(context :Context) :List<SessionProvider>? { return null }
}
