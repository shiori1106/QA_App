package jp.techacademy.shiori.tazawa.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_question_detail.view.*

class QuestionDetailListAdapter(context: Context, private val mQuestion: Question): BaseAdapter(){

    // どのレイアウトを使って表示させるかの判断するためのタイプを表す定数
    companion object{
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater? = null

    init{
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    // データの数を返す
    override fun getCount(): Int {
        return 1 + mQuestion.answers.size
    }

    // 渡ってきたポジションがどのタイプかを返す
    override fun getItemViewType(position: Int): Int {
        // 1行目＝ポジションが0の時に質問であるTYPE_QUESTIONを返し、
        // それ以外は回答なのでTYPE_ANSWERを返す
        return if (position == 0){
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    // データを返す
    override fun getItem(position: Int): Any {
        return mQuestion
    }

    // データのIDを返す
    override fun getItemId(position: Int): Long {
        return 0
    }

    // Viewを返す
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view

        // 1番上の質問について
        if (getItemViewType(position) == TYPE_QUESTION){
            if (convertView == null){
                convertView = mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
            }

            val body = mQuestion.body
            val name = mQuestion.name

            val bodyTextView = convertView.bodyTextView as TextView
            bodyTextView.text = body

            val nameTextView = convertView.nameTextView as TextView
            nameTextView.text = name

            val bytes = mQuestion.imageBytes
            if(bytes.isNotEmpty()){
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }
        } else {
            // 2番目以降の回答について
            if (convertView == null){
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val answer = mQuestion.answers[position -1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.bodyTextView as TextView
            bodyTextView.text = body

            val nameTextView = convertView.nameTextView as TextView
            nameTextView.text = name
        }

        return convertView
    }

}