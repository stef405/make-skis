package edu.umich.zhukevin.kotlinChatter

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Chatt(var username: String? = null,
            var message: String? = null,
            var timestamp: String? = null,
            imageUrl: String? = null,
            videoUrl: String? = null) {
    var imageUrl: String? by ChattPropDelegate(imageUrl)
    var videoUrl: String? by ChattPropDelegate(videoUrl)
}

class ChattPropDelegate private constructor ():
    ReadWriteProperty<Any?, String?> {
    private var _value: String? = null
        set(newValue) {
            newValue ?: run {
                field = null
                return
            }
            field = if (newValue == "null" || newValue.isEmpty()) null else newValue
        }

    constructor(initialValue: String?): this() { _value = initialValue }

    override fun getValue(thisRef: Any?, property: KProperty<*>) = _value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        _value = value
    }
}