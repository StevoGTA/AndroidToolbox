package codes.stevobrock.androidtoolbox.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.Nullable

//----------------------------------------------------------------------------------------------------------------------
class Spacer : LinearLayout {

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(context :Context) : super(context)
	constructor(context :Context, @Nullable attributeSet :AttributeSet) : super(context, attributeSet)
	constructor(context :Context, @Nullable attributeSet :AttributeSet, defStyleAttr :Int) :
		super(context, attributeSet, defStyleAttr)
	constructor(context :Context, attributeSet :AttributeSet, defStyleAttr :Int, defStyleRes :Int) :
		super(context, attributeSet, defStyleAttr, defStyleRes)
}
