package codes.stevobrock.androidtoolbox.sqlite

//----------------------------------------------------------------------------------------------------------------------
class SQLiteOrderBy {

	// Types
	enum class Order {
		ASCENDING,
		DESCENDING,
	}

	// Properties
	val	string :String

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(table :SQLiteTable? = null, tableColumn :SQLiteTableColumn, order :Order = Order.ASCENDING) {
		// Setup
		this.string =
				" ORDER BY " +
						(if (table != null) "`$table!.name`.`$tableColumn.name`" else "`$tableColumn.name`") +
						(if (order == Order.ASCENDING) "ASC" else "DESC")
	}
}
