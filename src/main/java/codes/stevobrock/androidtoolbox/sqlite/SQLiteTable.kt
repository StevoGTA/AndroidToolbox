package codes.stevobrock.androidtoolbox.sqlite

import codes.stevobrock.androidtoolbox.extensions.forEachChunk
import codes.stevobrock.androidtoolbox.sqlite.SQLiteTable.Options.Companion.WITHOUT_ROWID

//----------------------------------------------------------------------------------------------------------------------
// SQLiteTableColumn
val	SQLiteTableColumn.createString :String get() {
			// Compose column string
			var	string = "$this.name "

			// Check kind
			when (this.kind) {
				SQLiteTableColumn.Kind.INTEGER ->
					// Integer
					string += "INTEGER"

				SQLiteTableColumn.Kind.REAL ->
					// Real
					string += "REAL"

				SQLiteTableColumn.Kind.TEXT,
				SQLiteTableColumn.Kind.DATE_ISO8601_FRACTIONAL_SECONDS_AUTO_SET,
				SQLiteTableColumn.Kind.DATE_ISO8601_FRACTIONAL_SECONDS_AUTO_UPDATE ->
					// Text
					string += "TEXT"

				SQLiteTableColumn.Kind.BLOB ->
					// Blob
					string += "BLOB"
			}

			// Handle options
			if ((this.options and SQLiteTableColumn.Options.PRIMARY_KEY) != 0)
				string += " PRIMARY KEY"
			if ((this.options and SQLiteTableColumn.Options.AUTO_INCREMENT) != 0)
				string += " AUTOINCREMENT"
			if ((this.options and SQLiteTableColumn.Options.NOT_NULL) != 0)
				string += " NOT NULL"
			if ((this.options and SQLiteTableColumn.Options.UNIQUE) != 0)
				string += " UNIQUE"
			if ((this.options and SQLiteTableColumn.Options.CHECK) != 0)
				string += " CHECK"

			if (this.defaultValue != null) {
				// Default
				string += " DEFAULT ($this.defaultValue)"
			}

			return string
		}

//----------------------------------------------------------------------------------------------------------------------
// Types
typealias SQLiteTableAndTableColumn = Pair<SQLiteTable, SQLiteTableColumn>
typealias SQLiteTableColumnAndValue = Pair<SQLiteTableColumn, Any>

//----------------------------------------------------------------------------------------------------------------------
// SQLiteTable
class SQLiteTable {

	// Options
	class Options {
		companion object {
			// Values
			const	val	NONE = 0x00
			const	val	WITHOUT_ROWID = 0x01
		}
	}

	// Properties
	var	name :String
		private set

	private	val	options :Int
	private	val	statementPerformer :SQLiteStatementPerformer

	private	var	tableColumns :ArrayList<SQLiteTableColumn>
	private	var	tableColumnsMap = HashMap<String, SQLiteTableColumn>()
	private	var	tableColumnReferenceMap = HashMap<String, SQLiteTableColumnReference>()

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(name :String, options :Int, tableColumns :List<SQLiteTableColumn>,
			references :List<SQLiteTableColumnReference> = listOf(), statementPerformer :SQLiteStatementPerformer) {
		// Store
		this.name = name
		this.options = options
		this.tableColumns = ArrayList(tableColumns)
		this.statementPerformer = statementPerformer

		// Setup
		tableColumns.forEach() { this.tableColumnsMap[it.name + "TableColumn"] = it }
		references.forEach() { this.tableColumnReferenceMap[it.first.name] = it }
	}

	// Property methods
	//------------------------------------------------------------------------------------------------------------------
	operator fun get(key :String) :SQLiteTableColumn { return this.tableColumnsMap[key]!! }

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun create(ifNotExists :Boolean = true) {
		// Setup
		val	columnInfos =
					this.tableColumns.map() {
						// Start with create string
						var	columnInfo = it.createString

						// Add references if applicable
						val tableColumnReference = this.tableColumnReferenceMap[it.name]
						if (tableColumnReference != null)
							// Add reference
							columnInfo +=
									" REFERENCES " + tableColumnReference.second.name +
											"(" + tableColumnReference.third.name + ") ON UPDATE CASCADE"

						columnInfo
					}

		// Create
		val	string =
					"CREATE TABLE" + (if (ifNotExists) " IF NOT EXISTS" else "") + " `$this.name`" +
							" (" + columnInfos.joinToString() + ")" +
							(if ((this.options or WITHOUT_ROWID) != 0) " WITHOUT ROWID" else "")
		this.statementPerformer.addToTransactionOrPerform(string)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun rename(name :String) {
		// Perform
		this.statementPerformer.addToTransactionOrPerform("ALTER TABLE `$this.name` RENAME TO `$name`")

		// Update
		this.name = name
	}

	//------------------------------------------------------------------------------------------------------------------
	fun add(tableColumn :SQLiteTableColumn) {
		// Perform
		this.statementPerformer.addToTransactionOrPerform(
				"ALTER TABLE `$this.name` ADD COLUMN $tableColumn.createString")

		// Update
		this.tableColumns.add(tableColumn)
		this.tableColumnsMap[tableColumn.name + "TableColumn"] = tableColumn
	}

	//------------------------------------------------------------------------------------------------------------------
	fun add(trigger :SQLiteTrigger) {
		// Perform
		this.statementPerformer.addToTransactionOrPerform(trigger.string(this.name))
	}

	//------------------------------------------------------------------------------------------------------------------
	fun drop() {
		// Perform
		this.statementPerformer.addToTransactionOrPerform("DROP TABLE `$this.name`")
	}

	//------------------------------------------------------------------------------------------------------------------
	fun hasRow(where :SQLiteWhere) :Boolean { return count(where) > 0 }

	//------------------------------------------------------------------------------------------------------------------
	fun count(where :SQLiteWhere? = null) :Int {
		// Perform
		var	count = 0
		this.statementPerformer.perform("SELECT COUNT(*) FROM `$this.name`" + (where?.string ?: ""),
				where?.values) {
					// Query count
					count = it.integer(countAllTableColumn)!!
				}

		return count
	}

	//------------------------------------------------------------------------------------------------------------------
	fun rowID(where :SQLiteWhere) :Long? {
		// Query rowID
		var	rowID :Long? = null
		selectTableColumns(listOf(SQLiteTableColumn.rowID), where = where) { rowID = it.long(SQLiteTableColumn.rowID) }

		return rowID
	}

	//------------------------------------------------------------------------------------------------------------------
	fun selectTableColumns(tableColumns :List<SQLiteTableColumn>? = null, innerJoin :SQLiteInnerJoin? = null,
			where :SQLiteWhere? = null, orderBy :SQLiteOrderBy? = null, resultsRowProc :SQLiteResultsRowProc) {
		// Perform
		select(if (tableColumns != null) columnNamesFromTableColumns(tableColumns) else "*", innerJoin, where, orderBy,
				resultsRowProc)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun selectTableAndTableColumns(tableColumns :List<SQLiteTableAndTableColumn>, innerJoin :SQLiteInnerJoin? = null,
			where :SQLiteWhere? = null, orderBy :SQLiteOrderBy? = null, resultsRowProc :SQLiteResultsRowProc) {
		// Perform
		select(columnNamesFromTableAndTableColumns(tableColumns), innerJoin, where, orderBy, resultsRowProc)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun insertRow(info :List<SQLiteTableColumnAndValue>) :Long {
		// Perform
		var	lastInsertRowID :Long = 0
		insertRow(info) { lastInsertRowID = it }

		return lastInsertRowID
	}

	//------------------------------------------------------------------------------------------------------------------
	fun insertRow(info :List<SQLiteTableColumnAndValue>, lastInsertRowIDProc :(lastInsertRowID :Long) -> Unit) {
		// Setup
		val tableColumns = info.map() { it.first }
		val	statement =
					"INSERT INTO `$this.name` (" + columnNamesFromTableColumns(tableColumns) + ") VALUES (" +
							Array(info.size) { "?" }.joinToString(",") + ")"
		val	values = info.map() { it.second }

		// Perform
		this.statementPerformer.addToTransactionOrPerform(statement, values, lastInsertRowIDProc)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun insertOrReplaceRow(info :List<SQLiteTableColumnAndValue>) :Long {
		// Perform
		var	lastInsertRowID :Long = 0
		insertOrReplaceRow(info) { lastInsertRowID = it }

		return lastInsertRowID
	}

	//------------------------------------------------------------------------------------------------------------------
	fun insertOrReplaceRow(info :List<SQLiteTableColumnAndValue>,
			lastInsertRowIDProc :(lastInsertRowID :Long) -> Unit) {
		// Setup
		val tableColumns = info.map() { it.first }
		val	statement =
					"INSERT OR REPLACE INTO `$this.name` (" + columnNamesFromTableColumns(tableColumns) + ") VALUES (" +
							Array(info.size) { "?" }.joinToString(",") + ")"
		val	values = info.map() { it.second }

		// Perform
		this.statementPerformer.addToTransactionOrPerform(statement, values, lastInsertRowIDProc)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun insertOrReplaceRows(tableColumn :SQLiteTableColumn, values :List<Any>) {
		// Perform in chunks of SQLITE_LIMIT_VARIABLE_NUMBER
		values.forEachChunk(SQLiteStatementPerformer.variableNumberLimit) {
			// Setup
			val	statement =
						"INSERT OR REPLACE INTO `$this.name` (" + columnNamesFromTableColumns(listOf(tableColumn)) +
								") VALUES " + Array(it.count()) { "(?)" }.joinToString(",")

			// Perform
			this.statementPerformer.addToTransactionOrPerform(statement, it)
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	fun update(info :List<SQLiteTableColumnAndValue>, where :SQLiteWhere) {
		// Setup
		val	statement = "UPDATE `$this.name` SET " + info.joinToString() { "`$it.first.name` = ?" } + where.string
		val	values = info.map() { it.second } + where.values

		// Perform
		this.statementPerformer.addToTransactionOrPerform(statement, values)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun deleteRows(tableColumn :SQLiteTableColumn, values :List<Any>) {
		// Perform in chunks of SQLITE_LIMIT_VARIABLE_NUMBER
		values.forEachChunk(SQLiteStatementPerformer.variableNumberLimit) {
			// Setup
			val	statement =
						"DELETE FROM `$this.name` WHERE `$tableColumn.name` IN (" +
								Array(it.count()) { "?" }.joinToString(",") + ")"

			// Perform
			this.statementPerformer.addToTransactionOrPerform(statement, it)
		}
	}

	// Private methods
	//------------------------------------------------------------------------------------------------------------------
	private fun columnNamesFromTableColumns(tableColumns :List<SQLiteTableColumn>) :String {
		// Collect column names
		return tableColumns.joinToString(",") { "`$it.name`" }
	}

	//------------------------------------------------------------------------------------------------------------------
	private fun columnNamesFromTableAndTableColumns(tableColumns :List<SQLiteTableAndTableColumn>) :String {
		// Collect column names
		return tableColumns.joinToString(",") { "`$it.table.name`.`$it.tableColumn.name`" }
	}

	//------------------------------------------------------------------------------------------------------------------
	private fun select(columnNames :String, innerJoin :SQLiteInnerJoin?, where :SQLiteWhere?, orderBy :SQLiteOrderBy?,
			resultsRowProc :SQLiteResultsRowProc) {
		// Check if we have SQLiteWhere
		if (where != null) {
			// Iterate all groups in SQLiteWhere
			where.forEachValueGroup(SQLiteStatementPerformer.variableNumberLimit) { string, values ->
				// Compose statement
				val	statement =
							"SELECT $columnNames FROM `$this.name`" + (innerJoin?.string ?: "") + string +
									(orderBy?.string ?: "")

				// Perform
				this.statementPerformer.perform(statement, values, resultsRowProc)
			}
		} else {
			// No SQLiteWhere
			val	statement =
						"SELECT $columnNames FROM `$this.name`" + (innerJoin?.string ?: "") +
								(orderBy?.string ?: "")

			// Perform
			this.statementPerformer.perform(statement, resultsRowProc = resultsRowProc)
		}
	}

	// Companion object
	//------------------------------------------------------------------------------------------------------------------
	companion object {

		// Values
		private	val	countAllTableColumn =
							SQLiteTableColumn("COUNT(*)", SQLiteTableColumn.Kind.INTEGER,
									SQLiteTableColumn.Options.NONE)
	}
}
