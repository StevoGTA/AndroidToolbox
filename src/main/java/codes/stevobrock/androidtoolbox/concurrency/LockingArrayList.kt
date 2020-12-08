package codes.stevobrock.androidtoolbox.concurrency

import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.Comparator
import kotlin.collections.ArrayList

//----------------------------------------------------------------------------------------------------------------------
class LockingArrayList<T> {

	// Properties
			val	size :Int get() {
						// Get size
						this.lock.readLock().lock()
						val	size = this.arrayList.size
						this.lock.readLock().unlock()

						return size
					}
			val	values :List<T> get() {
						// Get values
						this.lock.readLock().lock()
						val	values = this.arrayList.toList()
						this.lock.readLock().unlock()

						return values
					}

	private	val	lock = ReentrantReadWriteLock()
	private val	arrayList = ArrayList<T>()

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun add(t :T) {
		// Add
		this.lock.writeLock().lock()
		this.arrayList.add(t)
		this.lock.writeLock().unlock()
	}

	//------------------------------------------------------------------------------------------------------------------
	fun forEach(action :(T) -> Unit) {
		// Perform
		this.lock.readLock().lock()
		for (t :T in this.arrayList)
			action(t)
		this.lock.readLock().unlock()
	}

	//------------------------------------------------------------------------------------------------------------------
	fun sortWith(comparator :Comparator<T>) {
		// Perform
		this.lock.writeLock().lock()
		this.arrayList.sortWith(comparator)
		this.lock.writeLock().unlock()
	}

	//------------------------------------------------------------------------------------------------------------------
	fun removeFirst() :T {
		// Perform
		this.lock.writeLock().lock()
		val	t = this.arrayList.removeAt(0)
		this.lock.writeLock().unlock()

		return t
	}

	//------------------------------------------------------------------------------------------------------------------
	fun removeIf(proc :(T) -> Boolean) {
		// Perform
		this.lock.writeLock().lock()
		for (i in arrayList.indices.reversed()) {
			// Setup
			val t = this.arrayList[i]

			// Call proc
			if (proc(t))
				// Remove
				this.arrayList.removeAt(i)
		}
		this.lock.writeLock().unlock()
	}

	//------------------------------------------------------------------------------------------------------------------
	fun clear() {
		// Clear
		this.lock.writeLock().lock()
		this.arrayList.clear()
		this.lock.writeLock().unlock()
	}
}
