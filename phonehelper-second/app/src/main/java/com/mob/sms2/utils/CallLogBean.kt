package com.mob.sms2.utils

/**
 * 时长，电话
 */
data class CallLogBean(val duration: Int, val number: String, val type: Int, val date: String, val name:String) {
    override fun equals(other: Any?): Boolean {
        if (other is CallLogBean) {
            return other.duration == duration && other.number == number && other.type == type && other.date == date
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = duration
        result = 31 * result + number.hashCode()
        result = 31 * result + type
        result = 31 * result + date.hashCode()
        return result
    }

}