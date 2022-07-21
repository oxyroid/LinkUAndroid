package com.linku.domain

import android.util.Log
import androidx.annotation.Keep
import com.linku.domain.extension.debug
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

@Serializable
@Keep
data class Result<out T>(
    private val data: T? = null,
    private val code: String = "?",
    @SerialName("msg")
    private val message: String? = null
) {
    private val isSuccess get() = code.trim() == "00000"

    fun peek() = data!!
    fun peekOrNull() = data

    suspend fun handle(block: suspend (T) -> Unit): Result<T> {
        if (isSuccess && data != null) block.invoke(data)
        return this
    }

    suspend fun handleUnit(block: suspend (Unit) -> Unit): Result<T> {
        if (isSuccess) block.invoke(Unit)
        return this
    }

    suspend fun catch(block: suspend (String, String) -> Unit): Result<T> {
        if (!isSuccess) block.invoke(message ?: "Unknown Error", code)
        return this
    }

    fun <R> map(converter: (T) -> R): Result<R> {
        if (data == null) return Result(
            code = code,
            message = message
        )
        return Result(
            data = converter(data),
            code = code,
            message = message
        )
    }

    companion object {
        val codes = mapOf(
            "00000" to "成功",
            "A0001" to "用户未登录",
            "A0002" to "账户或密码错误",
            "A0003" to "账号异常/账号不存在",
            "A0006" to "账户已存在",
            "A0007" to "注册异常",
            "A0008" to "账户已验证",
            "A0009" to "用户身份凭据异常",
            "A0010" to "请求的用户不存在",
            "A0011" to "不能添加自己为好友",
            "A0012" to "该好友关系已存在",
            "A0013" to "账户未验证",
            "B0001" to "JSON（反）序列化异常",
            "C0003" to "MQTT服务端状态异常",
            "C0004" to "MQTT服务异常",
            "D0001" to "未知错误",
            "D0002" to "请求参数异常",
            "D0003" to "参数非数字/格式化为数字异常",
            "D0004" to "资源不存在/身份验证失败",
            "D0005" to "缺少必要参数",
            "D0006" to "请求方法有误",
            "D0007" to "参数类型有误",
            "D0008" to "信息修改失败",
            "E0001" to "文件上传异常",
            "E0003" to "文件操作异常",
            "E0004" to "上传文件超过大小上限",
            "F0001" to "WebSocket服务异常",
            "G0001" to "邮件发送服务异常",
            "G0002" to "邮件发送间隔过短",
            "G0003" to "邮件验证请求失败",
            "H0001" to "SQL语句执行失败",
            "I0001" to "缺少用户Token",
            "J0001" to "会话不存在",
            "J0002" to "会话创建失败",
            "J0006" to "用户添加至会话失败",
            "J0007" to "用户移出会话失败",
            "J0003" to "无管理员权限",
            "J0004" to "删除会话失败",
            "J0005" to "目标用户已在此会话",
            "J0005" to "目标用户不在此会话",
            "J0006" to "管理员权限修改失败",
            "J0007" to "好友会话无法邀请加入",
            "K0001" to "消息发送失败",
            "K0002" to "已读状态设置失败",
            "K0003" to "无未读消息"
        )
    }
}

inline fun <T> sandbox(block: () -> Result<T>): Result<T> {
    return try {
        block.invoke()
    } catch (e: SerializationException) {
        debug { Log.e("SerializationException", "", e) }
        Result(
            code = "反序列化错误",
            message = "请将应用升级至最新版本再试！"
        )
    } catch (e: Exception) {
        debug { Log.e("Result Wrapper", "sandbox:", e) }
        Result(
            code = "?",
            message = e.message ?: "sandbox exception"
        )
    }
}