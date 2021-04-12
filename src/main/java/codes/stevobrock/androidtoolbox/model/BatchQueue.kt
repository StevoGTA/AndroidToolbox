package codes.stevobrock.androidtoolbox.model

//----------------------------------------------------------------------------------------------------------------------
class BatchQueue<T> {

	// Properties
	private	val maximumBatchSize :Int
	private	val proc :(items :List<T>) -> Unit
	private	val items = ArrayList<T>()

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(maximumBatchSize :Int, proc :(items :List<T>) -> Unit) {
		// Store
		this.maximumBatchSize = maximumBatchSize
		this.proc = proc
	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(proc :(items :List<T>) -> Unit) {
		// Store
		this.maximumBatchSize = 500
		this.proc = proc
	}

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun add(item :T) {
		// Add
		this.items.add(item)

		// Check if time to process some
		if (this.items.size >= this.maximumBatchSize) {
			// Time to process
			this.proc(this.items.slice(0..this.maximumBatchSize))
			this.items.subList(0, this.maximumBatchSize).clear()
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	fun add(items :List<T>) {
		// Add
		this.items.addAll(items)

		// Check if time to process some
		while (this.items.size >= this.maximumBatchSize) {
			// Time to process
			this.proc(this.items.slice(0..this.maximumBatchSize))
			this.items.subList(0, this.maximumBatchSize).clear()
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	fun finalize() {
		// Check for items
		if (this.items.isNotEmpty()) {
			// Call proc
			this.proc(this.items)

			// Cleanup
			this.items.clear()
		}
	}
}
