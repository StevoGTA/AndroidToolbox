package codes.stevobrock.androidtoolbox.sqlite

import codes.stevobrock.androidtoolbox.extensions.forEachChunk

//----------------------------------------------------------------------------------------------------------------------
class SQLiteWhere {

	// Properties
	var	string :String
		private set
	var	values :ArrayList<Any>
		private set

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(table :SQLiteTable? = null, tableColumn :SQLiteTableColumn, comparison :String = "=",
			value :Any? = null) {
		// Setup
		this.string = " WHERE " + (if (table != null) "`$table!.name`.`$tableColumn.name`" else "`$tableColumn.name`")
		this.values = ArrayList()
		append(comparison, value)
	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(table :SQLiteTable? = null, tableColumn :SQLiteTableColumn, values :List<Any>) {
		// Setup
		this.string =
				" WHERE " + (if (table != null) "`$table!.name`.`$tableColumn.name`" else "`$tableColumn.name`") +
						" IN (" + SQLiteWhere.variablePlaceholder + ")"
		this.values = ArrayList(values)
	}

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun forEachValueGroup(chunkSize :Int, proc :(string :String, values :List<Any>) -> Unit) {
		// Chunk values
		this.values.forEachChunk(chunkSize) {
			// Call proc
			proc(
					this.string.replace(SQLiteWhere.variablePlaceholder,
							Array(it.count()) { "?" }.joinToString(",")),
					it)
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
		this.values = ArrayList(values)

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
			this.values.add(value)
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
