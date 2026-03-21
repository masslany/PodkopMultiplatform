package pl.masslany.podkop.common.network.infrastructure.main.mapper

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.util.reflect.TypeInfo
import pl.masslany.podkop.common.network.models.request.Request

fun <T> Request<T>.toHttpRequestBuilder(): HttpRequestBuilder {
    val param = this
    return HttpRequestBuilder().apply {
        val formattedPath =
            when {
                param.path.contains("://") || param.path.startsWith("/") -> param.path
                else -> "/${param.path}"
            }
        url(formattedPath)

        method = param.method.toKtorHttpMethod()

        param.queryParameters?.forEach { queryParameter ->
            parameter(queryParameter.key, queryParameter.value)
        }

        param.queryParameterPairs?.forEach { (key, value) ->
            parameter(key, value)
        }

        param.headers?.forEach { (key, value) ->
            header(key, value)
        }

        param.body?.let { requestBody ->
            if (requestBody is MultiPartFormDataContent) {
                headers.remove(HttpHeaders.ContentType)
            }
            val requestBodyType =
                TypeInfo(
                    type = requestBody::class,
                )

            setBody(requestBody, requestBodyType)
        }
    }
}

private fun Request.HttpMethod.toKtorHttpMethod(): HttpMethod {
    return when (this) {
        Request.HttpMethod.GET -> HttpMethod.Get
        Request.HttpMethod.DELETE -> HttpMethod.Delete
        Request.HttpMethod.PATCH -> HttpMethod.Patch
        Request.HttpMethod.POST -> HttpMethod.Post
        Request.HttpMethod.PUT -> HttpMethod.Put
    }
}
