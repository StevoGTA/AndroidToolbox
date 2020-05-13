package codes.stevobrock.androidtoolbox.model

import java.io.File
import java.io.IOException

//----------------------------------------------------------------------------------------------------------------------
class FileExtendedAttributes(private val file :File) {

	// External methods
	//------------------------------------------------------------------------------------------------------------------
	@Throws(IOException::class)
	private external fun get(path :String, name :String) :String?

	//------------------------------------------------------------------------------------------------------------------
	@Throws(IOException::class)
	private external fun set(path :String, name :String, value :String)

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun getString(name :String) :String? { return get(this.file.path, name) }

	//------------------------------------------------------------------------------------------------------------------
	fun set(name :String, value :String) { set(this.file.path, name, value) }

	// Companion object
	//------------------------------------------------------------------------------------------------------------------
	companion object { init { System.loadLibrary("native-lib") } }
}
