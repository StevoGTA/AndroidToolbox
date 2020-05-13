package codes.stevobrock.androidtoolbox.model

import android.graphics.Bitmap

//----------------------------------------------------------------------------------------------------------------------
interface RemoteBitmapRetriever {

	// Methods
	fun queryRemoteBitmap(item :Any, completionProc :(bitmap :Bitmap) -> Unit)
	fun cancelQueryRemoteBitmap(item :Any)
}
