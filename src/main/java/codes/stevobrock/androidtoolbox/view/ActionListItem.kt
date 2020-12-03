package codes.stevobrock.androidtoolbox.view

import android.view.LayoutInflater
import android.view.View

//----------------------------------------------------------------------------------------------------------------------
abstract class ActionListItem(layoutInflater :LayoutInflater, resourceID :Int, private val listener :Listener) :
		ListItem(layoutInflater, resourceID) {

	//------------------------------------------------------------------------------------------------------------------
	// Interfaces
	interface Listener {
		fun onSelect(actionListItem :ActionListItem?)
	}

	//------------------------------------------------------------------------------------------------------------------
	// Properties
	override	val	view :View
						get() {
							// Do super
							val view = super.view
							view.setOnClickListener { this.listener.onSelect(this) }

							return view
						}
}
