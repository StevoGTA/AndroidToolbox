package codes.stevobrock.androidtoolbox.extensions

//----------------------------------------------------------------------------------------------------------------------
val String.asPhoneNumberForDisplay :String get() {
												// Setup
												val test = "$this          "
												val areaCode = test.subSequence(0, 3).filter() { it.isDigit() }
												val prefix = test.subSequence(3, 6).filter() { it.isDigit() }
												val suffix = test.subSequence(6, 10).filter() { it.isDigit() }

												// Check results
												return when {
													areaCode.isEmpty() ->   ""
													prefix.isEmpty() ->     "($areaCode"
													suffix.isEmpty() ->     "($areaCode) $prefix"
													else ->                 "($areaCode) $prefix-$suffix"
												}
											}
