package codes.stevobrock.androidtoolbox.sqlite

import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import codes.stevobrock.androidtoolbox.concurrency.LockingHashMap

//----------------------------------------------------------------------------------------------------------------------
// SQLiteStatement
class SQLiteStatement(private val string :String, private val values :List<Any>? = null,
		private val lastInsertRowIDProc :((lastInsertRowID: Long) -> Unit)? = null,
		private val processValuesProc :SQLiteResultsRowProcessValuesProc? = null) {

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun perform(database: SQLiteDatabase) {
		// Perform
		if (this.processValuesProc != null) {
			// Perform as query
			val	resultsRow = SQLiteResultsRow(database.rawQuery(this.string, null))
			if (resultsRow.moveToFirst()) {
				// Iterate all rows
				do { this.processValuesProc(resultsRow) } while (resultsRow.moveToNext())
			}
			resultsRow.close()
		} else {
			// Compile
			val statement = database.compileStatement(this.string)

			// Bind values
			this.values?.forEachIndexed() { index, value ->
				// Bind value
				DatabaseUtils.bindObjectToProgram(statement, index + 1, value)
			}

			if (this.lastInsertRowIDProc != null)
				// Perform as insert
				this.lastInsertRowIDProc(statement.executeInsert())
			else
				// Perform
				statement.execute()
		}
	}
}

//----------------------------------------------------------------------------------------------------------------------
// SQLiteStatementPerformer
class SQLiteStatementPerformer(private val database :SQLiteDatabase) {

	// TransactionResult
	enum class TransactionResult {
		COMMIT,
		ROLLBACK,
	}

	// Properties
	private	val	lock = Any()

	private	var	transactionsMap = LockingHashMap<Thread, ArrayList<SQLiteStatement>>()

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun addToTransactionOrPerform(statement :String, values :List<Any>? = null,
			lastInsertRowIDProc :((lastInsertRowID: Long) -> Unit)? = null) {
		// Setup
		val sqliteStatement = SQLiteStatement(statement, values, lastInsertRowIDProc)

		// Check for transaction
		val sqliteStatements = this.transactionsMap.value(Thread.currentThread())
		if (sqliteStatements != null) {
			// In transaction
			sqliteStatements.add(sqliteStatement)
			this.transactionsMap.set(sqliteStatements, Thread.currentThread())
		} else
			// Perform
			synchronized(this.lock) { sqliteStatement.perform(this.database) }
	}

	//------------------------------------------------------------------------------------------------------------------
	fun perform(statement :String, values :List<Any>? = null, processValuesProc :SQLiteResultsRowProcessValuesProc) {
		// Perform
		synchronized(this.lock) {
			// Perform statement
			SQLiteStatement(statement, values, processValuesProc = processValuesProc).perform(this.database)
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	fun performAsTransaction(proc: () -> TransactionResult) {
		// Internals check
		if (this.transactionsMap.value(Thread.currentThread()) != null)
			// Already in transaction
			throw AlreadyInTransactionException()

		// Start transaction
		this.transactionsMap.set(ArrayList(), Thread.currentThread())

		// Call proc and check result
		if (proc() == TransactionResult.COMMIT) {
			// Collect SQLiteStatements
			val sqliteStatements = this.transactionsMap.remove(Thread.currentThread())!!

			// Check for empty transaction
			if (sqliteStatements.isEmpty())
				// Empty
				return

			// Perform
			this.database.beginTransaction()
			sqliteStatements.forEach() { it.perform(this.database) }
			this.database.endTransaction()
		} else
			// No longer in transaction
			this.transactionsMap.remove(Thread.currentThread())
	}

	// Companion object
	companion object {

		// Properties
		const	val	variableNumberLimit = 999
	}

	// Private classes
	//------------------------------------------------------------------------------------------------------------------
	private class AlreadyInTransactionException : Exception()
}
