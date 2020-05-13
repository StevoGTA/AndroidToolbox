package codes.stevobrock.androidtoolbox.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

//----------------------------------------------------------------------------------------------------------------------
interface BitmapCache {

	// Methods
	fun retrieveBitmap(identifier :String) :Bitmap?
}

//----------------------------------------------------------------------------------------------------------------------
open class MemoryBitmapCache(sizeLimit :Long? = null) : MemoryDataCache(sizeLimit), BitmapCache {

	// Methods
	//------------------------------------------------------------------------------------------------------------------
	override fun retrieveBitmap(identifier :String) :Bitmap? {
		// Try to retrieve data
		val data = retrieveData(identifier) ?: return null

		return BitmapFactory.decodeByteArray(data, 0, data.size)
	}
}

//----------------------------------------------------------------------------------------------------------------------
open class FilesystemBitmapCache : FilesystemDataCache, BitmapCache {

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(folder : File, sizeLimit :Long? = null) : super(folder, sizeLimit)
	constructor(context : Context, folderName :String, sizeLimit :Long? = null) : super(context, folderName, sizeLimit)

	// Methods
	//------------------------------------------------------------------------------------------------------------------
	override fun retrieveBitmap(identifier :String) : Bitmap? {
		// Try to retrieve data
		val data = retrieveData(identifier) ?: return null

		return BitmapFactory.decodeByteArray(data, 0, data.size)
	}
}
