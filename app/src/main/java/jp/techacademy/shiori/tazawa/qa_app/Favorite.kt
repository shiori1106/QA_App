package jp.techacademy.shiori.tazawa.qa_app

import java.io.Serializable

class Favorite(val title: String, val body: String, val name: String, val uid: String, val questionUid: String, val genre: Int, bytes: ByteArray):Serializable{
    // プロパティ
    val imageBytes: ByteArray

    // コンストラクタ
    init{
        imageBytes = bytes.clone()
    }
}