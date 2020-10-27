package codes.stevobrock.androidtoolbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import codes.stevobrock.androidtoolbox.view.ObservableWebView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*

//----------------------------------------------------------------------------------------------------------------------
open class WebViewBottomSheetDialogFragment(val layoutID :Int, val webViewID :Int) :
		BottomSheetDialogFragment(setOf(Options.EXPANDED_AT_START)) {

	// Properties
						var closeProc :() -> Unit = {}

			lateinit    var webView :ObservableWebView

	private             var currentWebViewScrollY = 0

	// Fragment methods
	//------------------------------------------------------------------------------------------------------------------
	override fun onCreateView(inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?) : View? {
		// Setup UI
		val view = inflater.inflate(this.layoutID, container, false)

		this.webView = view.findViewById(this.webViewID)
		this.webView.onScrollChangedProc = { _, currentVerticalScroll, _, _ ->
			// Store
			this.currentWebViewScrollY = currentVerticalScroll
		}

//		BottomSheetBehavior.from(view).let { bottomSheetBehavior ->
//			// Add callback
//			bottomSheetBehavior.addBottomSheetCallback(object :BottomSheetBehavior.BottomSheetCallback() {
//				override fun onSlide(bottomSheet :View, slideOffset :Float) {}
//				override fun onStateChanged(bottomSheet :View, newState :Int) {
//					// Check state
//					if ((newState == STATE_DRAGGING) && (currentWebViewScrollY > 0))
//						// Not scrolled to the top
//						bottomSheetBehavior.state = STATE_EXPANDED
//					else if (newState == STATE_HIDDEN)
//						// Close
//						closeProc()
//				}
//			})
//		}

		return view
	}
}
