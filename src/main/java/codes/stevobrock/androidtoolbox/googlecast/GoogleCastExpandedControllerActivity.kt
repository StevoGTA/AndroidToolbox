package codes.stevobrock.androidtoolbox.googlecast

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.core.content.ContextCompat
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity

//----------------------------------------------------------------------------------------------------------------------
class GoogleCastExpandedControllerActivity : ExpandedControllerActivity() {

	// Activity methods
	//------------------------------------------------------------------------------------------------------------------
	override fun onCreate(savedInstanceState :Bundle?) {
		// Do super
		super.onCreate(savedInstanceState)

		// Retrieve Google Cast Item
		val googleCastItem = GoogleCastItem(intent)

		// Play
		GoogleCastManager.play(googleCastItem, CastContext.getSharedInstance(this))
	}

	//------------------------------------------------------------------------------------------------------------------
	override fun onCreateOptionsMenu(menu :Menu) :Boolean {
		// Do super
		super.onCreateOptionsMenu(menu)

		// Setup menu and to the action bar if it is present.
		if (menuID != null) {
			// Setup menu
			menuInflater.inflate(menuID!!, menu)
			CastButtonFactory.setUpMediaRouteButton(applicationContext, menu, mediaRouteMenuItemID!!)
		}

		return true
	}

	// Instantiation object
	//------------------------------------------------------------------------------------------------------------------
	companion object {

		// Properties
		@JvmStatic	var	menuID :Int? = null
		@JvmStatic	var	mediaRouteMenuItemID :Int? = null

		// Methods
		//--------------------------------------------------------------------------------------------------------------
		@JvmStatic
		fun start(context :Context, googleCastItem :GoogleCastItem) {
			// Setup intent
			val	intent = Intent(context, GoogleCastExpandedControllerActivity::class.java).apply {
				// Setup
				googleCastItem.put(this)
			}

			// Start
			ContextCompat.startActivity(context, intent, Bundle.EMPTY)
		}
	}
}
