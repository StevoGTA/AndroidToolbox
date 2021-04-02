package codes.stevobrock.androidtoolbox.networking

import android.net.Uri
import codes.stevobrock.androidtoolbox.concurrency.LockingInt
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Response
import java.net.URLEncoder

//----------------------------------------------------------------------------------------------------------------------
abstract class HTTPEndpointRequest {

	// Types
	enum class BodyType {
		JSON,
		URLENCODED,
	}

	enum class State {
		QUEUED,
		ACTIVE,
		FINISHED,
	}

	data	class MultiValueQueryComponent(val key :String, val values :List<Any>)

	// Properties
	val	method :HTTPEndpointMethod
	val	path :String
	val	queryComponents :Map<String, Any>?
	val	multiValueQueryComponent :MultiValueQueryComponent?
	val	headers :Map<String, String>?
	val	timeoutInterval :Double
	val	bodyData :ByteArray?

	var	state = State.QUEUED
		private set
	var	isCancelled = false
		private set

	// Lifecycle methods
//	//------------------------------------------------------------------------------------------------------------------
//	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>?,
//			multiValueQueryComponent :MultiValueQueryComponent?, headers :Map<String, String>,
//			timeoutInterval :Double = defaultTimeout) {
//		// Store
//		this.method = method
//		this.path = path
//		this.queryComponents = queryComponents
//		this.multiValueQueryComponent = multiValueQueryComponent
//		this.headers = headers
//		this.timeoutInterval = timeoutInterval
//		this.bodyData = null
//	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>?,
			multiValueQueryComponent :MultiValueQueryComponent?, headers :Map<String, String>?,
			timeoutInterval :Double = defaultTimeout, bodyData :ByteArray?) {
		// Store
		this.method = method
		this.path = path
		this.queryComponents = queryComponents
		this.multiValueQueryComponent = multiValueQueryComponent
		this.headers = headers
		this.timeoutInterval = timeoutInterval
		this.bodyData = bodyData
	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>? = null,
			multiValueQueryComponent :MultiValueQueryComponent?, headers :Map<String, String>?,
			timeoutInterval :Double = defaultTimeout, body :Map<String, Any>, bodyType :BodyType) {
		// Setup
		val headersUse = if (headers != null) HashMap<String, String>(headers) else HashMap<String, String>()

		val bodyData :ByteArray
		when (bodyType) {
			BodyType.JSON -> {
				// JSON
				headersUse["Content-Type"] = "application/json"

				val	moshiBuilder = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
				val	type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
				val json = moshiBuilder.adapter<Map<String, Any>>(type).toJson(body)
				bodyData = json.toByteArray()
			}

			BodyType.URLENCODED -> {
				// URL Encoded
				headersUse["Content-Type"] = "application/x-www-form-urlencoded"

				bodyData =
						body
								.map({ "${it.key}=" + URLEncoder.encode("${it.value}", "utf-8") })
								.joinToString("&")
								.toByteArray()
			}
		}

		// Store
		this.method = method
		this.path = path
		this.queryComponents = queryComponents
		this.multiValueQueryComponent = multiValueQueryComponent
		this.headers = headersUse
		this.timeoutInterval = timeoutInterval
		this.bodyData = bodyData
	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, uri :Uri, headers :Map<String, String>?,
			timeoutInterval :Double = defaultTimeout, bodyData :ByteArray?) {
		// Store
		this.method = method
		this.path = uri.toString()
		this.queryComponents = null
		this.multiValueQueryComponent = null
		this.headers = headers
		this.timeoutInterval = timeoutInterval
		this.bodyData = bodyData
	}

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun cancel() { this.isCancelled = true }

	//------------------------------------------------------------------------------------------------------------------
	fun transitionToState(state :State) { this.state = state }

	// Companion object
	//------------------------------------------------------------------------------------------------------------------
	companion object {
		// Properties
		var	defaultTimeout :Double = 60.0
	}

	// Private classes
	//------------------------------------------------------------------------------------------------------------------
	class UnableToProcessResponseDataException : Exception()
}

//----------------------------------------------------------------------------------------------------------------------
interface HTTPEndpointRequestProcessResults {

	// Methods
	fun processResults(response :Response?, exception :Exception?)
}

//----------------------------------------------------------------------------------------------------------------------
interface HTTPEndpointRequestProcessMultiResults {

	// Methods
	fun processResults(response :Response?, exception :Exception?, totalRequests :Int)
}

//----------------------------------------------------------------------------------------------------------------------
typealias DataHTTPEndpointRequestCompletionProc = (response :Response?, data :ByteArray?, exception :Exception?) -> Unit

class DataHTTPEndpointRequest : HTTPEndpointRequest, HTTPEndpointRequestProcessResults {

	// Properties
	var	completionProc :DataHTTPEndpointRequestCompletionProc = { _,_,_ -> }

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>? = null,
			multiValueQueryComponent :MultiValueQueryComponent? = null, headers :Map<String, String>? = null,
			timeoutInterval :Double = defaultTimeout) :
		super(method, path, queryComponents, multiValueQueryComponent, headers, timeoutInterval, null)

	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod = HTTPEndpointMethod.GET, uri :Uri, headers :Map<String, String>? = null,
			timeoutInterval :Double = defaultTimeout, bodyData :ByteArray? = null) :
		super(method, uri, headers, timeoutInterval, bodyData)

	// HTTPEndpointRequestProcessResults methods
	//------------------------------------------------------------------------------------------------------------------
	override fun processResults(response :Response?, exception :Exception?) {
		// Check if cancelled
		if (!this.isCancelled)
			// Call completion proc
			this.completionProc(response, response?.body?.bytes(), exception)
	}
}

// TODO: FileHTTPEndpointRequest

//----------------------------------------------------------------------------------------------------------------------
typealias HeadHTTPEndpointRequestCompletionProc = (response :Response?, exception :Exception?) -> Unit

class HeadHTTPEndpointRequest : HTTPEndpointRequest, HTTPEndpointRequestProcessResults {

	// Properties
	var	completionProc :HeadHTTPEndpointRequestCompletionProc = { _,_ -> }

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>? = null,
			multiValueQueryComponent :MultiValueQueryComponent? = null, headers :Map<String, String>? = null,
			timeoutInterval :Double = defaultTimeout) :
		super(method, path, queryComponents, multiValueQueryComponent, headers, timeoutInterval, null)

	// HTTPEndpointRequestProcessResults methods
	//------------------------------------------------------------------------------------------------------------------
	override fun processResults(response :Response?, exception :Exception?) {
		// Check if cancelled
		if (!this.isCancelled)
			// Call completion proc
			// To get headers, use response?.headers?.toMap()
			this.completionProc(response, exception)
	}
}

//----------------------------------------------------------------------------------------------------------------------
typealias IntegerHTTPEndpointRequestCompletionProc = (response :Response?, value :Int?, exception :Exception?) -> Unit

class IntegerHTTPEndpointRequest : HTTPEndpointRequest, HTTPEndpointRequestProcessResults {

	// Properties
	var	completionProc :IntegerHTTPEndpointRequestCompletionProc = { _,_,_ -> }

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>? = null,
			multiValueQueryComponent :MultiValueQueryComponent? = null, headers :Map<String, String>? = null,
			timeoutInterval :Double = defaultTimeout) :
		super(method, path, queryComponents, multiValueQueryComponent, headers, timeoutInterval, null)

	// HTTPEndpointRequestProcessResults methods
	//------------------------------------------------------------------------------------------------------------------
	override fun processResults(response :Response?, exception :Exception?) {
		// Check if cancelled
		if (!this.isCancelled) {
			// Handle results
			val byteArray = response?.body?.bytes()
			if (byteArray != null)
				// Call completion
				this.completionProc(response, byteArray.toString(Charsets.UTF_8).toInt(), null)
			else
				// Error
				this.completionProc(response, null, exception)
		}
	}
}

//----------------------------------------------------------------------------------------------------------------------
typealias JSONHTTPEndpointRequestSingleResponseCompletionProc<T> =
			(response :Response?, info :T?, exception :Exception?) -> Unit
typealias JSONHTTPEndpointRequestMultiResponsePartialResultsProc<T> =
			(response :Response?, info :T?, exception :Exception?) -> Unit
typealias JSONHTTPEndpointRequestMultiResponseCompletionProc = (exceptions :ArrayList<Exception>) -> Unit

class JSONHTTPEndpointRequest<T :Any> : HTTPEndpointRequest, HTTPEndpointRequestProcessMultiResults {

	// Properties
			var	completionProc :JSONHTTPEndpointRequestSingleResponseCompletionProc<T>? = null
			var	multiResponsePartialResultsProc :JSONHTTPEndpointRequestMultiResponsePartialResultsProc<T>? = null
			var	multiResponseCompletionProc :JSONHTTPEndpointRequestMultiResponseCompletionProc? = null

	private	val	clazz :Class<T>
	private	val completedRequestsCount = LockingInt()
	private	val exceptions = ArrayList<Exception>()

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(clazz :Class<T>, method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>?,
			multiValueQueryComponent :MultiValueQueryComponent?, headers :Map<String, String>?,
			timeoutInterval :Double = defaultTimeout, bodyData :ByteArray?) :
			super(method, path, queryComponents, multiValueQueryComponent, headers, timeoutInterval, bodyData) {
		// Store
		this.clazz = clazz
	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(clazz :Class<T>, method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>?,
			multiValueQueryComponent :MultiValueQueryComponent?, headers :Map<String, String>?,
			timeoutInterval :Double = defaultTimeout, body :Map<String, Any>, bodyType :BodyType) :
			super(method, path, queryComponents, multiValueQueryComponent, headers, timeoutInterval, body, bodyType) {
		// Store
		this.clazz = clazz
	}

	// HTTPEndpointRequestProcessMultiResults methods
	//------------------------------------------------------------------------------------------------------------------
	override fun processResults(response :Response?, exception :Exception?, totalRequests :Int) {
		// Check if cancelled
		if (!this.isCancelled) {
			// Handle results
			var t :T? = null
			var procException = exception

			if (response?.body != null) {
				// Try to create object from response body
				val tAdapter = Moshi.Builder().build().adapter(this.clazz)

				try {
					// Create t
					t = tAdapter.fromJson(response.body!!.source())
					if (t == null)
						procException = UnableToDecodeJSONException()
				} catch (e :Exception) {
					// Error
					procException = e
				}
			} else
				// Error
				procException = exception

			// Check error
			if (procException != null) this.exceptions += procException

			// Call proc
			if (totalRequests == 1) {
				// Single request (but could have been multiple)
				if (this.completionProc != null)
					// Single response expected
					this.completionProc!!(response, t, procException)
				else {
					// Multi-response possible
					this.multiResponsePartialResultsProc!!(response, t, procException)
					this.multiResponseCompletionProc!!(this.exceptions)
				}
			} else {
				// Multiple requests
				this.multiResponsePartialResultsProc!!(response, t, procException)
				if (this.completedRequestsCount.add(1) == totalRequests)
					// All done
					this.multiResponseCompletionProc!!(this.exceptions)
			}
		}
	}

	// Private classes
	//------------------------------------------------------------------------------------------------------------------
	private class UnableToDecodeJSONException : Exception()
}

inline fun <reified T : Any> JSONHTTPEndpointRequest(method :HTTPEndpointMethod, path :String,
		queryComponents :Map<String, Any>? = null,
		multiValueQueryComponent :HTTPEndpointRequest.MultiValueQueryComponent? = null,
		headers :Map<String, String>? = null, timeoutInterval :Double = HTTPEndpointRequest.defaultTimeout,
		bodyData :ByteArray? = null) =
	JSONHTTPEndpointRequest(T::class.java, method, path, queryComponents, multiValueQueryComponent, headers,
			timeoutInterval, bodyData)

inline fun <reified T : Any> JSONHTTPEndpointRequest(method :HTTPEndpointMethod, path :String,
		queryComponents :Map<String, Any>? = null,
		multiValueQueryComponent :HTTPEndpointRequest.MultiValueQueryComponent? = null,
		headers :Map<String, String>? = null, timeoutInterval :Double = HTTPEndpointRequest.defaultTimeout,
		body :Map<String, Any>, bodyType :HTTPEndpointRequest.BodyType) =
	JSONHTTPEndpointRequest(T::class.java, method, path, queryComponents, multiValueQueryComponent, headers,
			timeoutInterval, body, bodyType)

//----------------------------------------------------------------------------------------------------------------------
typealias StringHTTPEndpointRequestCompletionProc =
			(response :Response?, string :String?, exception :Exception?) -> Unit

class StringHTTPEndpointRequest : HTTPEndpointRequest, HTTPEndpointRequestProcessResults {

	// Properties
	var	completionProc :StringHTTPEndpointRequestCompletionProc = { _,_,_ -> }

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>? = null,
			multiValueQueryComponent :MultiValueQueryComponent? = null, headers :Map<String, String>? = null,
			timeoutInterval :Double = defaultTimeout) :
		super(method, path, queryComponents, multiValueQueryComponent, headers, timeoutInterval, null)

	// HTTPEndpointRequestProcessResults methods
	//------------------------------------------------------------------------------------------------------------------
	override fun processResults(response :Response?, exception :Exception?) {
		// Check if cancelled
		if (!this.isCancelled) {
			// Handle results
			val byteArray = response?.body?.bytes()
			if (byteArray != null)
				// Call completion
				this.completionProc(response, byteArray.toString(Charsets.UTF_8), null)
			else
				// Error
				this.completionProc(response, null, exception)
		}
	}
}

//----------------------------------------------------------------------------------------------------------------------
typealias SuccessHTTPEndpointRequestCompletionProc = (response :Response?, exception :Exception?) -> Unit

class SuccessHTTPEndpointRequest : HTTPEndpointRequest, HTTPEndpointRequestProcessResults {

	// Properties
	var	completionProc :SuccessHTTPEndpointRequestCompletionProc = { _,_ -> }

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>? = null,
			multiValueQueryComponent :MultiValueQueryComponent? = null, headers :Map<String, String>? = null,
			timeoutInterval :Double = defaultTimeout) :
		super(method, path, queryComponents, multiValueQueryComponent, headers, timeoutInterval, null)

	// HTTPEndpointRequestProcessResults methods
	//------------------------------------------------------------------------------------------------------------------
	override fun processResults(response :Response?, exception :Exception?) {
		// Check if cancelled
		if (!this.isCancelled)
			// Call completion proc
			this.completionProc(response, exception)
	}
}
