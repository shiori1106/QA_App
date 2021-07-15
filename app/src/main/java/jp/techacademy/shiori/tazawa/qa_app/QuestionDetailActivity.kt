package jp.techacademy.shiori.tazawa.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.listView

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference

    private val mEventListener = object: ChildEventListener{

        // アイテムのリストを取得
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>
            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers){
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUiD){
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        // リスト内のアイテムに対する変更がないかリッスンする
        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {

        }

        // リストから削除されるアイテムがないかリッスンする
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        // 並べ替えリストの項目順変更をリッスンする
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        //
        override fun onCancelled(databaseError: DatabaseError) {

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val databaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = databaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(
            AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }
}