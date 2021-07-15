package jp.techacademy.shiori.tazawa.qa_app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_answer_send.*

class AnswerSendActivity : AppCompatActivity() , View.OnClickListener, DatabaseReference.CompletionListener{

    // プロパティ
    // Intentで渡ってきたQuestionを保持する変数を定義
    private lateinit var mQuestion: Question

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer_send)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        // UIの準備
        sendButton.setOnClickListener(this)
    }

    // Firebaseへの書き込み完了を受け取る
    override fun onComplete(databaseError: DatabaseError?, ref: DatabaseReference) {
        progressBar.visibility = View.GONE

        // 成功したらfinishメソッドでActivityを閉じる
        if (databaseError == null) {
            finish()
            // 失敗した場合はSnackbarでその旨を表示
        } else {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.send_answer_failure), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onClick(v: View) {
        // キーボードが出てたら閉じる
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        val databaseReference = FirebaseDatabase.getInstance().reference
        val answerRef = databaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(
            AnswersPATH)

        val data = HashMap<String, String>()

        // UID
        data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

        // 表示名
        // Preferenceから名前を取る
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")
        data["name"] = name!!

        // 回答を取得する
        val answer = answerEditText.text.toString()

        if (answer.isEmpty()){
            // 回答が入力されていない時はエラーを表示するだけ
            Snackbar.make(v, getString(R.string.answer_error_message), Snackbar.LENGTH_LONG).show()
            return
        }
        data["body"] = answer

        progressBar.visibility = View.VISIBLE
        answerRef.push().setValue(data, this)
    }


}