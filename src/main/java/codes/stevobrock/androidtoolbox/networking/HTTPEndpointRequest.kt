package codes.stevobrock.androidtoolbox.networking

import android.os.Handler
import android.os.Looper
import com.squareup.moshi.Moshi
import okhttp3.Response

//----------------------------------------------------------------------------------------------------------------------
abstract class HTTPEndpointRequest {

	// Properties
	val	method :HTTPEndpointMethod
	val	path :String
	val	queryParameters :Map<String, Any>?
	val	headers :Map<String, String>?
	val	timeoutInterval :Double
	val	bodyData :ByteArray?

	var	isCancelled = false
		private set

	// Lifecycle methods
//	//------------------------------------------------------------------------------------------------------------------
//	constructor(method :HTTPEndpointMethod, path :String, queryParameters :Map<String, Any>?,
//			headers :Map<String, String>, timeoutInterval :Double) {
//		// Store
//		this.method = method
//		this.path = path
//		this.queryParameters = queryParameters
//		this.headers = headers
//		this.timeoutInterval = timeoutInterval
//		this.bodyData = null
//	}

	//------------------------------------------------------------------------------------------------------------------
	constructor(method :HTTPEndpointMethod, path :String, queryParameters :Map<String, Any>?,
				headers :Map<String, String>?, timeoutInterval :Double, bodyData :ByteArray?) {
		// Store
		this.method = method
		this.path = path
		this.queryParameters = queryParameters
		this.headers = headers
		this.timeoutInterval = timeoutInterval
		this.bodyData = bodyData
	}

//	//------------------------------------------------------------------------------------------------------------------
//	constructor(method :HTTPEndpointMethod, path :String, queryParameters :Map<String, Any>? = null,
//			headers :Map<String, String>?, timeoutInterval :Double, json :Object) {
//		// Setup
//		var	headersUse = if (headers != null) HashMap<String, String>(headers!!) else HashMap<String, String>()
//		headersUse.put("application/json", "Content-Type")
//
//		// Store
//		this.method = method
//		this.path = path
//		this.queryParameters = queryParameters
//		this.headers = headersUse
//		this.timeoutInterval = timeoutInterval
//		this.bodyData = bodyData
//	}

//	//------------------------------------------------------------------------------------------------------------------
//	constructor(method :HTTPEndpointMethod = HTTPEndpointMethod.GET, url :URL, timeoutInterval :Double) {
//		// Store
//		this.method = method
//		this.path = url.toString()
//		this.queryParameters = null
//		this.headers = null
//		this.timeoutInterval = timeoutInterval
//		this.bodyData = null
//	}

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun cancel() { this.isCancelled = true }

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
	constructor(method :HTTPEndpointMethod, path :String, queryParameters :Map<String, Any>? = null,
				headers :Map<String, String>? = null, timeoutInterval :Double = 0.0) :
		super(method, path, queryParameters, headers, timeoutInterval, null)

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
	constructor(method :HTTPEndpointMethod, path :String, queryParameters :Map<String, Any>? = null,
				headers :Map<String, String>? = null, timeoutInterval :Double = 0.0) :
		super(method, path, queryParameters, headers, timeoutInterval, null)

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
	constructor(method :HTTPEndpointMethod, path :String, queryParameters :Map<String, Any>? = null,
				headers :Map<String, String>? = null, timeoutInterval :Double = 0.0) :
		super(method, path, queryParameters, headers, timeoutInterval, null)

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
	constructor(method :HTTPEndpointMethod, path :String, queryParameters :Map<String, Any>? = null,
				headers :Map<String, String>? = null, timeoutInterval :Double = 0.0) :
		super(method, path, queryParameters, headers, timeoutInterval, null)

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
	constructor(clazz :Class<T>, method :HTTPEndpointMethod, path :String, queryParameters :Map<String, Any>?,
				headers :Map<String, String>?, timeoutInterval :Double) :
		super(method, path, queryParameters, headers, timeoutInterval, null) { this.clazz = clazz }

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
		queryParameters :Map<String, Any>? = null, headers :Map<String, String>? = null,
		timeoutInterval :Double = 0.0) =
	JSONHTTPEndpointRequest(T::class.java, method, path, queryParameters, headers, timeoutInterval)
