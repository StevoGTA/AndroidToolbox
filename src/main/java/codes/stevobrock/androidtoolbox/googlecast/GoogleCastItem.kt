package codes.stevobrock.androidtoolbox.googlecast

import android.content.Intent
import android.net.Uri

//----------------------------------------------------------------------------------------------------------------------
class GoogleCastItem {

	// Properties
	val	title :String?
	val	subtitle :String?
	val	uri :Uri

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(title :String?, subtitle :String?, uri :Uri) {
		// Store
		this.title = title
		this.subtitle = subtitle
		this.uri = uri
	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(intent :Intent) {
		// Retrieve
		this.title = intent.getStringExtra("GOOGLE_CAST_ITEM_TITLE")
		this.subtitle = intent.getStringExtra("GOOGLE_CAST_ITEM_SUBTITLE")
		this.uri = intent.getParcelableExtra("GOOGLE_CAST_ITEM_URI")!!

	}

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun put(intent :Intent) {
		// Put
		intent.putExtra("GOOGLE_CAST_ITEM_TITLE", this.title)
		intent.putExtra("GOOGLE_CAST_ITEM_SUBTITLE", this.subtitle)
		intent.putExtra("GOOGLE_CAST_ITEM_URI", this.uri)
	}
}
