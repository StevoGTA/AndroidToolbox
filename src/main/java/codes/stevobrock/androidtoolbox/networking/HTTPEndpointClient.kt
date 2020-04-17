package codes.stevobrock.androidtoolbox.networking

import android.net.Uri
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

//----------------------------------------------------------------------------------------------------------------------
open class HTTPEndpointClient(private val scheme :String, private val authority :String, private val path :String = "",
		private val multiValueQueryParameterHandling :MultiValueQueryParameterHandling =
				MultiValueQueryParameterHandling.REPEAT_KEY,
		val maximumURLLength :Int = 1024) {

	// Types
	enum class MultiValueQueryParameterHandling {
		USE_COMMA,
		REPEAT_KEY,
	}

	// Properties
			var	logTransactions = false

	private	val	okHttpClient :OkHttpClient = OkHttpClient()

	// Instance methods
	//------------------------------------------------------------------------------------------------------------------
	fun queue(httpEndpointRequest :HTTPEndpointRequest) {
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

			httpEndpointRequest.queryParameters?.forEach { key, value ->
				// Check value type
				if (value is List<*>) {
					// List
					when (this.multiValueQueryParameterHandling) {
						MultiValueQueryParameterHandling.REPEAT_KEY ->
							// Repeat key
							value.forEach { uriBuilder = uriBuilder.appendQueryParameter(key, "$it") }

						MultiValueQueryParameterHandling.USE_COMMA -> {
							// Use comma
							var	valuesString = ""
							value.withIndex().forEach {
								// Update values string
								valuesString += if (it.index == 0) "${it.value}" else ",${it.value}"
							}
							uriBuilder = uriBuilder.appendQueryParameter(key, valuesString)
						}
					}
				} else
					// Value
					uriBuilder = uriBuilder.appendQueryParameter(key, "{it.value}")
			}

			// Add URL
			requestBuilder.url(uriBuilder.build().toString())
		}

		// Set method
		when (httpEndpointRequest.method) {
			HTTPEndpointMethod.GET -> requestBuilder.get()
			HTTPEndpointMethod.HEAD -> requestBuilder.head()
			HTTPEndpointMethod.PATCH -> requestBuilder.patch(httpEndpointRequest.bodyData!!.toRequestBody())
			HTTPEndpointMethod.POST -> requestBuilder.post((httpEndpointRequest.bodyData ?: ByteArray(0)).toRequestBody())
			HTTPEndpointMethod.PUT -> requestBuilder.put(httpEndpointRequest.bodyData!!.toRequestBody())
		}

		// Add headers
		httpEndpointRequest.headers?.forEach { key, value -> requestBuilder.header(key, value) }

		// Set timeout
		var okHttpClient = this.okHttpClient
		if (httpEndpointRequest.timeoutInterval != 0.0)
			// Create new client with updated timeout
			okHttpClient =
					okHttpClient.newBuilder()
						.readTimeout((httpEndpointRequest.timeoutInterval * 1000.0).toLong(), TimeUnit.MILLISECONDS)
						.build()

		// Build request
		val request = requestBuilder.build()

		// Queue
		this.okHttpClient.newCall(request).enqueue(object :Callback {
			override fun onResponse(call :Call, response :Response) {
				// Process results
				httpEndpointRequest.processResults(response, null)
			}

			override fun onFailure(call :Call, e :IOException) {
				// Process results
				httpEndpointRequest.processResults(null, e)
			}
		})
	}
}
