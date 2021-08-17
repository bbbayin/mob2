package com.mob.sms2.utils

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object PhoneUtils {
    val TAG = "PhoneUtils"
    val callUri = CallLog.Calls.CONTENT_URI
    val columns = arrayOf(
            CallLog.Calls.CACHED_NAME// 通话记录的联系人
            , CallLog.Calls.NUMBER// 通话记录的电话号码
            , CallLog.Calls.DATE// 通话记录的日期
            , CallLog.Calls.DURATION// 通话时长
            , CallLog.Calls.TYPE
    )

    fun getCallLog(count: Int, context: Context): MutableList<CallLogBean> {
        val cursor: Cursor? = context.contentResolver.query(
                callUri, // 查询通话记录的URI
                columns, null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
        )
        val result: MutableList<CallLogBean> = mutableListOf()
        if (cursor != null) {
            var i = count
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            while (cursor.moveToNext() && i >= 1) {
                val name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));  //姓名
                val number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));  //号码
                val dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)); //获取通话日期
                val date = simpleDateFormat.format(Date(dateLong));
                val duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));//获取通话时长，值为多少秒
                val type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)); //获取通话类型：1.呼入2.呼出3.未接
                Log.i(TAG, "姓名：$name, 号码：$number, 日期：$date, 时长：$duration 秒, 类型: $type")
                result.add(CallLogBean(duration, number, type, date, name?:""))
                i--
            }
            cursor.close()
        }
        return result
    }

    fun getCallLog(context: Context) {
        val cursor: Cursor? = context.contentResolver.query(
                callUri, // 查询通话记录的URI
                columns, null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
        )
        cursor?.run {
            Log.i(TAG, "cursor count:" + cursor.count);
            while (cursor.moveToNext()) {
                val name = getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));  //姓名
                val number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));  //号码
                val dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)); //获取通话日期
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(dateLong));
//                val time = SimpleDateFormat("HH:mm").format(Date(dateLong));
                val duration =
                        cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));//获取通话时长，值为多少秒
                val type =
                        cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)); //获取通话类型：1.呼入2.呼出3.未接
//                val dayCurrent = SimpleDateFormat("dd").format(Date());
//                val dayRecord = SimpleDateFormat("dd").format(Date(dateLong));

                Log.i(
                        TAG, "姓名：$name, 号码：$number, 日期：$date, 时长：$duration 秒"
                )
            }
            cursor.close()
        }
    }
}