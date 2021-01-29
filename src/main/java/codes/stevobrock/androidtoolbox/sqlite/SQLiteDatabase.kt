package codes.stevobrock.androidtoolbox.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import codes.stevobrock.androidtoolbox.sqlite.SQLiteDatabase.Options.Companion.WAL_MODE
import codes.stevobrock.androidtoolbox.sqlite.SQLiteTable.Options.Companion.NONE

//----------------------------------------------------------------------------------------------------------------------
class SQLiteDatabase {

	// Options
	class Options {
		companion object {
			// Values
			const	val	NONE = 0x00
			const	val	WAL_MODE = 0x01
		}
	}

	// TransactionResult
	enum class TransactionResult {
		COMMIT,
		ROLLBACK,
	}

	// Properties
	private	val	database :SQLiteDatabase
	private	val	statementPerformer :SQLiteStatementPerformer

//	//------------------------------------------------------------------------------------------------------------------
//	static func doesExist(in folder :Folder, with name :String = "database") -> Bool {
//		// Check for known extensions
//		return FileManager.default.exists(folder.file(with: name.appending(pathExtension: "sqlite"))) ||
//				FileManager.default.exists(folder.file(with: name.appending(pathExtension: "sqlite3")))
//	}

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(context :Context, name :String, options :Int = WAL_MODE) {
		// Setup
		this.database = context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null)
		this.statementPerformer = SQLiteStatementPerformer(this.database)

		// Check options
		if ((options or WAL_MODE) != 0)
			// Activate WAL mode
			database.enableWriteAheadLogging()
	}

//	//------------------------------------------------------------------------------------------------------------------
//	constructor(folder :Folder, name :String = "database", options :Int = WAL_MODE) {
//		// Setup
//		let	urlBase = folder.url.appendingPathComponent(name)
//		let	file =
//					FileManager.default.exists(File(urlBase.appendingPathExtension("sqlite3"))) ?
//							File(urlBase.appendingPathExtension("sqlite3")) :
//							File(urlBase.appendingPathExtension("sqlite"))
//
//		try self.init(with: file, options: options)
//	}

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun table(name :String, options :Int = NONE, tableColumns :List<SQLiteTableColumn>,
			references :List<SQLiteTableColumnReference> = listOf()) :SQLiteTable {
		// Create table
		return SQLiteTable(name, options, tableColumns, references, this.statementPerformer)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun performAsTransaction(proc :() -> TransactionResult) {
		// Trigger statement performer to perform as a transaction
		this.statementPerformer.performAsTransaction() {
			// Call proc
			when (proc()) {
				TransactionResult.COMMIT -> SQLiteStatementPerformer.TransactionResult.COMMIT
				TransactionResult.ROLLBACK -> SQLiteStatementPerformer.TransactionResult.ROLLBACK
			}
		}
	}
}
