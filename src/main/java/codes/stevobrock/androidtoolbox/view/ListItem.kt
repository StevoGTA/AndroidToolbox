package codes.stevobrock.androidtoolbox.view

import android.view.LayoutInflater
import android.view.View

//----------------------------------------------------------------------------------------------------------------------
abstract class ListItem(private val layoutInflater :LayoutInflater, private val resourceID :Int) {

	// Properties
	open	val	view :View
					get() = this.layoutInflater.inflate(this.resourceID, null)

			val	isEnabled :Boolean
					get() = true
}
