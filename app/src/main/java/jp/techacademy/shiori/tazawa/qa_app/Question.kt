package jp.techacademy.shiori.tazawa.qa_app

import java.io.Serializable

// Intentでデータを渡せるようにSerializableクラスを実装
class Question(val title: String, val body: String, val name: String, val uid: String, val questionUid: String, val genre: Int, bytes: ByteArray, val answers: ArrayList<Answer>):Serializable{
    // プロパティ
    val imageBytes: ByteArray

    // コンストラクタ
    init{
        imageBytes = bytes.clone()
    }
}