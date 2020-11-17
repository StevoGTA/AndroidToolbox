package codes.stevobrock.androidtoolbox.model

//----------------------------------------------------------------------------------------------------------------------
interface NotificationObserver {

	// Methods
	fun onReceiveNotification(name :String, sender :Any?, info :Map<String, Any>?)
}

//----------------------------------------------------------------------------------------------------------------------
private class ProcNotificationObserver(private val proc :(sender :Any?, info :Map<String, Any>?) -> Unit)
		: NotificationObserver {

	// NotificationObserver methods
	//------------------------------------------------------------------------------------------------------------------
	override fun onReceiveNotification(name: String, sender: Any?, info: Map<String, Any>?) {
		// Call proc
		this.proc(sender, info)
	}
}

//----------------------------------------------------------------------------------------------------------------------
class NotificationCenter {

	// Types
	private class NotificationObserverInfo(val sender :Any?, val notificationObserver :NotificationObserver)

	// Properties
	private	var	notificationObserverMap = mutableMapOf<String, MutableList<NotificationObserverInfo>>()

	// Methods
	//------------------------------------------------------------------------------------------------------------------
	fun add(notificationObserver :NotificationObserver, name :String, sender :Any? = null) {
		// Add
		val notificationObserverInfos = this.notificationObserverMap[name] ?: mutableListOf()
		notificationObserverInfos.add(NotificationObserverInfo(sender, notificationObserver))
		this.notificationObserverMap[name] = notificationObserverInfos
	}

	//------------------------------------------------------------------------------------------------------------------
	fun add(name :String, proc :(sender :Any?, info :Map<String, Any>?) -> Unit) :NotificationObserver {
		// Setup
		val notificationObserver = ProcNotificationObserver(proc)

		// Add
		add(notificationObserver, name, null)

		return notificationObserver
	}

	//------------------------------------------------------------------------------------------------------------------
	fun add(name :String, sender :Any, proc :(sender :Any?, info :Map<String, Any>?) -> Unit)
			:NotificationObserver {
		// Setup
		val notificationObserver = ProcNotificationObserver(proc)

		// Add
		add(notificationObserver, name, sender)

		return notificationObserver
	}

	//------------------------------------------------------------------------------------------------------------------
	fun remove(notificationObserver :NotificationObserver, name :String, sender :Any? = null) {
		// Query existing
		var notificationObserverInfos = this.notificationObserverMap[name]
		if (notificationObserverInfos == null) return

		// Remove
		notificationObserverInfos =
				notificationObserverInfos.filter {
						(it.notificationObserver == notificationObserver) && (it.sender == sender)
				} as MutableList<NotificationObserverInfo>

		// Update
		if (!notificationObserverInfos.isEmpty())
			// Store
			this.notificationObserverMap[name] = notificationObserverInfos
		else
			// No more
			this.notificationObserverMap.remove((name))
	}

	//------------------------------------------------------------------------------------------------------------------
	fun remove(notificationObserver :NotificationObserver) {
		// Iterate all notification names
		val keys = this.notificationObserverMap.keys.toTypedArray()
		keys.forEach { name ->
			// Query existing
			var notificationObserverInfos = this.notificationObserverMap[name]
			if (notificationObserverInfos == null) return

			// Remove
			notificationObserverInfos =
				notificationObserverInfos.filter {
					(it.notificationObserver != notificationObserver)
				} as MutableList<NotificationObserverInfo>

			// Update
			if (!notificationObserverInfos.isEmpty())
				// Store
				this.notificationObserverMap[name] = notificationObserverInfos
			else
				// No more
				this.notificationObserverMap.remove(name)
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	fun post(name :String, info :Map<String, Any>? = null, sender :Any? = null) {
		// Iterate notification observer infos
		this.notificationObserverMap[name]?.forEach {
			// Check sender
			if ((it.sender == null) || (it.sender == sender))
				// Notify
				it.notificationObserver.onReceiveNotification(name, sender, info)
		}
	}

	// Companion object
	//------------------------------------------------------------------------------------------------------------------
	companion object {

		// Properties
		val	shared = NotificationCenter()
	}
}
