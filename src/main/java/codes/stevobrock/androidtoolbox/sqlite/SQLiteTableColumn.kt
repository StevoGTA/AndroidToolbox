package codes.stevobrock.androidtoolbox.sqlite

import codes.stevobrock.androidtoolbox.sqlite.SQLiteTableColumn.Options.Companion.NONE
import codes.stevobrock.androidtoolbox.sqlite.SQLiteTableColumn.Options.Companion.NOT_NULL

//----------------------------------------------------------------------------------------------------------------------
// SQLiteTableColumnReference
typealias SQLiteTableColumnReference = Triple<SQLiteTableColumn, SQLiteTable, SQLiteTableColumn>

//----------------------------------------------------------------------------------------------------------------------
// SQLiteTableColumn
class SQLiteTableColumn(val name :String, val kind :Kind, val options :Int, val defaultValue :Any? = null) {

	// Kind
	enum class Kind {
		// Values
		// INTEGER values are whole numbers (either positive or negative).
		INTEGER,

		// REAL values are real numbers with decimal values that use 8-byte floats.
		REAL,

		// TEXT is used to store character data. The maximum length of TEXT is unlimited. SQLite supports
		//	various character encodings.
		TEXT,

		// BLOB stands for a binary large object that can be used to store any kind of data. The maximum size
		//	of BLOBs is unlimited
		BLOB,

		// Dates (not built-in bytes, but we handle)
		//	See https://sqlite.org/lang_datefunc.html
		DATE_ISO8601_FRACTIONAL_SECONDS_AUTO_SET,		// YYYY-MM-DDTHH:MM:SS.SSS (will auto set on insert/replace)
		DATE_ISO8601_FRACTIONAL_SECONDS_AUTO_UPDATE;	// YYYY-MM-DDTHH:MM:SS.SSS (will auto update on insert/replace)

		// Properties
		val	isInteger :Boolean get() {
					// Switch this
					when (this) {
						INTEGER ->	return true
						else ->		return false
					}
				}
		val	isReal :Boolean get() {
					// Switch self
					when (this) {
						REAL ->	return true
						else ->	return false
					}
				}
		val	isText :Boolean get() {
					// Switch self
					when (this) {
						TEXT ->	return true
						DATE_ISO8601_FRACTIONAL_SECONDS_AUTO_SET -> return true
						DATE_ISO8601_FRACTIONAL_SECONDS_AUTO_UPDATE -> return true
						else ->	return false
					}
				}
		val	isBlob :Boolean get() {
					// Switch self
					when (this) {
						BLOB ->	return true
						else ->	return false
					}
				}
	}

	// Options
	class Options {
		companion object {
			// Values
			const	val	NONE = 0x00
			const	val	PRIMARY_KEY = 0x01
			const	val	AUTO_INCREMENT = 0x02
			const	val	NOT_NULL = 0x04
			const	val	UNIQUE = 0x08
			const	val	CHECK = 0x10
		}
	}

	// Companion object
	//------------------------------------------------------------------------------------------------------------------
	companion object {

		// Values
		val	rowID = SQLiteTableColumn("rowID", Kind.INTEGER, NONE)
		val	all = SQLiteTableColumn("*", Kind.INTEGER, NONE)

		// Methods
		//--------------------------------------------------------------------------------------------------------------
		fun dateISO8601FractionalSecondsAutoSet(name :String) :SQLiteTableColumn {
			// Return info
			return SQLiteTableColumn(name, Kind.DATE_ISO8601_FRACTIONAL_SECONDS_AUTO_SET, NOT_NULL,
					"strftime('%Y-%m-%dT%H:%M:%f', 'now', 'localtime')")
		}

		//--------------------------------------------------------------------------------------------------------------
		fun dateISO8601FractionalSecondsAutoUpdate(name :String) :SQLiteTableColumn {
			// Return info
			return SQLiteTableColumn(name, Kind.DATE_ISO8601_FRACTIONAL_SECONDS_AUTO_UPDATE, NOT_NULL,
					"strftime('%Y-%m-%dT%H:%M:%f', 'now', 'localtime')")
		}
	}
}
