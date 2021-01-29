package codes.stevobrock.androidtoolbox.concurrency

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

//----------------------------------------------------------------------------------------------------------------------
class LockingHashMap<T, U> {

	// Properties
			val	values :Collection<U> get() = this.lock.read() { this.map.values }

	private	val	lock = ReentrantReadWriteLock()
	private	val	map = HashMap<T, U>()

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun value(key :T) :U? { return this.lock.read() { this.map[key] } }

	//------------------------------------------------------------------------------------------------------------------
	fun set(value :U, key :T) :LockingHashMap<T, U> { this.lock.write() { this.map[key] = value }; return this }

	//------------------------------------------------------------------------------------------------------------------
	fun remove(key :T) :U? {
		// Perform under lock
		this.lock.write() {
			// Retrieve value
			val value = this.map[key]

			// Remove from map
			this.map.remove(key)

			return value
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	fun removeAll() : HashMap<T, U> {
		// Perform
		this.lock.write() {
			// Get map
			val map = this.map

			// Remove all
			this.map.clear()

			return map
		}
	}
}
