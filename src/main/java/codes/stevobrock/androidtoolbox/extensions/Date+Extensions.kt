package codes.stevobrock.androidtoolbox.extensions

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

//----------------------------------------------------------------------------------------------------------------------
// Date extension

//----------------------------------------------------------------------------------------------------------------------
fun dateFromISO8601(string :String) :Date? {
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
fun dateFromRFC3339(string :String) :Date? {
	// Catch errors
	try {
		// Try to convert
		return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz").parse(string)
	} catch (e :Exception) {
		// Invalid string
		return null
	}
}

//----------------------------------------------------------------------------------------------------------------------
fun dateFromRFC3339Extended(string :String) :Date? {
	// Catch errors
	try {
		// Try to convert
		return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSzzz").parse(string)
	} catch (e :Exception) {
		// Invalid string
		return null
	}
}

//----------------------------------------------------------------------------------------------------------------------
fun Date.iso8601() :String { return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszz").format(this) }

//----------------------------------------------------------------------------------------------------------------------
fun Date.rfc3339() :String { return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzz").format(this) }

//----------------------------------------------------------------------------------------------------------------------
fun Date.rfc3339Extended() :String {
	// Return date
	return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSzzz").format(this)
}
