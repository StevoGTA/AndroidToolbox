package codes.stevobrock.androidtoolbox.extensions

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

//----------------------------------------------------------------------------------------------------------------------
// Date extension

//----------------------------------------------------------------------------------------------------------------------
fun dateFromISO8601(string :String?) :Date? {
	// Check for null
	if (string == null) return null

	// Catch errors
	try {
		// Try to convert
		return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszz").parse(string)
	} catch (e :Exception) {
		// Invalid string
		return null
	}
}

//----------------------------------------------------------------------------------------------------------------------
fun dateFromRFC3339(string :String?) :Date? {
	// Check for null
	if (string == null) return null

	// Setup
	val formats =
				arrayListOf<String>(
					"yyyy-MM-dd'T'HH:mm:ssX",
					"yyyy-MM-dd'T'HH:mm:sszzz",
				)
	for (format in formats) {
		// Catch errors
		try {
			// Try to convert
			return SimpleDateFormat(format).parse(string)
		} catch (exception :Exception) {}
	}

	return null
}

//----------------------------------------------------------------------------------------------------------------------
fun dateFromRFC3339Extended(string :String?) :Date? {
	// Check for null
	if (string == null) return null

	// Setup
	val formats =
				arrayListOf<String>(
					"yyyy-MM-dd'T'HH:mm:ss.SX",
					"yyyy-MM-dd'T'HH:mm:ss.SSX",
					"yyyy-MM-dd'T'HH:mm:ss.SSSX",
					"yyyy-MM-dd'T'HH:mm:ss.SSSSX",
					"yyyy-MM-dd'T'HH:mm:ss.SSSSSX",
					"yyyy-MM-dd'T'HH:mm:ss.SSSSSSX",
					"yyyy-MM-dd'T'HH:mm:ss.SSSSSSSX",
					"yyyy-MM-dd'T'HH:mm:ss.SSSSSSSzzz",
				)
	for (format in formats) {
		// Catch errors
		try {
			// Try to convert
			return SimpleDateFormat(format).parse(string)
		} catch (exception :Exception) {}
	}

	return null
}

//----------------------------------------------------------------------------------------------------------------------
val Date.iso8601 :String get() { return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszz").format(this) }

//----------------------------------------------------------------------------------------------------------------------
val Date.rfc3339 :String get() { return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz").format(this) }

//----------------------------------------------------------------------------------------------------------------------
val Date.rfc3339Extended :String get() {
	// Return date
	return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSzzz").format(this)
}
