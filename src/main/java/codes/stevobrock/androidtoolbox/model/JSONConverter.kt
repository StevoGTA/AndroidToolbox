package codes.stevobrock.androidtoolbox.model

import com.squareup.moshi.Moshi
import java.lang.Exception

//----------------------------------------------------------------------------------------------------------------------
class JSONConverter<T : Any>(private val clazz :Class<T>) {

	// Methods
	//--------------------------------------------------------------------------------------------------------------
	fun fromJson(string :String) :T { return Moshi.Builder().build().adapter<T>(this.clazz).fromJson(string)!! }

	//--------------------------------------------------------------------------------------------------------------
	fun toJson(t :T) :String { return Moshi.Builder().build().adapter<T>(this.clazz).toJson(t) }
}

//----------------------------------------------------------------------------------------------------------------------
inline fun <reified T : Any> JSONConverter() = JSONConverter(T::class.java)
