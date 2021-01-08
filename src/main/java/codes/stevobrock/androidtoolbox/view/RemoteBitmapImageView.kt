package codes.stevobrock.androidtoolbox.view

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Size
import codes.stevobrock.androidtoolbox.model.RemoteBitmapRetriever

//----------------------------------------------------------------------------------------------------------------------
class RemoteBitmapImageView : androidx.appcompat.widget.AppCompatImageView {

	// Properties
	private	var	remoteBitmapRetriever :RemoteBitmapRetriever? = null
	private	var	identifier :String? = null

	// Lifecycle methods
	constructor(context :Context) : super(context)
	constructor(context :Context, attributeSet :AttributeSet?) : super(context, attributeSet)
	constructor(context :Context, attributeSet :AttributeSet?, defaultStyleAttribute :Int) :
			super(context, attributeSet, defaultStyleAttribute)

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun setup(item :Any, remoteBitmapRetriever :RemoteBitmapRetriever, defaultBitmap :Bitmap? = null,
			aspectFit :Boolean = true) {
		// Cleanup if necessary
		cleanup()

		// Setup
		val	bitmap = remoteBitmapRetriever.bitmap(item, Size(0, 0), aspectFit)
		if (bitmap != null)
			// Have bitmap
			setImageBitmap(bitmap)
		else {
			// Store
			this.remoteBitmapRetriever = remoteBitmapRetriever

			// Setup UI
			setImageBitmap(defaultBitmap)

			// Retrieve bitmap
			this.identifier =
					this.remoteBitmapRetriever!!.retrieveRemoteBitmap(item, Size(0, 0), aspectFit)
							{ bitmap, exception ->
								// Note that we are loaded
								this.identifier = null

								// Update UI
								setImageBitmap(bitmap)
							}
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	fun cleanup() {
		// Check if we have a thing
		if (this.identifier != null) {
			// Cancel in-flight
			this.remoteBitmapRetriever!!.cancelRetrieveRemoteBitmap(this.identifier!!)
			this.identifier = null
		}
	}
}
