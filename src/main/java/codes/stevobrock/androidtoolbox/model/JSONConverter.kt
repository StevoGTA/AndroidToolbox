package codes.stevobrock.androidtoolbox.model

import com.squareup.moshi.Moshi
import okio.BufferedSource
import okio.Utf8
import java.lang.Exception

//----------------------------------------------------------------------------------------------------------------------
class JSONConverter<T : Any>(private val clazz :Class<T>) {

	// Methods
	//--------------------------------------------------------------------------------------------------------------
	fun fromJson(byteArray :ByteArray) :T { return fromJson(String(byteArray)) }

	//--------------------------------------------------------------------------------------------------------------
	fun fromJson(string :String) :T { return Moshi.Builder().build().adapter<T>(this.clazz).fromJson(string)!! }

	//--------------------------------------------------------------------------------------------------------------
	fun toJsonString(t :T) :String { return Moshi.Builder().build().adapter<T>(this.clazz).toJson(t) }

	//--------------------------------------------------------------------------------------------------------------
	fun toJsonByteArray(t :T) :ByteArray { return toJsonString(t).toByteArray() }
}

//----------------------------------------------------------------------------------------------------------------------
inline fun <reified T : Any> JSONConverter() = JSONConverter(T::class.java)
