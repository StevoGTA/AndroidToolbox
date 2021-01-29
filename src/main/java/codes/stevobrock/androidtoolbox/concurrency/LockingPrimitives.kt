package codes.stevobrock.androidtoolbox.concurrency

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

//----------------------------------------------------------------------------------------------------------------------
class LockingInt(private var valueInternal :Int = 0) {

	// Properties
			val	value :Int get() = this.lock.read() { this.valueInternal }

	private	val	lock = ReentrantReadWriteLock()

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun set(value :Int) :LockingInt { this.lock.write() { this.valueInternal = value }; return this }

	//------------------------------------------------------------------------------------------------------------------
	fun add(value :Int) :LockingInt { this.lock.write() { this.valueInternal += value }; return this }

	//------------------------------------------------------------------------------------------------------------------
	fun subtract(value :Int) :LockingInt { this.lock.write() { this.valueInternal -= value }; return this }
}
