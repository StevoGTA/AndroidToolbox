package codes.stevobrock.androidtoolbox.concurrency

import java.util.concurrent.locks.ReentrantReadWriteLock

//----------------------------------------------------------------------------------------------------------------------
class LockingInt(private var valueInternal :Int = 0) {

	// Properties
			val	value :Int get() {
						// Get size
						this.lock.readLock().lock()
						val	value = this.valueInternal
						this.lock.readLock().unlock()

						return value
					}

	private	val	lock = ReentrantReadWriteLock()

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun set(value :Int) {
		// Set
		this.lock.writeLock().lock()
		this.valueInternal = value
		this.lock.writeLock().unlock()
	}

	//------------------------------------------------------------------------------------------------------------------
	fun add(value :Int) {
		// Set
		this.lock.writeLock().lock()
		this.valueInternal += value
		this.lock.writeLock().unlock()
	}

	//------------------------------------------------------------------------------------------------------------------
	fun subtract(value :Int) {
		// Set
		this.lock.writeLock().lock()
		this.valueInternal -= value
		this.lock.writeLock().unlock()
	}
}
