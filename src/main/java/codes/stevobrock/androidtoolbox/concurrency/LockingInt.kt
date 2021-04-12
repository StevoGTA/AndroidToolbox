package codes.stevobrock.androidtoolbox.concurrency

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

//----------------------------------------------------------------------------------------------------------------------
class LockingInt(private var valueInternal :Int = 0) {

	// Properties
			val	value :Int get() = this.lock.read() { this.valueInternal }

	private	val	lock = ReentrantReadWriteLock()

	private	var	semaphore :Object? = null

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun set(value :Int) :Int { this.lock.write() { this.valueInternal = value }; return value }

	//------------------------------------------------------------------------------------------------------------------
	fun add(value :Int) :Int {
		// Add
		val	resultValue :Int
		this.lock.write() {
			// Update value
			this.valueInternal += value

			// Signal
			if (this.semaphore != null) synchronized(this.semaphore!!) { this.semaphore!!.notify() }

			// Store
			resultValue = this.valueInternal
		}

		return resultValue
	}

	//------------------------------------------------------------------------------------------------------------------
	fun subtract(value :Int) :Int {
		// Subtract
		val	resultValue :Int
		this.lock.write() {
			// Update value
			this.valueInternal -= value

			// Signal
			if (this.semaphore != null) synchronized(this.semaphore!!) { this.semaphore!!.notify() }

			// Store
			resultValue = this.valueInternal
		}

		return resultValue
	}

	//------------------------------------------------------------------------------------------------------------------
	fun wait(value :Int) {
		// Setup
		this.semaphore = Object()

		while (this.value != value) synchronized(this.semaphore!!) { this.semaphore!!.wait() }
	}
}
