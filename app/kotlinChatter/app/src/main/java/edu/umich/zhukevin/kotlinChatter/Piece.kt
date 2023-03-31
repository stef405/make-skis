package edu.umich.zhukevin.kotlinChatter

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Piece(
    var piece_id: String? = null,
    piece_img: String? = null,
    solution_img: String? = null,
    var difficulty: String? = null
) {
    var piece_img: String? by PiecePropDelegate(piece_img)
    var solution_img: String? by PiecePropDelegate(solution_img)
}

class PiecePropDelegate private constructor ():
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