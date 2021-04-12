package codes.stevobrock.androidtoolbox.concurrency

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

//----------------------------------------------------------------------------------------------------------------------
class LockingArrayHashMap<T, U> {

	// Properties
			val isEmpty :Boolean get() = this.lock.read() { map.isEmpty() }
			val	allValues :List<U> get() =
					this.lock.read() {
						// Collect values
						val allValues = ArrayList<U>()
						map.values.forEach() { allValues.addAll(it) }

						return allValues
					}

	private	val lock = ReentrantReadWriteLock()
	private	val map = HashMap<T, ArrayList<U>>()

	// Instance Methods
	//------------------------------------------------------------------------------------------------------------------
	fun addArrayValue(key :T, value :U) {
		// Perform under lock
		this.lock.write() {
			// Check if has existing array
			val	arrayList = this.map[key]
			if (arrayList != null) {
				// Have existing array
				arrayList.add(value)
				this.map[key] = arrayList
			} else
				// First item
				this.map[key] = arrayListOf(value)
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	fun values(key :T) :List<U>? { return this.lock.read() { this.map[key] } }
}
