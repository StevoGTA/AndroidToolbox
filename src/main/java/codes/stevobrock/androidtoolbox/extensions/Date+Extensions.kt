package codes.stevobrock.androidtoolbox.extensions

import java.text.SimpleDateFormat
import java.util.*

////----------------------------------------------------------------------------------------------------------------------
//fun Date(standardizedString :String) :Date? {
//	return null
//}

fun dateFromStandardized(string :String) :Date? {
	return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+00:00'").parse(string)
//	return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ':00'").parse(string)
}
