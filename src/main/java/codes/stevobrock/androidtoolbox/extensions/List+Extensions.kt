package codes.stevobrock.androidtoolbox.extensions

//----------------------------------------------------------------------------------------------------------------------
// List extension

//----------------------------------------------------------------------------------------------------------------------
fun <T> List<T>.forEachChunk(chunkSize :Int, proc :(ts :List<T>) -> Unit) {
		// Check count
		if (count() == 0)
			// Nothing to do
			return
		else if (count() <= chunkSize)
			// All in one chunk
			proc(this)
		else
			// Chunk the array and call the proc on the new arrays
			chunked(chunkSize).forEach() { proc(it) }
}
