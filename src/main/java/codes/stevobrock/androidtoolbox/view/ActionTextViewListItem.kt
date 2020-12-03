package codes.stevobrock.androidtoolbox.view

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

//----------------------------------------------------------------------------------------------------------------------
class ActionTextViewListItem : ActionListItem {

	//------------------------------------------------------------------------------------------------------------------
	// Properties
	override	val	view :View
		get() {
			// Do super
			val view = super.view

			// Set Text(s)
			this.titleTextView = view.findViewById(this.titleTextViewResourceID)
			this.titleTextView!!.text = this.title
			if (this.subtitleTextViewResourceID > 0) {
				this.subtitleTextView = view.findViewById(this.subtitleTextViewResourceID)
				this.subtitleTextView!!.text = this.subtitle
			}

			return view
		}

	private var subtitleTextViewResourceID = 0
	private var titleTextViewResourceID :Int
	private var subtitle :String? = null
	private var title :String
	private var subtitleTextView :TextView? = null
	private var titleTextView :TextView? = null

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(layoutInflater :LayoutInflater, resourceID :Int, titleTextViewResourceID :Int, title :String,
			subtitleTextViewResourceID :Int, subtitle :String?, listener :ActionListItem.Listener) :
			super(layoutInflater, resourceID, listener) {
		// Store
		this.titleTextViewResourceID = titleTextViewResourceID
		this.subtitleTextViewResourceID = subtitleTextViewResourceID
		this.title = title
		this.subtitle = subtitle
	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(layoutInflater :LayoutInflater, resourceID :Int, titleTextViewResourceID :Int, title :String,
			listener :ActionListItem.Listener) : super(layoutInflater, resourceID, listener) {
		// Store
		this.titleTextViewResourceID = titleTextViewResourceID
		this.title = title
	}

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun setTitle(title :String) { this.title = title }

	//------------------------------------------------------------------------------------------------------------------
	fun setSubtitle(subtitle :String?) { this.subtitle = subtitle }
}
