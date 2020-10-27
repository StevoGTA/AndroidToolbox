package codes.stevobrock.androidtoolbox.concurrency

import java.util.concurrent.locks.ReentrantReadWriteLock

//----------------------------------------------------------------------------------------------------------------------
class LockingHashMap<T, U> {

	// Properties
			val	values :Collection<U>
				get() {
						// Retrieve values
						this.lock.readLock().lock()
						val	values = this.map.values
						this.lock.readLock().unlock()

						return values
					}
	private	val	lock = ReentrantReadWriteLock()
	private	val	map = HashMap<T, U>()

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun value(key :T) : U? {
		// Retrieve value
		this.lock.readLock().lock()
		val	value = this.map[key]
		this.lock.readLock().unlock()

		return value
	}

	//------------------------------------------------------------------------------------------------------------------
	fun set(value :U, key :T) {
		// Store
		this.lock.writeLock().lock()
		this.map[key] = value
		this.lock.writeLock().unlock()
	}

	//------------------------------------------------------------------------------------------------------------------
	fun remove(key :T) {
		// Remove
		this.lock.writeLock().lock()
		this.map.remove(key)
		this.lock.writeLock().unlock()
	}

	//------------------------------------------------------------------------------------------------------------------
	fun removeAll() {
		// Remove a;;
		this.lock.writeLock().lock()
		this.map.clear()
		this.lock.writeLock().unlock()
	}
}
