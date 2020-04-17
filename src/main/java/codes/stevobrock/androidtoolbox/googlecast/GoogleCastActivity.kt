package codes.stevobrock.androidtoolbox.googlecast

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

//----------------------------------------------------------------------------------------------------------------------
open class GoogleCastActivity : AppCompatActivity() {

	// Properties
	private	var	castContext :CastContext? = null

	// Activity methods
	//------------------------------------------------------------------------------------------------------------------
	override fun onCreate(savedInstanceState :Bundle?) {
		// Do super
		super.onCreate(savedInstanceState)

		// Setup
		this.castContext = CastContext.getSharedInstance(this)
	}

	//------------------------------------------------------------------------------------------------------------------
	override fun onResume() {
		// Do super
		super.onResume()

		// Check Google Play Services status
		val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
		if (result != ConnectionResult.SUCCESS)
			// Show dialog
			GoogleApiAvailability.getInstance().getErrorDialog(this, result, 0).show()
	}

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun setupMediaRouteButton(menu :Menu, menuItemID :Int) {
		// Setup Media Route Button
		CastButtonFactory.setUpMediaRouteButton(applicationContext, menu, menuItemID)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun hasCurrentCastSession() :Boolean { return this.castContext!!.sessionManager.currentSession != null }
}
