package codes.stevobrock.androidtoolbox.extensions

import android.util.Base64
import java.nio.ByteBuffer
import java.util.*

//----------------------------------------------------------------------------------------------------------------------
val UUID.base64EncodedString :String get() {
											// Setup
											val	byteBuffer = ByteBuffer.wrap(ByteArray(16))
											byteBuffer.putLong(this.mostSignificantBits)
											byteBuffer.putLong(this.leastSignificantBits)

											val	byteArray = byteBuffer.array()

											return Base64.encodeToString(byteArray, Base64.DEFAULT)
										}
