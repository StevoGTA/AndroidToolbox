package codes.stevobrock.androidtoolbox.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

//----------------------------------------------------------------------------------------------------------------------
// View extension

//----------------------------------------------------------------------------------------------------------------------
fun View.showKeyboard() {
	// Request focus
	this.requestFocus()

	// Show keyboard
	val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
	inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

//----------------------------------------------------------------------------------------------------------------------
fun View.hideKeyboard() {
	// Hide keyboard
	val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
	inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}
