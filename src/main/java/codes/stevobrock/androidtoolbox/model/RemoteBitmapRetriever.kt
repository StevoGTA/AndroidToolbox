package codes.stevobrock.androidtoolbox.model

import android.graphics.Bitmap
import android.util.Size

//----------------------------------------------------------------------------------------------------------------------
interface RemoteBitmapRetriever {

	// Methods
	fun bitmap(item :Any, size :Size, aspectFit :Boolean) :Bitmap?
	fun retrieveRemoteBitmap(item :Any, size :Size, aspectFit :Boolean,
			completionProc :(bitmap :Bitmap?, exception :Exception?) -> Unit) :String?
	fun cancelRetrieveRemoteBitmap(identifier :String)
}
