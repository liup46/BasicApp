package com.basic.net

import android.net.ParseException
import android.nfc.TagLostException
import android.os.ParcelFileDescriptor.FileDescriptorDetachedException
import android.util.Base64DataException
import com.basic.env.App
import com.basic.util.NetworkUtils
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import okhttp3.internal.http2.ConnectionShutdownException
import okhttp3.internal.http2.StreamResetException
import org.json.JSONException
import java.io.*
import java.net.*
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.ClosedChannelException
import java.nio.channels.FileLockInterruptionException
import java.util.*
import java.util.jar.JarException
import java.util.zip.ZipException
import javax.net.ssl.SSLException


class HttpException(val code: String, val msg: String) : RuntimeException("HTTP Error $code $msg") {
    companion object {
        //8000 业务请求成功
        const val SUCCESS = "8000"

        //follow cat error code
        const val ERROR_CODE_UNKNOW = "400000"

        const val ERROR_CODE_NO_NETWORK = "-401000"
        const val ERROR_CODE_SOCKET_NOT_CONNECT_EXCEPTION = "401001" //网络未链接 not connect
        const val ERROR_CODE_SOCKET_NOT_AVAILABLE_EXCEPTION = "401002" //网络不可用 not available
        const val ERROR_CODE_SOCKET_EXCEPTION = "401003" //SocketException
        const val ERROR_CODE_UNKNOWN_HOST_EXCEPTION = "401004" //UnknownHostException
        const val ERROR_CODE_TIMEOUT = "-401005"
        const val ERROR_CODE_UNKNOWN_SERVICE_EXCEPTION = "401006" //UnknownServiceException
        const val ERROR_CODE_CONNECTION_SHUTDOWN_EXCEPTION = "401005" //ConnectionShutdownException
        const val ERROR_CODE_PORT_UNREACHABLE_EXCEPTION = "401007" //PortUnreachableException
        const val ERROR_CODE_BIND_EXCEPTION = "401008" //BindException
        const val ERROR_CODE_NO_ROUTE_TO_HOST_EXCEPTION = "401009" //NoRouteToHostException
        const val ERROR_CODE_CONNECT_EXCEPTION = "-401010"
        const val ERROR_CODE_RETRY = "-401011"
        const val ERROR_CODE_SSL = "-401012"
        const val ERROR_CODE_PROTOCOL = "-401013"
        const val ERROR_CODE_MALFORMEDURL_EXCEPTION = "-401014"

        const val ERROR_CODE_PARSE = "-402001"
        const val ERROR_CODE_EOF_EXCEPTION = "-402002"
        const val ERROR_CODE_ASYNCHRONOUSCLOSE_EXCEPTION = "-402003"
        const val ERROR_CODE_BASE64DATA_EXCEPTION = "-402004"
        const val ERROR_CODE_CHARCONVERSION_EXCEPTION = "-402005"
        const val ERROR_CODE_CLOSEDCHANNEL_EXCEPTION = "-402006"
        const val ERROR_CODE_CHARACTERCODING_EXCEPTION = "-402007"
        const val ERROR_CODE_PARCELFILEDESCRIPTOR_EXCEPTION = "-402008"
        const val ERROR_CODE_FILELOCKINTERRUPTION_EXCEPTION = "-402009"
        const val ERROR_CODE_FILENOTFOUND_EXCEPTION = "-402010"
        const val ERROR_CODE_INVALIDCLASS_EXCEPTION = "-402011"
        const val ERROR_CODE_INVALIDOBJECT_EXCEPTION = "-402012"
        const val ERROR_CODE_MALFORMEDJSON_EXCEPTION = "-402013"
        const val ERROR_CODE_NOTACTIVE_EXCEPTION = "-402014"
        const val ERROR_CODE_NOTSERIALIZABLE_EXCEPTION = "-402015"
        const val ERROR_CODE_OBJECTSTREAM_EXCEPTION = "-402016"
        const val ERROR_CODE_STREAMRESET_EXCEPTION = "-402017"
        const val ERROR_CODE_SYNCFAILED_EXCEPTION = "-402018"
        const val ERROR_CODE_TAGLOST_EXCEPTION = "-402019"
        const val ERROR_CODE_UTFDATAFORMAT_EXCEPTION = "-402020"
        const val ERROR_CODE_UNSUPPORTEDENCODING_EXCEPTION = "-402021"
        const val ERROR_CODE_ZIP_EXCEPTION = "-402022"
        const val ERROR_CODE_INTERRUPTEDIO_EXCEPTION = "-402023"
        const val ERROR_CODE_JAREXCEPTION = "-402024"
        const val ERROR_CODE_INVALIDPROPERTIESFORMAT_EXCEPTION = "-402025"
        const val ERROR_CODE_EOF_V2_EXCEPTION = "-402026"

        const val ERROR_CODE_REQUEST_CANCEL = "403001" // 请求取消

        /**
         * 通用错误码，遇到此错误码即弹Toast
         */
        const val ERROR_CODE_9031 = "9031"

        const val ERROR_MSG_PARSE = "数据解析出错啦，请稍后再试"
        const val ERROR_MSG_SERVER_CONNECT_FAILED = "服务器连接失败"
        const val ERROR_MSG_TIMEOUT = "网络连接超时,请重试"
        const val ERROR_MSG_NO_NETWORK = "网络已断开，请检查网络连接"
        const val ERROR_MSG_UNKNOWN = "请求异常，请稍后再试"
        const val ERROR_MSG_CANCLED = "取消被取消"

        fun catchException(e: Throwable): HttpException {
            if(e is HttpException){
                return e
            }
            val httpException =
                if (e is JsonParseException || e is JSONException || e is ParseException) { //解析异常
                    HttpException(ERROR_CODE_PARSE, ERROR_MSG_PARSE)
                } else if (e is SocketTimeoutException) {
                    if (!NetworkUtils.netIsConnected(App.getContext())) {
                        HttpException(
                            ERROR_CODE_SOCKET_NOT_CONNECT_EXCEPTION,
                            ERROR_MSG_SERVER_CONNECT_FAILED
                        )
                    } else if (!NetworkUtils.netIsAvailable(App.getContext())) {
                        HttpException(
                            ERROR_CODE_SOCKET_NOT_AVAILABLE_EXCEPTION,
                            ERROR_MSG_SERVER_CONNECT_FAILED
                        )
                    } else {
                        HttpException(ERROR_CODE_TIMEOUT, ERROR_MSG_TIMEOUT)
                    }
                } else if (e is UnknownHostException || e is SocketException || e is ConnectionShutdownException || e is UnknownServiceException) {
                    if (!NetworkUtils.isNetworkConnected(App.getContext())) {
                        HttpException(ERROR_CODE_NO_NETWORK, ERROR_MSG_NO_NETWORK)
                    } else {
                        if (e is UnknownHostException) {
                            HttpException(
                                ERROR_CODE_UNKNOWN_HOST_EXCEPTION,
                                ERROR_MSG_SERVER_CONNECT_FAILED
                            )
                        } else if (e is ConnectionShutdownException) {
                            HttpException(
                                ERROR_CODE_CONNECTION_SHUTDOWN_EXCEPTION,
                                ERROR_MSG_SERVER_CONNECT_FAILED
                            )
                        } else if (e is UnknownServiceException) {
                            HttpException(
                                ERROR_CODE_UNKNOWN_SERVICE_EXCEPTION,
                                ERROR_MSG_SERVER_CONNECT_FAILED
                            )
                        } else if (e is ConnectException) {
                            HttpException(
                                ERROR_CODE_CONNECT_EXCEPTION,
                                ERROR_MSG_SERVER_CONNECT_FAILED
                            )
                        } else if (e is NoRouteToHostException) {
                            HttpException(
                                ERROR_CODE_NO_ROUTE_TO_HOST_EXCEPTION,
                                ERROR_MSG_SERVER_CONNECT_FAILED
                            )
                        } else if (e is PortUnreachableException) {
                            HttpException(
                                ERROR_CODE_PORT_UNREACHABLE_EXCEPTION,
                                ERROR_MSG_SERVER_CONNECT_FAILED
                            )
                        } else if (e is BindException) {
                            HttpException(
                                ERROR_CODE_BIND_EXCEPTION,
                                ERROR_MSG_SERVER_CONNECT_FAILED
                            )
                        } else {
                            HttpException(
                                ERROR_CODE_SOCKET_EXCEPTION,
                                ERROR_MSG_SERVER_CONNECT_FAILED
                            )
                        }
                    }
                } else if (e is HttpRetryException) {
                    HttpException(ERROR_CODE_RETRY, ERROR_MSG_SERVER_CONNECT_FAILED)
                } else if (e is SSLException) {
                    HttpException(ERROR_CODE_SSL, ERROR_MSG_SERVER_CONNECT_FAILED)
                } else if (e is ProtocolException) {
                    HttpException(
                        ERROR_CODE_PROTOCOL,
                        ERROR_MSG_SERVER_CONNECT_FAILED
                    )
                } else if (e is AsynchronousCloseException) {
                    HttpException(
                        ERROR_CODE_ASYNCHRONOUSCLOSE_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is Base64DataException) {
                    HttpException(
                        ERROR_CODE_BASE64DATA_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is CharConversionException) {
                    HttpException(
                        ERROR_CODE_CHARCONVERSION_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is CharacterCodingException) {
                    HttpException(
                        ERROR_CODE_CHARACTERCODING_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is ClosedChannelException) {
                    HttpException(
                        ERROR_CODE_CLOSEDCHANNEL_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is EOFException) {
                    HttpException(ERROR_CODE_EOF_EXCEPTION, ERROR_MSG_UNKNOWN)
                } else if (e is FileDescriptorDetachedException) {
                    HttpException(
                        ERROR_CODE_PARCELFILEDESCRIPTOR_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is FileLockInterruptionException) {
                    HttpException(
                        ERROR_CODE_FILELOCKINTERRUPTION_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is FileNotFoundException) {
                    HttpException(
                        ERROR_CODE_FILENOTFOUND_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is InterruptedIOException) {
                    HttpException(
                        ERROR_CODE_INTERRUPTEDIO_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is InvalidClassException) {
                    HttpException(
                        ERROR_CODE_INVALIDCLASS_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is InvalidObjectException) {
                    HttpException(
                        ERROR_CODE_INVALIDOBJECT_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is InvalidPropertiesFormatException) {
                    HttpException(
                        ERROR_CODE_INVALIDPROPERTIESFORMAT_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is JarException) {
                    HttpException(ERROR_CODE_JAREXCEPTION, ERROR_MSG_UNKNOWN)
                } else if (e is MalformedJsonException) {
                    HttpException(ERROR_CODE_MALFORMEDJSON_EXCEPTION, ERROR_MSG_UNKNOWN)
                } else if (e is MalformedURLException) {
                    HttpException(ERROR_CODE_MALFORMEDURL_EXCEPTION, ERROR_MSG_UNKNOWN)
                } else if (e is NotActiveException) {
                    HttpException(ERROR_CODE_NOTACTIVE_EXCEPTION, ERROR_MSG_UNKNOWN)
                } else if (e is NotSerializableException) {
                    HttpException(
                        ERROR_CODE_NOTSERIALIZABLE_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is ObjectStreamException) {
                    HttpException(
                        ERROR_CODE_OBJECTSTREAM_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is StreamResetException) {
                    HttpException(
                        ERROR_CODE_STREAMRESET_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is SyncFailedException) {
                    HttpException(
                        ERROR_CODE_SYNCFAILED_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is TagLostException) {
                    HttpException(ERROR_CODE_TAGLOST_EXCEPTION, ERROR_MSG_UNKNOWN)
                } else if (e is UTFDataFormatException) {
                    HttpException(
                        ERROR_CODE_UTFDATAFORMAT_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is UnsupportedEncodingException) {
                    HttpException(
                        ERROR_CODE_UNSUPPORTEDENCODING_EXCEPTION,
                        ERROR_MSG_UNKNOWN
                    )
                } else if (e is ZipException) {
                    HttpException(ERROR_CODE_ZIP_EXCEPTION, ERROR_MSG_UNKNOWN)
                } else {
                    if (e.message != null && e.message == "Canceled") {
                        HttpException(ERROR_CODE_REQUEST_CANCEL, ERROR_MSG_CANCLED)
                    } else if (e.message != null && e.message == "unexpected end of stream on Connection") {
                        HttpException(
                            ERROR_CODE_EOF_V2_EXCEPTION,
                            ERROR_MSG_UNKNOWN
                        )
                    } else {
                        HttpException(ERROR_CODE_UNKNOW, ERROR_MSG_UNKNOWN)
                    }
                }
            httpException.initCause(e)
            return httpException
        }
    }


}

