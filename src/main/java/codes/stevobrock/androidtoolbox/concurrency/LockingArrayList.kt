package codes.stevobrock.androidtoolbox.concurrency

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.concurrent.read
import kotlin.concurrent.write

//----------------------------------------------------------------------------------------------------------------------
class LockingArrayList<T> {

	// Properties
			val	size :Int get() = this.lock.read() { this.arrayList.size }
			val	values :List<T> get() = this.lock.read() { this.arrayList.toList() }

	private	val	lock = ReentrantReadWriteLock()
	private val	arrayList = ArrayList<T>()

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun add(t :T) :LockingArrayList<T> { this.lock.write() { this.arrayList.add(t) }; return this }

	//------------------------------------------------------------------------------------------------------------------
	fun forEach(action :(T) -> Unit) {
		// Perform
		this.lock.read() {
			// Iterate items
			for (t :T in this.arrayList)
				// Call proc
				action(t)
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	fun sortWith(comparator :Comparator<T>) :LockingArrayList<T> {
		// Sort
		this.lock.write() { this.arrayList.sortWith(comparator) }

		return this
	}

	//------------------------------------------------------------------------------------------------------------------
	fun removeFirst() :T { return this.lock.write() { this.arrayList.removeAt(0) } }

	//------------------------------------------------------------------------------------------------------------------
	fun removeIf(proc :(T) -> Boolean) {
		// Perform
		this.lock.write() {
			// Iterate all in reverse
			for (i in arrayList.indices.reversed()) {
				// Setup
				val t = this.arrayList[i]

				// Call proc
				if (proc(t))
					// Remove
					this.arrayList.removeAt(i)
			}
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	fun clear() :List<T> {
		// Perform
		this.lock.write() {
			// Get values
			val	values = this.arrayList.toList()

			// Remove all values
			this.arrayList.clear()

			return values
		}
	}
}
