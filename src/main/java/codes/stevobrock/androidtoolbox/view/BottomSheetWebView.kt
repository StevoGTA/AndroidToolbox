package codes.stevobrock.androidtoolbox.view

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

//----------------------------------------------------------------------------------------------------------------------
open class BottomSheetWebView(context :Context, layoutID :Int, observableWebViewID :Int,
		val options :Set<Options> = HashSet()) : FrameLayout(context) {

	// Types
	enum class Options {
		EXPANDED_AT_START
	}

	// Properties
	private val observableWebView :ObservableWebView
	private val bottomSheetDialog :BottomSheetDialog

	private var currentWebViewScrollY = 0

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	init {
		// Setup UI
		inflate(context, layoutID, this)

		this.observableWebView = findViewById(observableWebViewID)
		this.observableWebView.onScrollChangedProc = { _, currentVerticalScroll, _, _ ->
			// Store
			this.currentWebViewScrollY = currentVerticalScroll
		}

		this.bottomSheetDialog =
					BottomSheetDialog(context).apply {
						// Check options
						if (options.contains(Options.EXPANDED_AT_START))
							// Start expanded
							behavior.state = BottomSheetBehavior.STATE_EXPANDED
					}
		bottomSheetDialog.setContentView(this)
        (parent as? View)?.let { view ->
            BottomSheetBehavior.from(view).let { behaviour ->
                behaviour.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if ((newState == BottomSheetBehavior.STATE_DRAGGING) && (currentWebViewScrollY > 0)) {
                            // Check if Webview can scroll up or not
                            behaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                        } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            // Dismiss
                            bottomSheetDialog.dismiss()
                        }
                    }
                })
            }
        }
	}

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun showWithURL(url :String) {
		// Setup URL
		this.observableWebView.loadUrl(url)

		// Show
		this.bottomSheetDialog.show()
	}

	//------------------------------------------------------------------------------------------------------------------
	fun dismiss() { this.bottomSheetDialog.dismiss() }
}
