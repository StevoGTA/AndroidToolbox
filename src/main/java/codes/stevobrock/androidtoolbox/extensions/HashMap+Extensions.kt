package codes.stevobrock.androidtoolbox.extensions

//----------------------------------------------------------------------------------------------------------------------
fun <T, U> HashMap<T, U>.update(key :T, value :U?) {
	// Check if setting or removing
	if (value != null)
		// Setting
		this[key] = value
	else
		// Removing
		remove(key)
}

//----------------------------------------------------------------------------------------------------------------------
fun <T, U> HashMap<T, Set<U>>.appendSetValue(key :T, value :U) {
	// Check if already have key
	if (contains(key))
		// Add value
		this[key]!!.plus(value)
	else
		// First value
		this[key] = setOf(value)
}
