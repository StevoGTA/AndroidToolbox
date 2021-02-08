package codes.stevobrock.androidtoolbox.networking

import android.net.Uri
import android.util.Log
import codes.stevobrock.androidtoolbox.concurrency.LockingArrayList
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

//----------------------------------------------------------------------------------------------------------------------
open class HTTPEndpointClient(private val scheme :String, private val authority :String, private val path :String = "",
		private val multiValueQueryParameterHandling :MultiValueQueryParameterHandling =
				MultiValueQueryParameterHandling.REPEAT_KEY,
		private val maximumURLLength :Int = 1024, private val maximumConcurrentHTTPEndpointRequests :Int = 5) {

	// Types
	enum class Priority(val value :Int) {
		NORMAL(0),
		BACKGROUND(1),
	}

	enum class MultiValueQueryParameterHandling {
		REPEAT_KEY,
		USE_COMMA,
	}

	class HTTPEndpointRequestInfo(val httpEndpointRequest :HTTPEndpointRequest, val identifier :String,
			val priority :Priority)

	// Properties
			var	logTransactions = false

	private	val	okHttpClient :OkHttpClient = OkHttpClient()

	private	val	updateActiveHTTPEndpointRequestsLock = ReentrantLock()

	private	var	activeHTTPEndpointRequestInfos = LockingArrayList<HTTPEndpointRequestInfo>()
	private	var	queuedHTTPEndpointRequestInfos = LockingArrayList<HTTPEndpointRequestInfo>()

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun queue(httpEndpointRequest :HTTPEndpointRequest, identifier :String = "", priority :Priority = Priority.NORMAL) {
		// Add to queue
		this.queuedHTTPEndpointRequestInfos.add(HTTPEndpointRequestInfo(httpEndpointRequest, identifier, priority))

		// Update active
		updateActiveHTTPEndpointRequests()
	}

	//------------------------------------------------------------------------------------------------------------------
	fun queue(dataHTTPEndpointRequest :DataHTTPEndpointRequest, identifier :String = "",
			priority :Priority = Priority.NORMAL, completionProc :(ByteArray?, Exception?) -> Unit) {
		// Setup
		dataHTTPEndpointRequest.completionProc = completionProc

		// Queue
		queue(dataHTTPEndpointRequest, identifier, priority)
	}

//	//------------------------------------------------------------------------------------------------------------------
//	fun queue(fileHTTPEndpointRequest :FileHTTPEndpointRequest, identifier :String = "",
//			priority :Priority = Priority.NORMAL, completionProc :(ByteArray?, Exception?) -> Unit) {
//		// Setup
//		fileHTTPEndpointRequest.completionProc = completionProc
//
//		// Queue
//		queue(fileHTTPEndpointRequest, identifier, priority)
//	}

	//------------------------------------------------------------------------------------------------------------------
	fun queue(headHTTPEndpointRequest :HeadHTTPEndpointRequest, identifier :String = "",
			priority :Priority = Priority.NORMAL, completionProc :(Map<String, String>?, Exception?) -> Unit) {
		// Setup
		headHTTPEndpointRequest.completionProc = completionProc

		// Queue
		queue(headHTTPEndpointRequest, identifier, priority)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun queue(integerHTTPEndpointRequest :IntegerHTTPEndpointRequest, identifier :String = "",
			priority :Priority = Priority.NORMAL, completionProc :(Int?, Exception?) -> Unit) {
		// Setup
		integerHTTPEndpointRequest.completionProc = completionProc

		// Queue
		queue(integerHTTPEndpointRequest, identifier, priority)
	}

//	//------------------------------------------------------------------------------------------------------------------
//	fun queue<T :Any>(jsonHTTPEndpointRequest :JSONHTTPEndpointRequest<T :Any>, identifier :String = "",
//			priority :Priority = Priority.NORMAL, completionProc :(T?, Exception?) -> Unit) {
//		// Setup
//		jsonHTTPEndpointRequest.completionProc = completionProc
//
//		// Queue
//		queue(jsonHTTPEndpointRequest, identifier, priority)
//	}

	//------------------------------------------------------------------------------------------------------------------
	fun queue(stringHTTPEndpointRequest :StringHTTPEndpointRequest, identifier :String = "",
			priority :Priority = Priority.NORMAL, completionProc :(String?, Exception?) -> Unit) {
		// Setup
		stringHTTPEndpointRequest.completionProc = completionProc

		// Queue
		queue(stringHTTPEndpointRequest, identifier, priority)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun queue(successHTTPEndpointRequest :SuccessHTTPEndpointRequest, identifier :String = "",
			priority :Priority = Priority.NORMAL, completionProc :(Exception?) -> Unit) {
		// Setup
		successHTTPEndpointRequest.completionProc = completionProc

		// Queue
		queue(successHTTPEndpointRequest, identifier, priority)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun cancel(identifier :String) {
		// One at a time please...
		this.updateActiveHTTPEndpointRequestsLock.lock()

		// Iterate all
		this.activeHTTPEndpointRequestInfos.forEach {
			// Check identifier
			if (it.identifier == identifier)
				// Identifier matches, cancel
				it.httpEndpointRequest.cancel()
		}
		this.queuedHTTPEndpointRequestInfos.removeIf {
			// Check identifier
			if (it.identifier == identifier) {
				// Match
				it.httpEndpointRequest.cancel()

				true
			} else
				// No match
				false
		}

		// Resume
		this.updateActiveHTTPEndpointRequestsLock.unlock()
	}

	// Private methods
	//------------------------------------------------------------------------------------------------------------------
	private fun updateActiveHTTPEndpointRequests() {
		// One at a time please...
		this.updateActiveHTTPEndpointRequestsLock.lock()

		// Remove finished
		this.activeHTTPEndpointRequestInfos.removeIf {
			// Check if state is FINISHED
			it.httpEndpointRequest.state == HTTPEndpointRequest.State.FINISHED
		}

		// Check if have available "slots"
		if (this.activeHTTPEndpointRequestInfos.size < this.maximumConcurrentHTTPEndpointRequests) {
			// Sort queued
			this.queuedHTTPEndpointRequestInfos.sortWith(Comparator { info1, info2 ->
				// Compare priority
				if (info1.priority.value > info2.priority.value)
					return@Comparator -1
				else
					return@Comparator 0
			})

			// Activate up to the maximum
			while ((this.queuedHTTPEndpointRequestInfos.size > 0) &&
					(this.activeHTTPEndpointRequestInfos.size < this.maximumConcurrentHTTPEndpointRequests)) {
				// Get first queued
				val httpEndpointRequestInfo = this.queuedHTTPEndpointRequestInfos.removeFirst()
				val httpEndpointRequest = httpEndpointRequestInfo.httpEndpointRequest
				if (httpEndpointRequest.isCancelled)
					// Skip
					continue

				// Make active
				httpEndpointRequest.transitionToState(HTTPEndpointRequest.State.ACTIVE)
				this.activeHTTPEndpointRequestInfos.add(httpEndpointRequestInfo)

				// Setup
				val requestBuilder = Request.Builder()

				// Add URL
				if (httpEndpointRequest.path.startsWith("http") || httpEndpointRequest.path.startsWith("https"))
					// Already have fully-formed URL
					requestBuilder.url(httpEndpointRequest.path)
				else {
					// Compose URL
					var	uriBuilder =
						Uri.Builder()
							.scheme(this.scheme)
							.authority(this.authority)
							.path(this.path + httpEndpointRequest.path)

					// Check if have query parameters
					if (httpEndpointRequest.queryComponents != null) {
						// Iterate query parameters
						for ((key, value) in httpEndpointRequest.queryComponents) {
							// Check value type
							if (value is List<*>) {
								// List
								when (this.multiValueQueryParameterHandling) {
									MultiValueQueryParameterHandling.REPEAT_KEY ->
										// Repeat key
										value.forEach() { uriBuilder = uriBuilder.appendQueryParameter(key, "$it") }

									MultiValueQueryParameterHandling.USE_COMMA -> {
										// Use comma
										var valuesString = ""
										value.withIndex().forEach() {
											// Update values string
											valuesString += if (it.index == 0) "${it.value}" else ",${it.value}"
										}
										uriBuilder = uriBuilder.appendQueryParameter(key, valuesString)
									}
								}
							} else
								// Value
								uriBuilder = uriBuilder.appendQueryParameter(key, "$value")
						}
					}

					// Add URL
					requestBuilder.url(uriBuilder.build().toString())
				}

				// Set method
				when (httpEndpointRequest.method) {
					HTTPEndpointMethod.GET -> requestBuilder.get()
					HTTPEndpointMethod.HEAD -> requestBuilder.head()
					HTTPEndpointMethod.PATCH -> requestBuilder.patch(httpEndpointRequest.bodyData!!.toRequestBody())
					HTTPEndpointMethod.POST ->
							requestBuilder.post((httpEndpointRequest.bodyData ?: ByteArray(0)).toRequestBody())
					HTTPEndpointMethod.PUT -> requestBuilder.put(httpEndpointRequest.bodyData!!.toRequestBody())
				}

				// Check if have headers
				if (httpEndpointRequest.headers != null)
					// Add headers
					for ((key, value) in httpEndpointRequest.headers) { requestBuilder.header(key, value) }

				// Set timeout
				var okHttpClient = this.okHttpClient
				if (httpEndpointRequest.timeoutInterval != 0.0)
					// Create new client with updated timeout
					okHttpClient =
							okHttpClient.newBuilder()
								.readTimeout((httpEndpointRequest.timeoutInterval * 1000.0).toLong(),
										TimeUnit.MILLISECONDS)
								.build()

				// Build request
				val request = requestBuilder.build()

				// Check if logging
				if (this.logTransactions)
					// Log
					Log.i("HTTPEndpointClient", "Sending request to " + request.url)

				// Queue
				this.okHttpClient.newCall(request).enqueue(object :Callback {
					override fun onResponse(call :Call, response :Response) {
						// Transition to finished
						httpEndpointRequest.transitionToState(HTTPEndpointRequest.State.FINISHED)

						// Check cancelled
						if (!httpEndpointRequest.isCancelled)
							// Process results
							httpEndpointRequest.processResults(response, null)

						// Update
						updateActiveHTTPEndpointRequests()
					}

					override fun onFailure(call :Call, e :IOException) {
						// Transition to finished
						httpEndpointRequest.transitionToState(HTTPEndpointRequest.State.FINISHED)

						// Check cancelled
						if (!httpEndpointRequest.isCancelled)
							// Process results
							httpEndpointRequest.processResults(null, e)

						// Update
						updateActiveHTTPEndpointRequests()
					}
				})
			}
		}

		// Resume
		this.updateActiveHTTPEndpointRequestsLock.unlock()
	}
}
