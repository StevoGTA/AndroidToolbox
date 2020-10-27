package codes.stevobrock.androidtoolbox.view

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

//----------------------------------------------------------------------------------------------------------------------
class ObservableWebView : WebView {

	// Properties
	var onScrollChangedProc
				:(currentHorizontalScroll: Int, currentVerticalScroll: Int, oldHorizontalScroll: Int,
						oldCurrentVerticalScroll: Int) -> Unit = { _,_,_,_ -> }

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(context :Context?) : super(context)
	constructor(context :Context?, attrs :AttributeSet?) : super(context, attrs)
	constructor(context :Context?, attrs :AttributeSet?, defStyle :Int) : super(context, attrs, defStyle)

	// WebView methods
	//------------------------------------------------------------------------------------------------------------------
	override fun onScrollChanged(currentHorizontalScroll :Int, currentVerticalScroll :Int, oldHorizontalScroll :Int,
			oldCurrentVerticalScroll :Int) {
		// Do super
		super.onScrollChanged(currentHorizontalScroll, currentVerticalScroll, oldHorizontalScroll,
				oldCurrentVerticalScroll)

		// Call
		onScrollChangedProc(currentHorizontalScroll, currentVerticalScroll, oldHorizontalScroll,
				oldCurrentVerticalScroll)
	}
}
