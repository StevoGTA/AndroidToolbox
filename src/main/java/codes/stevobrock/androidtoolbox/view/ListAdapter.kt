package codes.stevobrock.androidtoolbox.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import java.util.*

//----------------------------------------------------------------------------------------------------------------------
class ListAdapter(context :Context, listItemsArrayList :ArrayList<ListItem>) :
		ArrayAdapter<ListItem>(context, 0, listItemsArrayList) {

	// ArrayAdapter methods
	//------------------------------------------------------------------------------------------------------------------
	override fun getView(position :Int, view :View?, parentViewGroup :ViewGroup) :View {
		// Get view
		return getItem(position)!!.view
	}

	//------------------------------------------------------------------------------------------------------------------
	override fun isEnabled(position :Int) :Boolean {
		// Get is enabled
		return getItem(position)!!.isEnabled
	}
}
