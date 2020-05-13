package codes.stevobrock.androidtoolbox.model

//----------------------------------------------------------------------------------------------------------------------
interface NotificationObserver {

	// Methods
	fun onReceiveNotification(name :String, sender :Any?, info :Map<String, Any>?)
}

//----------------------------------------------------------------------------------------------------------------------
object NotificationCenter {

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
		this.notificationObserverMap.keys.forEach { name ->
			// Query existing
			var notificationObserverInfos = this.notificationObserverMap[name]
			if (notificationObserverInfos == null) return

			// Remove
			notificationObserverInfos =
				notificationObserverInfos.filter {
					(it.notificationObserver == notificationObserver)
				} as MutableList<NotificationObserverInfo>

			// Update
			if (!notificationObserverInfos.isEmpty())
				// Store
				this.notificationObserverMap[name] = notificationObserverInfos
			else
				// No more
				this.notificationObserverMap.remove((name))
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
}
