package net.igorilic.didyoubuyit.helper

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import java.io.*
import kotlin.math.min

open class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val listener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) : Request<String?>(method, url, errorListener) {

    private val twoHyphens = "--"
    private val lineEnd = "\r\n"
    private val boundary: String = "apiclient-" + System.currentTimeMillis()

    override fun getBodyContentType(): String? {
        return "multipart/form-data;boundary=$boundary"
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray? {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)
        try {
            // populate text payload
            val params = params
            if (params != null && params.isNotEmpty()) {
                textParse(dos, params, paramsEncoding)
            }

            // populate data byte payload
            val data: Map<String, DataPart>? = getByteData()
            data?.let {
                if (it.isNotEmpty()) {
                    dataParse(dos, it)
                }
            }

            // close multipart form data after text and file data
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
            return bos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(AuthFailureError::class)
    open fun getByteData(): Map<String, DataPart>? {
        return null
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<String?>? {
        return try {
            Response.success(
                response.data.toString(),
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: Exception) {
            Response.error(ParseError(e))
        }
    }

    override fun deliverResponse(response: String?) {
        listener.onResponse(response)
    }


    override fun deliverError(error: VolleyError?) {
        errorListener!!.onErrorResponse(error)
    }

    @Throws(IOException::class)
    private fun textParse(
        dataOutputStream: DataOutputStream,
        params: Map<String, String>,
        encoding: String
    ) {
        try {
            for ((key, value) in params) {
                buildTextPart(dataOutputStream, key, value)
            }
        } catch (uee: UnsupportedEncodingException) {
            throw RuntimeException("Encoding not supported: $encoding", uee)
        }
    }

    @Throws(IOException::class)
    private fun dataParse(dataOutputStream: DataOutputStream, data: Map<String, DataPart>) {
        for ((key, value) in data) {
            buildDataPart(dataOutputStream, value, key)
        }
    }

    @Throws(IOException::class)
    private fun buildTextPart(
        dataOutputStream: DataOutputStream,
        parameterName: String,
        parameterValue: String
    ) {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd)
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"$parameterName\"$lineEnd")
        dataOutputStream.writeBytes(lineEnd)
        dataOutputStream.writeBytes(parameterValue + lineEnd)
    }

    @Throws(IOException::class)
    private fun buildDataPart(
        dataOutputStream: DataOutputStream,
        dataFile: DataPart,
        inputName: String
    ) {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd)
        dataOutputStream.writeBytes(
            "Content-Disposition: form-data; name=\"" +
                    inputName + "\"; filename=\"" + dataFile.getFileName() + "\"" + lineEnd
        )

        if (
            dataFile.getType() != null &&
            dataFile.getType() is String &&
            (dataFile.getType() as String).trim().isNotEmpty()
        ) {
            dataOutputStream.writeBytes("Content-Type: " + dataFile.getType().toString() + lineEnd)
        }

        dataOutputStream.writeBytes(lineEnd)
        val fileInputStream = ByteArrayInputStream(dataFile.getContent())
        var bytesAvailable: Int = fileInputStream.available()
        val maxBufferSize = 1024 * 1024
        var bufferSize = min(bytesAvailable, maxBufferSize)
        val buffer = ByteArray(bufferSize)
        var bytesRead: Int = fileInputStream.read(buffer, 0, bufferSize)
        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize)
            bytesAvailable = fileInputStream.available()
            bufferSize = min(bytesAvailable, maxBufferSize)
            bytesRead = fileInputStream.read(buffer, 0, bufferSize)
        }
        dataOutputStream.writeBytes(lineEnd)
    }

    class DataPart() {
        private var fileName: String? = null
        private var content: ByteArray? = null
        private var type: String? = null

        constructor(FileName: String, Content: ByteArray, Type: String) : this() {
            this.fileName = FileName
            this.content = Content
            this.type = Type
        }

        fun getFileName(): String? {
            return fileName
        }

        fun getContent(): ByteArray? {
            return content
        }

        fun getType(): String? {
            return type
        }
    }

}