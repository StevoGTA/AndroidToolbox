package codes.stevobrock.androidtoolbox.sqlite

import android.database.Cursor
import java.lang.Exception

//----------------------------------------------------------------------------------------------------------------------
// Types
typealias SQLiteResultsRowProc = (resultsRow :SQLiteResultsRow) -> Unit

//----------------------------------------------------------------------------------------------------------------------
// SQLiteResultsRow
class SQLiteResultsRow(private val cursor :Cursor) {

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun moveToFirst() :Boolean { return this.cursor.moveToFirst() }

	//------------------------------------------------------------------------------------------------------------------
	fun moveToNext() :Boolean { return this.cursor.moveToNext() }

	//------------------------------------------------------------------------------------------------------------------
	fun integer(tableColumn :SQLiteTableColumn) :Int? {
		// Setup
		if (!tableColumn.kind.isInteger) throw TableColumnKindMismatchException()

		val columnIndex = this.cursor.getColumnIndex(tableColumn.name)
		if (columnIndex == -1) throw TableColumnNotFoundException()

		return if (!this.cursor.isNull(columnIndex)) this.cursor.getInt(columnIndex) else null
	}

	//------------------------------------------------------------------------------------------------------------------
	fun long(tableColumn :SQLiteTableColumn) :Long? {
		// Setup
		if (!tableColumn.kind.isInteger) throw TableColumnKindMismatchException()

		val columnIndex = this.cursor.getColumnIndex(tableColumn.name)
		if (columnIndex == -1) throw TableColumnNotFoundException()

		return if (!this.cursor.isNull(columnIndex)) this.cursor.getLong(columnIndex) else null
	}

	//------------------------------------------------------------------------------------------------------------------
	fun float(tableColumn :SQLiteTableColumn) :Float? {
		// Setup
		if (!tableColumn.kind.isReal) throw TableColumnKindMismatchException()

		val columnIndex = this.cursor.getColumnIndex(tableColumn.name)
		if (columnIndex == -1) throw TableColumnNotFoundException()

		return if (!this.cursor.isNull(columnIndex)) this.cursor.getFloat(columnIndex) else null
	}

	//------------------------------------------------------------------------------------------------------------------
	fun double(tableColumn :SQLiteTableColumn) :Double? {
		// Setup
		if (!tableColumn.kind.isReal) throw TableColumnKindMismatchException()

		val columnIndex = this.cursor.getColumnIndex(tableColumn.name)
		if (columnIndex == -1) throw TableColumnNotFoundException()

		return if (!this.cursor.isNull(columnIndex)) this.cursor.getDouble(columnIndex) else null
	}

	//------------------------------------------------------------------------------------------------------------------
	fun text(tableColumn :SQLiteTableColumn) :String? {
		// Setup
		if (!tableColumn.kind.isText) throw TableColumnKindMismatchException()

		val columnIndex = this.cursor.getColumnIndex(tableColumn.name)
		if (columnIndex == -1) throw TableColumnNotFoundException()

		return if (!this.cursor.isNull(columnIndex)) this.cursor.getString(columnIndex) else null
	}

	//------------------------------------------------------------------------------------------------------------------
	fun blob(tableColumn :SQLiteTableColumn) :ByteArray? {
		// Setup
		if (!tableColumn.kind.isBlob) throw TableColumnKindMismatchException()

		val columnIndex = this.cursor.getColumnIndex(tableColumn.name)
		if (columnIndex == -1) throw TableColumnNotFoundException()

		return if (!this.cursor.isNull(columnIndex)) this.cursor.getBlob(columnIndex) else null
	}

	//------------------------------------------------------------------------------------------------------------------
	fun close() { this.cursor.close() }

	// Private classes
	//------------------------------------------------------------------------------------------------------------------
	private class TableColumnKindMismatchException : Exception()
	private	class TableColumnNotFoundException :Exception()
}
