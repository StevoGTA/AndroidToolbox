package codes.stevobrock.androidtoolbox.fragment

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

//----------------------------------------------------------------------------------------------------------------------
open class BottomSheetDialogFragment(val options :Set<Options> = HashSet()) : BottomSheetDialogFragment() {

	// Types
	enum class Options {
		EXPANDED_AT_START
	}

	// BottomSheetDialogFragment methods
	//------------------------------------------------------------------------------------------------------------------
	override fun onCreateDialog(savedInstanceState: Bundle?) :Dialog {
		// Setup
		val options = this.options

		// Return dialog
		return BottomSheetDialog(requireContext(), theme).apply {
			// Check options
			if (options.contains(Options.EXPANDED_AT_START))
				// Start expanded
				behavior.state = BottomSheetBehavior.STATE_EXPANDED
		}
	}
}
