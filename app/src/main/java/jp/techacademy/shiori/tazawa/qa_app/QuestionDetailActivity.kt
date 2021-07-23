package jp.techacademy.shiori.tazawa.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.content_main.listView

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference

    // 追加
    private lateinit var mFavorite: ArrayList<Favorite>
    private lateinit var favoriteQuestionRef: DatabaseReference

    private var isFavorite = false
    val user = FirebaseAuth.getInstance().currentUser


    private val mEventListener = object: ChildEventListener{

        // アイテムのリストを取得
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            // 質問詳細画面用
            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUiD) {
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

        // 渡ってきたお気に入り情報を保持する
        mFavorite = extras.get("favorite") as ArrayList<Favorite>

        // お気に入りに登録されている場合は、isFavoriteをtrueにして、ハートの色を変える
        for (favorite in mFavorite){
            if(favorite.uid == mQuestion.questionUid){ //なぜかuidにimageが入ってしまっている
                isFavorite = true

                favoteImageView.apply {
                    setImageResource(R.drawable.ic_baseline_favorite_border_24_pink)
                }
            }
        }


        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        // お気に入りリストの準備

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
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


        // 質問詳細画面用
        val databaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = databaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(
            AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)


        // ハートボタンが押されたとき、ログイン情報を確認する

        favoteImageView.setOnClickListener { v ->

            if (user == null){
                // ログインしていない場合はログイン画面に遷移させる

                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)

            } else {

                // お気に入りに登録されているときは、お気に入りから削除してハートの色を変える
                favoriteQuestionRef = databaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid)

                if (isFavorite) {

                // お気に入りに登録されている場合

                    isFavorite = false

                    // メッセージを表示
                    Snackbar.make(v, getString(R.string.favorite_delete), Snackbar.LENGTH_LONG).show()

                    // firebase上のお気に入りから削除
                    favoriteQuestionRef.removeValue()

                    // 表示されているハートの色を変える
                    favoteImageView.apply{
                        setImageResource(R.drawable.ic_baseline_favorite_border_24)
                    }


                } else {

                // お気に入りに登録されていない場合

                    isFavorite = true

                    // メッセージを表示
                    Snackbar.make(v, getString(R.string.favorite_add), Snackbar.LENGTH_LONG).show()

                    // firebase上のお気に入りに登録
                    val data = HashMap<String, String>()

                    data["body"] = mQuestion.body
                    val imageBytes = mQuestion.imageBytes
                    if (imageBytes.isNotEmpty()){
                        data["image"] = Base64.encodeToString(imageBytes,Base64.DEFAULT)
                    }
                    data["name"] = mQuestion.name
                    data["title"] = mQuestion.title
                    data["uid"] = mQuestion.questionUid
                    data["genre"] = mQuestion.genre.toString()

                    favoriteQuestionRef.setValue(data)

                    // 表示されているハートの色を変える
                    favoteImageView.apply{
                        setImageResource(R.drawable.ic_baseline_favorite_border_24_pink)
                    }
                }
            }
        }
    }
}