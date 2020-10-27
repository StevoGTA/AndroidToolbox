package codes.stevobrock.androidtoolbox.model

import android.content.Context
import codes.stevobrock.androidtoolbox.extensions.dateFromRFC3339Extended
import codes.stevobrock.androidtoolbox.extensions.rfc3339Extended
import java.io.File
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock

//----------------------------------------------------------------------------------------------------------------------
interface DataCache {

	// Methods
	fun store(data :ByteArray, identifier :String)
	fun retrieveData(identifier :String) :ByteArray?
}

//----------------------------------------------------------------------------------------------------------------------
open class MemoryDataCache(private val sizeLimit :Long? = null) : DataCache {

	// Types
	//------------------------------------------------------------------------------------------------------------------
	class ItemInfo(val data :ByteArray, val identifier :String) {

		// Properties
		var	lastAccessedDate = Date()
			private set

		// Instance methods
		//--------------------------------------------------------------------------------------------------------------
		fun noteAccessed() { this.lastAccessedDate = Date() }
	}

	// Properties
	private	val	mapLock = ReentrantReadWriteLock()

	private	var	map = HashMap<String, ItemInfo>()

	// DataCache methods
	//------------------------------------------------------------------------------------------------------------------
	override fun store(data :ByteArray, identifier :String) {
		// Update map
		this.mapLock.writeLock().lock()

		// Store new item info
		this.map[identifier] = ItemInfo(data, identifier)

		// Check if doing pruning
		if (this.sizeLimit != null) {
			// Collect info
			val itemInfos = this.map.values
			var totalSize = itemInfos.fold(0) { sum, itemInfo -> sum + itemInfo.data.size }
			if (totalSize > this.sizeLimit) {
				// Need to prune
				val itemInfosSorted = itemInfos.sortedBy { it.lastAccessedDate }.toMutableList()
				while (totalSize > this.sizeLimit) {
					// Remote the first
					val itemInfo = itemInfosSorted.removeAt(0)
					totalSize -= itemInfo.data.size
					this.map.remove(itemInfo.identifier)
				}
			}
		}

		// Done
		this.mapLock.writeLock().unlock()
	}

	//------------------------------------------------------------------------------------------------------------------
	override fun retrieveData(identifier :String) :ByteArray? {
		// Retrieve item info
		this.mapLock.readLock().lock()
		val itemInfo = this.map[identifier]
		this.mapLock.readLock().unlock()

		// Note accessed
		itemInfo?.noteAccessed()

		return itemInfo?.data
	}
}

//----------------------------------------------------------------------------------------------------------------------
open class FilesystemDataCache : DataCache {

	// Types
	//------------------------------------------------------------------------------------------------------------------
	class ItemInfo {

		// Properties
		val	file :File
		val	size :Long

		var	lastAccessedDate :Date
			private set

		// Lifecycle methods
		//--------------------------------------------------------------------------------------------------------------
		constructor(file :File, data :ByteArray) {
			// Store
			this.file = file
			this.size = data.size.toLong()
			this.lastAccessedDate = Date()

			// Write
			file.writeBytes(data)
			FileExtendedAttributes(this.file).set("lastAccessedDate", this.lastAccessedDate.rfc3339Extended)
		}

		//--------------------------------------------------------------------------------------------------------------
		constructor(file :File) {
			// Store
			this.file = file
			this.size = file.length()

			// Retrieve last accessed date from filesystem
			val string = FileExtendedAttributes(this.file).getString("lastAccessedDate")
			if (string != null)
				// Have last accessed date string
				this.lastAccessedDate = dateFromRFC3339Extended(string)!!
			else
				// Assume is now
				this.lastAccessedDate = Date()
		}

		// Instance methods
		//--------------------------------------------------------------------------------------------------------------
		fun noteAccessed() {
			// Update internals
			this.lastAccessedDate = Date()

			// Update filesystem
			FileExtendedAttributes(this.file).set("lastAccessedDate", this.lastAccessedDate.rfc3339Extended)
		}
	}

	// Properties
	private	val	mapLock = ReentrantReadWriteLock()

	private	var	folder :File
	private	var	sizeLimit :Long? = null
	private	var	map = HashMap<String, ItemInfo>()

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(folder :File, sizeLimit :Long? = null) {
		// Store
		this.folder = folder
		this.sizeLimit = sizeLimit

		// Setup
		this.folder.mkdirs()

		// Note existing files
		this.folder.walk().filter { it.isFile && !it.isHidden }.forEach { this.map[it.name] = ItemInfo(it) }
	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(context :Context, folderName :String, sizeLimit :Long? = null) {
		// Setup
		this.folder = File(context.cacheDir, folderName)
		this.sizeLimit = sizeLimit

		// Setup
		this.folder.mkdirs()

		// Note existing files
		this.folder.walk().filter { it.isFile && !it.isHidden }.forEach { this.map[it.name] = ItemInfo(it) }
	}

	// DataCache methods
	//------------------------------------------------------------------------------------------------------------------
	override fun store(data :ByteArray, identifier :String) {
		// Setup
		val file = File(this.folder, identifier)
		val itemInfo = ItemInfo(file, data)

		// Update map
		this.mapLock.writeLock().lock()

		// Store new item info
		this.map[identifier] = itemInfo

		// Check if doing pruning
		if (this.sizeLimit != null) {
			// Collect info
			val itemInfos = this.map.values
			var totalSize = itemInfos.fold(0L) { sum, _itemInfo -> sum + _itemInfo.size }
			if (totalSize > this.sizeLimit!!) {
				// Need to prune
				val itemInfosSorted = itemInfos.sortedBy { it.lastAccessedDate }.toMutableList()
				while (totalSize > this.sizeLimit!!) {
					// Remote the first
					val _itemInfo = itemInfosSorted.removeAt(0)
					_itemInfo.file.delete()
					totalSize -= _itemInfo.size
					this.map.remove(_itemInfo.file.name)
				}
			}
		}

		// Done
		this.mapLock.writeLock().unlock()
	}

	//------------------------------------------------------------------------------------------------------------------
	override fun retrieveData(identifier :String) :ByteArray? {
		// Retrieve item info
		this.mapLock.readLock().lock()
		val itemInfo = this.map[identifier]
		this.mapLock.readLock().unlock()

		// Note accessed
		itemInfo?.noteAccessed()

		return itemInfo?.file?.readBytes()
	}
}
