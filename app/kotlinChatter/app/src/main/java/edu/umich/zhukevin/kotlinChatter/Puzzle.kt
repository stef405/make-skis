package edu.umich.zhukevin.kotlinChatter

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Puzzle(
    var user_id: String? = null,
    var puzzle_id: String? = null,
    var height: String? = null,
    var width: String? = null,
    imageUrl: String? = null
) {
    var imageUrl: String? by PuzzlePropDelegate(imageUrl) //Probably have to change to some other function
}

class PuzzlePropDelegate private constructor ():
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