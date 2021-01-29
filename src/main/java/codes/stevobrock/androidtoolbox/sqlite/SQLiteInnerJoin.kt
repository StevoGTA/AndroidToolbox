package codes.stevobrock.androidtoolbox.sqlite

//----------------------------------------------------------------------------------------------------------------------
class SQLiteInnerJoin {

	// Properties
	var	string :String
		private set

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(table :SQLiteTable, tableColumn :SQLiteTableColumn, otherTable :SQLiteTable,
			otherTableColumn :SQLiteTableColumn? = null) {
		// Setup
		this.string = ""
		and(table, tableColumn, otherTable, otherTableColumn)
	}

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun and(table :SQLiteTable, tableColumn :SQLiteTableColumn, otherTable :SQLiteTable,
			otherTableColumn :SQLiteTableColumn? = null) :SQLiteInnerJoin {
		// Append
		val otherTableColumnName = otherTableColumn?.name ?: tableColumn.name
		this.string +=
				" INNER JOIN `$otherTable.name` ON " +
						"`$otherTable.name`.`$otherTableColumnName` = " +
						"`$table.name`.`$tableColumn.name`"

		return this
	}
}
