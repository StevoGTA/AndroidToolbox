package codes.stevobrock.androidtoolbox.sqlite

import codes.stevobrock.androidtoolbox.extensions.forEachChunk

//----------------------------------------------------------------------------------------------------------------------
class SQLiteWhere {

	// Properties
	var	string :String
		private set
	var	values :ArrayList<ArrayList<Any>>?
		private set

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(table :SQLiteTable? = null, tableColumn :SQLiteTableColumn, comparison :String = "=",
			value :Any? = null) {
		// Setup
		this.string = " WHERE " + (if (table != null) "`$table!.name`.`$tableColumn.name`" else "`$tableColumn.name`")
		this.values = null

		append(comparison, value)
	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(table :SQLiteTable? = null, tableColumn :SQLiteTableColumn, values :List<Any>) {
		// Setup
		this.string =
				" WHERE " + (if (table != null) "`$table!.name`.`$tableColumn.name`" else "`$tableColumn.name`") +
						" IN (" + SQLiteWhere.variablePlaceholder + ")"

		this.values = ArrayList()
		this.values!!.add(ArrayList(values))
	}

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun forEachValueGroup(groupSize :Int, proc :(string :String, values :List<Any>) -> Unit) {
		// Check if need to group
		if (!this.string.contains(variablePlaceholder))
			// Perform
			proc(this.string, this.values ?: arrayListOf())
		else {
			// Group
			var	preValueGroupValues = ArrayList<Any>()
			var	valueGroup = ArrayList<Any>()
			var postValueGroupValues = ArrayList<Any>()
			(this.values ?: arrayListOf()).forEach() {
				// Check count
				if (it.size == 1) {
					// Single value
					if (valueGroup.isEmpty())
						// Pre
						preValueGroupValues.addAll(it)
					else
						// Post
						postValueGroupValues.addAll(it)
				} else
					// Value group
					valueGroup = it
			}

			// Check if need to group
			val	allValues = preValueGroupValues + valueGroup + postValueGroupValues
			if (allValues.size <= groupSize)
				// Can perform as a single group
				proc(
						this.string
								.replace(variablePlaceholder,
										Array(allValues.size.coerceAtLeast(1)) { "?" }
								.joinToString(",")),
						allValues)
			else
				// Must perform in groups
				valueGroup.forEachChunk(groupSize - preValueGroupValues.size - postValueGroupValues.size) {
					// Setup
					val	values = preValueGroupValues + it + postValueGroupValues

					// Call proc
					proc(
							this.string
									.replace(variablePlaceholder, Array(it.size) { "?" }
									.joinToString(",")),
							values)
				}
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	fun and(table :SQLiteTable? = null, tableColumn :SQLiteTableColumn, comparison :String = "=", value :Any)
			:SQLiteWhere {
		// Append
		this.string += " AND " + (if (table != null) "`$table!.name`.`$tableColumn.name`" else "`$tableColumn.name`")
		append(comparison, value)

		return this
	}

	//------------------------------------------------------------------------------------------------------------------
	fun and(table :SQLiteTable? = null, tableColumn :SQLiteTableColumn, values :List<Any>) :SQLiteWhere {
		// Append
		this.string +=
				" WHERE " + (if (table != null) "`$table!.name`.`$tableColumn.name=`" else "`$tableColumn.name`") +
						" IN (" + SQLiteWhere.variablePlaceholder + ")"

		if (this.values == null) this.values = ArrayList()
		this.values!!.add(ArrayList(values))

		return this
	}

	//------------------------------------------------------------------------------------------------------------------
	fun or(table :SQLiteTable? = null, tableColumn :SQLiteTableColumn, comparison :String = "=", value :Any)
			:SQLiteWhere {
		// Append
		this.string += " OR " + (if (table != null) "`$table!.name`.`$tableColumn.name`" else "`$tableColumn.name`")
		append(comparison, value)

		return this
	}

	// Private methods
	//------------------------------------------------------------------------------------------------------------------
	private fun append(comparison :String, value :Any?) {
		// Check value type
		if (value == null) {
			// Value is null
			if (comparison == "=") {
				// IS NULL
				this.string += " IS NULL"
			} else if (comparison == "!=") {
				// IS NOT NULL
				this.string += " IS NOT NULL"
			} else {
				// Unsupported null comparison
				throw UnsupportedNullComparisonException()
			}
		} else {
			// Actual value
			this.string += " $comparison ?"

			if (this.values == null) this.values = ArrayList()
			this.values!!.add(arrayListOf(value))
		}
	}

	// Private classes
	//------------------------------------------------------------------------------------------------------------------
	private class UnsupportedNullComparisonException : Exception()

	// Companion object
	//------------------------------------------------------------------------------------------------------------------
	companion object {

		// Values
		val	variablePlaceholder = "##VARIABLEPLACEHOLDER##"
	}
}
