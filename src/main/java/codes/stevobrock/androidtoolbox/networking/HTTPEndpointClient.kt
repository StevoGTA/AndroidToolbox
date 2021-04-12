package codes.stevobrock.androidtoolbox.networking

import android.net.Uri
import android.util.Log
import codes.stevobrock.androidtoolbox.concurrency.LockingArrayList
import codes.stevobrock.androidtoolbox.concurrency.LockingInt
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

//----------------------------------------------------------------------------------------------------------------------
private fun String.urlQueryEncode(encodePlus :Boolean) :String {
	// Check options
	if (encodePlus)
		// Percent-encode +
		return this
				.replace("%", "%25")
				.replace("!", "%21")
				.replace("#", "%23")
				.replace("$", "%24")
				.replace("&", "%26")
				.replace("\'", "%27")
				.replace("(", "%28")
				.replace(")", "%29")
				.replace("*", "%2A")
				.replace(",", "%2C")
				.replace(":", "%3A")
				.replace(";", "%3B")
				.replace("=", "%3D")
				.replace("?", "%3F")
				.replace("@", "%40")
				.replace("[", "%5B")
				.replace("]", "%5D")
				.replace("+", "%2B")
	else
		// Pass-through +
		return this
				.replace("%", "%25")
				.replace("!", "%21")
				.replace("#", "%23")
				.replace("$", "%24")
				.replace("&", "%26")
				.replace("\'", "%27")
				.replace("(", "%28")
				.replace(")", "%29")
				.replace("*", "%2A")
				.replace(",", "%2C")
				.replace(":", "%3A")
				.replace(";", "%3B")
				.replace("=", "%3D")
				.replace("?", "%3F")
				.replace("@", "%40")
				.replace("[", "%5B")
				.replace("]", "%5D")
}

//----------------------------------------------------------------------------------------------------------------------
private fun HTTPEndpointRequest.requests(scheme :String, authority :String, port :Int?,
		options :Set<HTTPEndpointClient.Options>, maximumUriLength :Int) :ArrayList<Request> {
	// Setup
	val requestBuilder = Request.Builder()
	when (this.method) {
		HTTPEndpointMethod.GET -> requestBuilder.get()
		HTTPEndpointMethod.HEAD -> requestBuilder.head()
		HTTPEndpointMethod.PATCH -> requestBuilder.patch(this.bodyData!!.toRequestBody())
		HTTPEndpointMethod.POST ->
				requestBuilder.post((this.bodyData ?: ByteArray(0)).toRequestBody())
		HTTPEndpointMethod.PUT -> requestBuilder.put(this.bodyData!!.toRequestBody())
	}
	if (this.headers != null)
		// Add headers
		for ((key, value) in this.headers) { requestBuilder.header(key, value) }

	// Build requests
	val requests = ArrayList<Request>()
	if (this.path.startsWith("http") || this.path.startsWith("https"))
		// Already have fully-formed URL
		requests.add(requestBuilder.url(this.path).build())
	else {
		// Compose Requests
		val	uriBuilder :Uri.Builder? =
					Uri.Builder()
						.scheme(scheme)
						.encodedAuthority(if (port != null) "$authority:$port!!" else authority)
						.path(this.path)

		val	queryComponents =
					this.queryComponents?.map() {
						"$it.key=$it.value"
								.urlQueryEncode(
										options.contains(HTTPEndpointClient.Options.PERCENT_ENCODE_PLUS_CHARACTER))
					}
		val	queryString = (queryComponents ?: arrayListOf())!!.joinToString("&")
		val	hasQuery = queryString.isNotEmpty() || (this.multiValueQueryComponent != null)
		val	urlRoot = uriBuilder!!.build().toString() + (if (hasQuery) "?" else "") + queryString

		// Check if have multi-value query parameters
		if ((this.multiValueQueryComponent != null) && this.multiValueQueryComponent.values.isNotEmpty()){
			// Process multi-value query component
			val	key = URLEncoder.encode(this.multiValueQueryComponent.key, "UTF-8")
			val values =
						this.multiValueQueryComponent.values.map() {
							// Check options
							"$it"
									.urlQueryEncode(
											options.contains(HTTPEndpointClient.Options.PERCENT_ENCODE_PLUS_CHARACTER))
						}

			// Check options
			var queryComponent = ""
			if (options.contains(HTTPEndpointClient.Options.MULTI_VALUE_QUERY_USE_COMMA)) {
				// Use comma
				val	urlBase = if (queryString.isNotEmpty()) "$urlRoot&$key=" else "$urlRoot?$key="
				values.forEach() {
					// Compose string with next value
					val queryComponentTry = if (queryComponent.isNotEmpty()) "$queryComponent,$it" else it
					if ((urlBase.length + queryComponentTry.length) <= maximumUriLength)
						// We good
						queryComponent = queryComponentTry
					else {
						// Generate Request
						requests.add(requestBuilder.url(urlBase + queryComponent).build())

						// Restart
						queryComponent = it
					}
				}

				// Generate final Request
				requests.add(requestBuilder.url(urlBase + queryComponent).build())
			} else {
				// Repeat key
				val	urlBase = if (queryString.isNotEmpty()) "$urlRoot&" else "$urlRoot?"
				values.forEach() {
					// Check if can add
					val queryComponentTry = if (queryComponent.isNotEmpty()) "$queryComponent&$key=$it" else "$key=$it"
					if ((urlBase.length + queryComponentTry.length) <= maximumUriLength)
						// We good
						queryComponent = queryComponentTry
					else {
						// Generate Request
						requests.add(requestBuilder.url(urlBase + queryComponent).build())

						// Restart
						queryComponent = "$key=$it"
					}
				}

				// Generate final Request
				requests.add(requestBuilder.url(urlBase + queryComponent).build())
			}
		}

		// Check if have any Requests
		if (requests.isEmpty())
			// Generate Request
			requests.add(requestBuilder.url(urlRoot).build())
	}

	return requests
}

//----------------------------------------------------------------------------------------------------------------------
open class HTTPEndpointClient(private val scheme :String, private val authority :String, private val port :Int? = null,
		private val options :Set<Options> = HashSet(), private val maximumUriLength :Int = 1024,
		private val maximumConcurrentHTTPEndpointRequests :Int = 5) {
	// Types
	enum class Options(val value :Int) {
		MULTI_VALUE_QUERY_USE_COMMA(1 shl 0),
		PERCENT_ENCODE_PLUS_CHARACTER(1 shl 1),
	}

	enum class Priority(val value :Int) {
		NORMAL(0),
		BACKGROUND(1),
	}

	enum class LogOptions(val value :Int) {
		REQUEST_AND_RESPONSE(1 shl 0),
		REQUEST_QUERY(1 shl 1),
		REQUEST_HEADERS(1 shl 2),
		REQUEST_BODY(1 shl 3),
		REQUEST_BODY_SIZE(1 shl 4),
		RESPONSE_HEADERS(1 shl 5),
		RESPONSE_BODY(1 shl 6),
	}

	//------------------------------------------------------------------------------------------------------------------
	class HTTPEndpointRequestInfo(val httpEndpointRequest :HTTPEndpointRequest, val identifier :String,
			val priority :Priority) {
		// Properties
		private	var	totalPerformInfosCount = 0
		private	var	finishedPerformInfosCount = LockingInt()

		// Instance methods
		//--------------------------------------------------------------------------------------------------------------
		fun httpEndpointRequestPerformInfos(scheme :String, authority :String, port : Int?,
											options :Set<HTTPEndpointClient.Options>, maximumUriLength :Int)
				:List<HTTPEndpointRequestPerformInfo> {
			// Setup
			val requests = this.httpEndpointRequest.requests(scheme, authority, port, options, maximumUriLength)
			this.totalPerformInfosCount = requests.size

			// Check HTTPEndpointRequest type
			if (this.httpEndpointRequest is HTTPEndpointRequestProcessResults) {
				// Will only ever be a single Request
				val	httpEndpointRequestProcessResults = this.httpEndpointRequest as HTTPEndpointRequestProcessResults

				return requests.map() {
					HTTPEndpointRequestPerformInfo(this, it) { response, exception ->
						// Process results
						httpEndpointRequestProcessResults.processResults(response, exception)
					}
				}
			} else {
				// Can end up being multiple Requests
				val	httpEndpointRequestProcessMultiResults =
							this.httpEndpointRequest as HTTPEndpointRequestProcessMultiResults
				val	requestsCount = requests.size

				return requests.map() {
					HTTPEndpointRequestPerformInfo(this, it) { response, exception ->
						// Process results
						httpEndpointRequestProcessMultiResults.processResults(response, exception, requestsCount)
					}
				}
			}
		}

		//--------------------------------------------------------------------------------------------------------------
		fun transitionToState(state :HTTPEndpointRequest.State) {
			// Check state
			if ((state == HTTPEndpointRequest.State.ACTIVE) &&
					(this.httpEndpointRequest.state == HTTPEndpointRequest.State.QUEUED))
				// Transition to active
				this.httpEndpointRequest.transitionToState(HTTPEndpointRequest.State.ACTIVE)
			else if (state == HTTPEndpointRequest.State.FINISHED) {
				// One more finished
				if (this.finishedPerformInfosCount.add(1) == this.totalPerformInfosCount)
					// Finished finished
					this.httpEndpointRequest.transitionToState(HTTPEndpointRequest.State.FINISHED)
			}
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	class HTTPEndpointRequestPerformInfo(private val httpEndpointRequestInfo :HTTPEndpointRequestInfo,
			val request :Request, private val completionProc :(response :Response?, exception :Exception?) -> Unit) {
		// Properties
		val identifier :String = this.httpEndpointRequestInfo.identifier
		val priority :Priority = this.httpEndpointRequestInfo.priority
		val	timeoutInterval :Double = this.httpEndpointRequestInfo.httpEndpointRequest.timeoutInterval
		val isCancelled :Boolean = this.httpEndpointRequestInfo.httpEndpointRequest.isCancelled

		var	state = HTTPEndpointRequest.State.QUEUED
			private set

		// Instance Methods
		//--------------------------------------------------------------------------------------------------------------
		fun transitionToState(state :HTTPEndpointRequest.State) {
			// Update state
			this.state = state

			// Inform HTTPEndpointRequestInfo
			this.httpEndpointRequestInfo.transitionToState(state)
		}

		//--------------------------------------------------------------------------------------------------------------
		fun cancel() { this.httpEndpointRequestInfo.httpEndpointRequest.cancel() }

		//--------------------------------------------------------------------------------------------------------------
		fun processResults(response :Response?, exception :Exception?) {
			// Process results
			if (response != null) {
				// Have a response
				val statusCode = response.code
				if (statusCode == HTTPEndpointStatus.OK.value)
					// Success
					this.completionProc(response, null)
				else
					// Some other response
					this.completionProc(response, HTTPEndpointStatusException(HTTPEndpointStatus.from(statusCode)!!))
			} else
				// Exception
				this.completionProc(response, exception)
		}
	}

	// Properties
			var	logOptions = HashSet<LogOptions>()

	private	val	okHttpClient :OkHttpClient = OkHttpClient()

	private	val	updateActiveHTTPEndpointRequestPerformInfosLock = ReentrantLock()

	private	var	activeHTTPEndpointRequestPerformInfos = LockingArrayList<HTTPEndpointRequestPerformInfo>()
	private	var	queuedHTTPEndpointRequestPerformInfos = LockingArrayList<HTTPEndpointRequestPerformInfo>()
	private	var	requestIndex = 0

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun queue(httpEndpointRequest :HTTPEndpointRequest, identifier :String = "", priority :Priority = Priority.NORMAL) {
		// Add to queue
		val httpEndpointRequestInfo = HTTPEndpointRequestInfo(httpEndpointRequest, identifier, priority)
		this.queuedHTTPEndpointRequestPerformInfos.add(
				httpEndpointRequestInfo.httpEndpointRequestPerformInfos(this.scheme, this.authority, this.port,
						this.options, this.maximumUriLength))

		// Update active
		updateHTTPEndpointRequestPerformInfos()
	}

	//------------------------------------------------------------------------------------------------------------------
	fun queue(dataHTTPEndpointRequest :DataHTTPEndpointRequest, identifier :String = "",
			priority :Priority = Priority.NORMAL, completionProc :DataHTTPEndpointRequestCompletionProc) {
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
			priority :Priority = Priority.NORMAL, completionProc :HeadHTTPEndpointRequestCompletionProc) {
		// Setup
		headHTTPEndpointRequest.completionProc = completionProc

		// Queue
		queue(headHTTPEndpointRequest, identifier, priority)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun queue(integerHTTPEndpointRequest :IntegerHTTPEndpointRequest, identifier :String = "",
			priority :Priority = Priority.NORMAL, completionProc :IntegerHTTPEndpointRequestCompletionProc) {
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
			priority :Priority = Priority.NORMAL, completionProc :StringHTTPEndpointRequestCompletionProc) {
		// Setup
		stringHTTPEndpointRequest.completionProc = completionProc

		// Queue
		queue(stringHTTPEndpointRequest, identifier, priority)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun queue(successHTTPEndpointRequest :SuccessHTTPEndpointRequest, identifier :String = "",
			priority :Priority = Priority.NORMAL, completionProc :SuccessHTTPEndpointRequestCompletionProc) {
		// Setup
		successHTTPEndpointRequest.completionProc = completionProc

		// Queue
		queue(successHTTPEndpointRequest, identifier, priority)
	}

	//------------------------------------------------------------------------------------------------------------------
	fun cancel(identifier :String) {
		// One at a time please...
		this.updateActiveHTTPEndpointRequestPerformInfosLock.lock()

		// Iterate all
		this.activeHTTPEndpointRequestPerformInfos.forEach() {
			// Check identifier
			if (it.identifier == identifier)
				// Identifier matches, cancel
				it.cancel()
		}
		this.queuedHTTPEndpointRequestPerformInfos.removeIf() {
			// Check identifier
			if (it.identifier == identifier) {
				// Identifier matches, cancel
				it.cancel()

				true
			} else
				// No match
				false
		}

		// Resume
		this.updateActiveHTTPEndpointRequestPerformInfosLock.unlock()
	}

	// Private methods
	//------------------------------------------------------------------------------------------------------------------
	private fun updateHTTPEndpointRequestPerformInfos() {
		// One at a time please...
		this.updateActiveHTTPEndpointRequestPerformInfosLock.lock()

		// Remove finished
		this.activeHTTPEndpointRequestPerformInfos.removeIf() { it.state == HTTPEndpointRequest.State.FINISHED }

		// Check if have available "slots"
		if (this.activeHTTPEndpointRequestPerformInfos.size < this.maximumConcurrentHTTPEndpointRequests) {
			// Sort queued
			this.queuedHTTPEndpointRequestPerformInfos.sortWith(Comparator() { info1, info2 ->
				// Compare priority
				if (info1.priority.value > info2.priority.value) return@Comparator -1 else return@Comparator 0
			})

			// Activate up to the maximum
			while ((this.queuedHTTPEndpointRequestPerformInfos.size > 0) &&
					(this.activeHTTPEndpointRequestPerformInfos.size < this.maximumConcurrentHTTPEndpointRequests)) {
				// Get first queued
				val httpEndpointRequestPerformInfo = this.queuedHTTPEndpointRequestPerformInfos.removeFirst()
				if (httpEndpointRequestPerformInfo.isCancelled)
					// Skip
					continue

				val request = httpEndpointRequestPerformInfo.request

				val requestIndex = this.requestIndex
				this.requestIndex++

				// Make active
				httpEndpointRequestPerformInfo.transitionToState(HTTPEndpointRequest.State.ACTIVE)
				this.activeHTTPEndpointRequestPerformInfos.add(httpEndpointRequestPerformInfo)

				// Create new client with timeout
				val okHttpClient =
							this.okHttpClient.newBuilder()
								.readTimeout((httpEndpointRequestPerformInfo.timeoutInterval * 1000.0).toLong(),
										TimeUnit.MILLISECONDS)
								.build()

				// Log
				val	logOptions = this.logOptions
				val	requestInfo = "${request.url.host}:${request.url.encodedPath} ($requestIndex)"
				if (logOptions.contains(LogOptions.REQUEST_AND_RESPONSE)) {
					// Setup
					val logMessages = ArrayList<String>()

					// Log request
					logMessages.add("$this.javaClass.simpleName: $request.method to $requestInfo")
					if (logOptions.contains(LogOptions.REQUEST_QUERY))
						// Log query
						logMessages.add("    Query: " + (request.url.query ?: "n/a"))
					if (logOptions.contains(LogOptions.REQUEST_HEADERS))
						// Log headers
						logMessages.add("    Headers: ${request.headers}")
					if (logOptions.contains(LogOptions.REQUEST_BODY))
						// Log body
						logMessages.add(
								"    Body: " +
										if (request.body != null)
											request.body?.toString() ?: "unable to decode"
										else
											"")
					if (logOptions.contains(LogOptions.REQUEST_BODY_SIZE))
						// Log body size
						logMessages.add(
								"    Body size: " + (request.body ?: ByteArray(0).toRequestBody()).contentLength())
					logProc(logMessages)
				}

				// Queue
				val	startSeconds = (Date().time * 1000).toDouble()
				okHttpClient.newCall(request).enqueue(object :Callback {
					override fun onResponse(call :Call, response :Response) {
						// Log
						if (logOptions.contains(LogOptions.REQUEST_AND_RESPONSE)) {
							// Setup
							val	deltaSeconds = (Date().time * 1000).toDouble() - startSeconds
							val logMessages = ArrayList<String>()

							// Log response
							val	deltaSecondsString = String.format("%0.3f", deltaSeconds)
							logMessages.add(
									"    $this.javaClass.simpleName received status $response.code for $requestInfo in ${deltaSecondsString}s")
							if (logOptions.contains(LogOptions.RESPONSE_HEADERS))
								// Log headers
								logMessages.add("        Headers: ${response.headers}")
							if (logOptions.contains(LogOptions.RESPONSE_BODY))
								// Log body
								logMessages.add(
										"        Body: " +
												if (response.body != null)
													response.body?.toString() ?: "unable to decode"
												else
													"")
							logProc(logMessages)
						}

						// Transition to finished
						httpEndpointRequestPerformInfo.transitionToState(HTTPEndpointRequest.State.FINISHED)

						// Check cancelled
						if (!httpEndpointRequestPerformInfo.isCancelled)
							// Process results
							httpEndpointRequestPerformInfo.processResults(response, null)

						// Update
						updateHTTPEndpointRequestPerformInfos()
					}

					override fun onFailure(call :Call, exception :IOException) {
						// Log
						if (logOptions.contains(LogOptions.REQUEST_AND_RESPONSE)) {
							// Log
							val	logMessages = ArrayList<String>()
							logMessages.add("    $this.javaClass.simpleName received exception $exception for $requestInfo")
							logProc(logMessages)
						}

						// Transition to finished
						httpEndpointRequestPerformInfo.transitionToState(HTTPEndpointRequest.State.FINISHED)

						// Check cancelled
						if (!httpEndpointRequestPerformInfo.isCancelled)
							// Process results
							httpEndpointRequestPerformInfo.processResults(null, exception)

						// Update
						updateHTTPEndpointRequestPerformInfos()
					}
				})
			}
		}

		// Resume
		this.updateActiveHTTPEndpointRequestPerformInfosLock.unlock()
	}

	// Companion object
	//------------------------------------------------------------------------------------------------------------------
	companion object {
		// Properties
		var	logProc :(messages :List<String>) -> Unit = { messages ->
					// Iterate messages and log
					messages.forEach() { Log.i("HTTPEndpointClient", it) }
				}
	}
}
