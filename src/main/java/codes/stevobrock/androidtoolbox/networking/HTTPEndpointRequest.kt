package codes.stevobrock.androidtoolbox.networking

import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.squareup.moshi.Moshi
import okhttp3.Response
import java.net.URLEncoder

//----------------------------------------------------------------------------------------------------------------------
abstract class HTTPEndpointRequest {

	// Types
	enum class State {
		QUEUED,
		ACTIVE,
		FINISHED,
	}

	data class MultiValueQueryComponent(val key :String, val values :List<Any>)

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
//			timeoutInterval :Double) {
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
			multiValueQueryComponent :MultiValueQueryComponent?, headers :Map<String, String>?, timeoutInterval :Double,
			bodyData :ByteArray?) {
		// Store
		this.method = method
		this.path = path
		this.queryComponents = queryComponents
		this.multiValueQueryComponent = multiValueQueryComponent
		this.headers = headers
		this.timeoutInterval = timeoutInterval
		this.bodyData = bodyData
	}

//	//------------------------------------------------------------------------------------------------------------------
//	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>? = null,
//			multiValueQueryComponent :MultiValueQueryComponent?, headers :Map<String, String>?, timeoutInterval :Double,
//			jsonBody :Object) {
//		// Setup
//		var	headersUse = if (headers != null) HashMap<String, String>(headers!!) else HashMap<String, String>()
//		headersUse["application/json"] = "Content-Type"
//
//		val moshi = Moshi.Builder().build()
//		val	jsonString = moshi.adapter(HashMap<String, Any>.class).toJson(jsonBody)
//
//		// Store
//		this.method = method
//		this.path = path
//		this.queryComponents = queryComponents
//		this.multiValueQueryComponent = multiValueQueryComponent
//		this.headers = headersUse
//		this.timeoutInterval = timeoutInterval
//		this.bodyData = bodyData
//	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>? = null,
			multiValueQueryComponent :MultiValueQueryComponent?, headers :Map<String, String>?, timeoutInterval :Double,
			urlEncodedBody :Map<String, Any>) {
		// Setup
		var	headersUse = if (headers != null) HashMap<String, String>(headers) else HashMap<String, String>()
		headersUse["Content-Type"] = "application/x-www-form-urlencoded"

		// Store
		this.method = method
		this.path = path
		this.queryComponents = queryComponents
		this.multiValueQueryComponent = multiValueQueryComponent
		this.headers = headersUse
		this.timeoutInterval = timeoutInterval
		this.bodyData =
				urlEncodedBody
						.map({ "${it.key}=" + URLEncoder.encode("${it.value}", "utf-8") })
						.joinToString(separator = "&")
						.toByteArray()
	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, uri :Uri, timeoutInterval :Double) {
		// Store
		this.method = method
		this.path = uri.toString()
		this.queryComponents = null
		this.multiValueQueryComponent = null
		this.headers = null
		this.timeoutInterval = timeoutInterval
		this.bodyData = null
	}

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun cancel() { this.isCancelled = true }

	//------------------------------------------------------------------------------------------------------------------
	fun transitionToState(state :State) { this.state = state }

	// Subclass methods
	//------------------------------------------------------------------------------------------------------------------
	abstract fun processResults(response :Response?, exception :Exception?)
}

//----------------------------------------------------------------------------------------------------------------------
class SuccessHTTPEndpointRequest : HTTPEndpointRequest {

	// Properties
	var	completionProc :(Exception?) -> Unit = { _ -> }

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>? = null,
			multiValueQueryComponent :MultiValueQueryComponent? = null, headers :Map<String, String>? = null,
			timeoutInterval :Double = 0.0) :
		super(method, path, queryComponents, multiValueQueryComponent, headers, timeoutInterval, null)

	// HTTPEndpointRequest methods
	//------------------------------------------------------------------------------------------------------------------
	override fun processResults(response :Response?, exception :Exception?) {
		// Check if cancelled
		if (!this.isCancelled)
			// Call completion proc
			this.completionProc(exception)
	}
}

//----------------------------------------------------------------------------------------------------------------------
class HeadHTTPEndpointRequest : HTTPEndpointRequest {

	// Properties
	var	completionProc :(Map<String, String>?, Exception?) -> Unit = { _,_ -> }

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>? = null,
			multiValueQueryComponent :MultiValueQueryComponent? = null, headers :Map<String, String>? = null,
			timeoutInterval :Double = 0.0) :
		super(method, path, queryComponents, multiValueQueryComponent, headers, timeoutInterval, null)

	// HTTPEndpointRequest methods
	//------------------------------------------------------------------------------------------------------------------
	override fun processResults(response :Response?, exception :Exception?) {
		// Check if cancelled
		if (!this.isCancelled)
			// Call completion proc
			this.completionProc(response?.headers?.toMap(), exception)
	}
}

//----------------------------------------------------------------------------------------------------------------------
class DataHTTPEndpointRequest : HTTPEndpointRequest {

	// Properties
	var	completionProc :(ByteArray?, Exception?) -> Unit = { _,_ -> }

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>? = null,
			multiValueQueryComponent :MultiValueQueryComponent? = null, headers :Map<String, String>? = null,
			timeoutInterval :Double = 0.0) :
		super(method, path, queryComponents, multiValueQueryComponent, headers, timeoutInterval, null)

	//------------------------------------------------------------------------------------------------------------------
	constructor(uri :Uri, method :HTTPEndpointMethod = HTTPEndpointMethod.GET, timeoutInterval :Double = 0.0) :
		super(method, uri, timeoutInterval)

	// HTTPEndpointRequest methods
	//------------------------------------------------------------------------------------------------------------------
	override fun processResults(response :Response?, exception :Exception?) {
		// Check if cancelled
		if (!this.isCancelled)
			// Call completion proc
			this.completionProc(response?.body?.bytes(), exception)
	}
}

//----------------------------------------------------------------------------------------------------------------------
class StringHTTPEndpointRequest : HTTPEndpointRequest {

	// Properties
	var	completionProc :(String?, Exception?) -> Unit = { _,_ -> }

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>? = null,
			multiValueQueryComponent :MultiValueQueryComponent? = null, headers :Map<String, String>? = null,
			timeoutInterval :Double = 0.0) :
		super(method, path, queryComponents, multiValueQueryComponent, headers, timeoutInterval, null)

	// HTTPEndpointRequest methods
	//------------------------------------------------------------------------------------------------------------------
	override fun processResults(response :Response?, exception :Exception?) {
		// Check if cancelled
		if (!this.isCancelled) {
			// Handle results
			val byteArray = response?.body?.bytes()
			if (byteArray != null)
				// Call completion
				this.completionProc(byteArray.toString(Charsets.UTF_8), null)
			else
				// Error
				this.completionProc(null, exception)
		}
	}
}

//----------------------------------------------------------------------------------------------------------------------
class JSONHTTPEndpointRequest<T :Any> : HTTPEndpointRequest {

	// Properties
			var	completionProc :(T?, Exception?) -> Unit = { _,_ -> }

	private	val	clazz :Class<T>

	// Lifecycle methods
	//------------------------------------------------------------------------------------------------------------------
	constructor(clazz :Class<T>, method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>?,
			multiValueQueryComponent :MultiValueQueryComponent?, headers :Map<String, String>?, timeoutInterval :Double,
			bodyData :ByteArray?) :
			super(method, path, queryComponents, multiValueQueryComponent, headers, timeoutInterval, bodyData) {
		// Store
		this.clazz = clazz
	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(clazz :Class<T>, method :HTTPEndpointMethod, path :String, queryComponents :Map<String, Any>?,
			multiValueQueryComponent :MultiValueQueryComponent?, headers :Map<String, String>?, timeoutInterval :Double,
			urlEncodedBody :Map<String, Any>) :
			super(method, path, queryComponents, multiValueQueryComponent, headers, timeoutInterval, urlEncodedBody) {
		// Store
		this.clazz = clazz
	}

	// HTTPEndpointRequest methods
	//------------------------------------------------------------------------------------------------------------------
	override fun processResults(response :Response?, exception :Exception?) {
		// Check if cancelled
		if (!this.isCancelled) {
			// Handle results
			var t :T? = null
			var exceptionUse = exception

			if (response?.body != null) {
				// Try to create object from response body
				val moshi = Moshi.Builder().build()
				val tAdapter = moshi.adapter(this.clazz)

				try {
					// Create t
					t = tAdapter.fromJson(response.body!!.source())
					if (t == null)
						exceptionUse = UnableToDecodeJSONException()
				} catch (e :Exception) {
					// Error
					exceptionUse = e
				}
			}

			// Call completion on the main thread
			Handler(Looper.getMainLooper()).post(Runnable { this.completionProc(t, exceptionUse) })
		}
	}

	// Private classes
	//------------------------------------------------------------------------------------------------------------------
	private class UnableToDecodeJSONException : Exception()
}

//----------------------------------------------------------------------------------------------------------------------
inline fun <reified T : Any> JSONHTTPEndpointRequest(method :HTTPEndpointMethod, path :String,
		queryComponents :Map<String, Any>? = null,
		multiValueQueryComponent :HTTPEndpointRequest.MultiValueQueryComponent? = null,
		headers :Map<String, String>? = null, timeoutInterval :Double = 0.0, bodyData :ByteArray? = null) =
	JSONHTTPEndpointRequest(T::class.java, method, path, queryComponents, multiValueQueryComponent, headers,
			timeoutInterval, bodyData)

//----------------------------------------------------------------------------------------------------------------------
inline fun <reified T : Any> JSONHTTPEndpointRequest(method :HTTPEndpointMethod, path :String,
		queryComponents :Map<String, Any>? = null,
		multiValueQueryComponent :HTTPEndpointRequest.MultiValueQueryComponent? = null,
		headers :Map<String, String>? = null, timeoutInterval :Double = 0.0, urlEncodedBody :Map<String, Any>) =
	JSONHTTPEndpointRequest(T::class.java, method, path, queryComponents, multiValueQueryComponent, headers,
			timeoutInterval, urlEncodedBody)
