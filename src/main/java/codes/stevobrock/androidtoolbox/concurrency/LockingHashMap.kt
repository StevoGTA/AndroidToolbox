package codes.stevobrock.androidtoolbox.concurrency

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

//----------------------------------------------------------------------------------------------------------------------
class LockingHashMap<T, U> {

	// Properties
			val	hashMap :HashMap<T, U> get() = this.lock.read() { this.map }
			val	size :Int get() = this.lock.read() { this.map.size }
			val	isEmpty :Boolean get() = this.lock.read() { this.map.isEmpty() }
			val	keys :Collection<T> get() = this.lock.read() { this.map.keys }
			val	values :Collection<U> get() = this.lock.read() { this.map.values }

	private	val	lock = ReentrantReadWriteLock()
	private	val	map = HashMap<T, U>()

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun value(key :T) :U? { return this.lock.read() { this.map[key] } }

	//------------------------------------------------------------------------------------------------------------------
	fun set(key :T, value :U) :LockingHashMap<T, U> { this.lock.write() { this.map[key] = value }; return this }

	//------------------------------------------------------------------------------------------------------------------
	fun merge(map :Map<T, U>) :LockingHashMap<T, U> { this.lock.write() { this.map.putAll(map) }; return this }

	//------------------------------------------------------------------------------------------------------------------
	fun update(key :T, proc :(previous :U?) -> U?) :LockingHashMap<T, U> {
		// Update value under lock
		this.lock.write() {
			// Get new value
			val	newValue = proc(this.map[key])

			// Check new value
			if (newValue != null)
				// Store
				this.map[key] = newValue
			else
				// Remove
				this.map.remove(key)
		}

		return this
	}

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
	fun remove(keys :List<T>) :LockingHashMap<T, U> {
		// Remove keys
		this.lock.write() { keys.forEach() { this.map.remove(it) } };

		return this
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
