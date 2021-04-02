package codes.stevobrock.androidtoolbox.sqlite

//----------------------------------------------------------------------------------------------------------------------
class SQLiteTrigger {

	// Properties
	private	val	updateTableColumn :SQLiteTableColumn
	private	val	comparisonTableColumn :SQLiteTableColumn

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(updateTableColumn :SQLiteTableColumn, comparisonTableColumn :SQLiteTableColumn) {
		// Store
		this.updateTableColumn = updateTableColumn
		this.comparisonTableColumn = comparisonTableColumn
	}

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun string(table :SQLiteTable) :String {
		// Return string
		return "CREATE TRIGGER `${table.name}-${this.updateTableColumn.name}Trigger`" +
				" AFTER UPDATE ON ${table.name}" +
				" FOR EACH ROW" +
				" BEGIN UPDATE ${table.name}" +
				" SET $this.updateTableColumn.name=$this.updateTableColumn.defaultValue!" +
				" WHERE $this.comparisonTableColumn.name=NEW.$this.comparisonTableColumn.name;" +
				" END"
	}
}
