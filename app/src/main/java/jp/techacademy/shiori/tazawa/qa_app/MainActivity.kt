package jp.techacademy.shiori.tazawa.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{

    private var mGenre = 0

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter
    private lateinit var mFavoriteAdapter: FavoriteAdapter
    private lateinit var mFavoriteArrayList: ArrayList<Favorite>

    private var mGenreRef: DatabaseReference? = null
    private var userRef: DatabaseReference? = null

    // ログイン済みのユーザーを取得する
    val user = FirebaseAuth.getInstance().currentUser


    // QuestionListAdapterにデータを設定するため、Firebaseからデータを取得
    // ChildEventListenerではデータに追加・変化があったときに受け取る
    private val mEventListener = object: ChildEventListener{

        // 質問が追加された時に呼ばれる
        // QuestionクラスとAnswerを作成し、ArrayListに追加
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes =
                    if (imageString.isNotEmpty()){
                        Base64.decode(imageString, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }



            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String, String>?
            if (answerMap != null){
                for (key in answerMap.keys){
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)

                }
            }

            val question = Question(title, body, name, uid, dataSnapshot.key ?: "", mGenre, bytes, answerArrayList)
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }

        // 質問に対して回答が投稿されたときに呼ばれる
        // 変化があった質問に対するQuestionクラスのインスタンスで保持している、
        // 回答のArrayListをいったんクリアにし、取得した回答を設定
        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            for (question in mQuestionArrayList){
                if (dataSnapshot.key.equals(question.questionUid)){
                    // このアプリで変更がある可能性があるのは回答（Answer）のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null){
                        for(key in answerMap.keys){
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {
            TODO("Not yet implemented")
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            TODO("Not yet implemented")
        }

        override fun onCancelled(p0: DatabaseError) {
            TODO("Not yet implemented")
        }
    }

    // お気に入りリスト取得用
    private val favoriteEventListener = object: ChildEventListener{
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            val questionUid = dataSnapshot.key ?: ""
            val map = dataSnapshot.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val image = map["image"] ?: ""
            val uid = map["uid"] ?: "" //questionUid
            val genre = map["genre"] ?: ""


            val favorite = Favorite(title, body, name, uid, image, genre)
            mFavoriteArrayList.add(favorite)
            mFavoriteAdapter.notifyDataSetChanged()

            }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            TODO("Not yet implemented")
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.d("kotlintest","削除されました")
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            TODO("Not yet implemented")
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // idがtoolbarがインポート宣言により取得されているので
        // id名でActionBarのサポートを依頼
        setSupportActionBar(toolbar)

        // fabにClickリスナーを登録
        fab.setOnClickListener { view ->

            // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
            if (mGenre == 0){
                Snackbar.make(view, getString(R.string.question_no_select_genre), Snackbar.LENGTH_LONG).show()
            } else {

            }

            /*
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser
            */


            // ログインしていなければログイン画面に遷移させる
            if (user == null){
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)

            } else {

                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)

            }
        }

        // ナビゲーションドロワーの設定
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        mFavoriteAdapter = FavoriteAdapter(this)
        mFavoriteArrayList = ArrayList<Favorite>()
        mFavoriteAdapter.notifyDataSetChanged()

        listView.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する + お気に入り情報も渡す
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            intent.putExtra("favorite", mFavoriteArrayList)
            startActivity(intent)

        }

        // お気に入りリストを作成する
        mFavoriteAdapter.setFavoriteArrayList(mFavoriteArrayList)
        listView.adapter = mFavoriteAdapter

        userRef = mDatabaseReference.child(FavoritePATH).child(user!!.uid)
        userRef!!.addChildEventListener(favoriteEventListener)


    }

    override fun onResume(){
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        // 1:趣味を既定の選択とする
        if(mGenre == 0){
            onNavigationItemSelected(navigationView.menu.getItem(0))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings){
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true

        }

        return super.onOptionsItemSelected(item)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.nav_hobby) {
            // ツールバーに表示させるタイトルを設定
            toolbar.title = getString(R.string.menu_hobby_label)
            mGenre = 1

        } else if (id == R.id.nav_life) {
            toolbar.title = getString(R.string.menu_life_label)
            mGenre = 2

        } else if (id == R.id.nav_health) {
            toolbar.title = getString(R.string.menu_health_label)
            mGenre = 3

        } else if (id == R.id.nav_computer) {
            toolbar.title = getString(R.string.menu_computer_label)
            mGenre = 4

        } else if (id == R.id.nav_favorite){
            toolbar.title = getString(R.string.menu_favorite_label)
            mGenre = 5
        }

        drawer_layout.closeDrawer(GravityCompat.START)


        // お気に入りのときは、お気に入り一覧をクリアしてから再度Adapterにセットし、
        // AdapterをListViewにセットし直す
        if (mGenre == 5){


            // 後から戻ってきたときのために、再度お気に入りリストを取得するようにする
            mFavoriteArrayList.clear()
            mFavoriteAdapter.setFavoriteArrayList(mFavoriteArrayList)
            listView.adapter = mFavoriteAdapter

            userRef = mDatabaseReference.child(FavoritePATH).child(user!!.uid)
            userRef!!.addChildEventListener(favoriteEventListener)


            return true


        } else {

            // お気に入り以外のときは、
            // 質問のリストをクリアしてから再度Adapterにセットし、
            // AdapterをListViewにセットし直す
            mQuestionArrayList.clear()
            mAdapter.setQuestionArrayList(mQuestionArrayList)
            listView.adapter = mAdapter

            // 選択したジャンルにリスナーを登録する
            if (mGenreRef != null) {
                mGenreRef!!.removeEventListener(mEventListener)
            }

            mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
            mGenreRef!!.addChildEventListener(mEventListener)

            return true

        }
    }
}