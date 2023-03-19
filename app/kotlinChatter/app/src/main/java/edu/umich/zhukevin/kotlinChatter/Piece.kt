package edu.umich.zhukevin.kotlinChatter

class Piece(
    imageUrl: String? = null,
    var id: Int? = null
) {
    var imageUrl: String? by ChattPropDelegate(imageUrl) //Probably have to change to some other function
}