package codes.stevobrock.androidtoolbox.view

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import codes.stevobrock.androidtoolbox.model.RemoteBitmapRetriever

//----------------------------------------------------------------------------------------------------------------------
class RemoteBitmapImageView : androidx.appcompat.widget.AppCompatImageView {

	// Properties
	private				var	t :Any? = null
	private	lateinit	var	remoteBitmapRetriever :RemoteBitmapRetriever

	// Lifecycle methods
	constructor(context :Context?) : super(context)
	constructor(context :Context?, attributeSet :AttributeSet?) : super(context, attributeSet)
	constructor(context :Context?, attributeSet :AttributeSet?, defaultStyleAttribute :Int) :
			super(context, attributeSet, defaultStyleAttribute)

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun setup(t :Any, remoteBitmapRetriever :RemoteBitmapRetriever, defaultBitmap :Bitmap? = null) {
		// Cleanup if necessary
		cleanup()

		// Store
		this.t = t
		this.remoteBitmapRetriever = remoteBitmapRetriever

		// Setup UI
		setImageBitmap(defaultBitmap)

		// Query bitmap
		this.remoteBitmapRetriever.queryRemoteBitmap(this.t!!) {
			// Update UI
			setImageBitmap(it)
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	fun cleanup() {
		// Check if we have a thing
		if (this.t != null) {
			// Cancel any remote bitmap query that is in-flight.
			this.remoteBitmapRetriever.cancelQueryRemoteBitmap(this.t!!)
			this.t = null
		}
	}
}
