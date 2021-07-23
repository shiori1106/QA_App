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
import kotlinx.android.synthetic.main.list_favorite.view.*

class FavoriteAdapter(context: Context): BaseAdapter() {

    private var mLayoutInflater: LayoutInflater
    private var mFavoriteArrayList = ArrayList<Favorite>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    // データの数を返す
    override fun getCount(): Int {
        return mFavoriteArrayList.size
    }

    // データを返す
    override fun getItem(position: Int): Any {
        return mFavoriteArrayList[position]
    }

    // データのIDを返す
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // Viewを返す
    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        var convertView = view

        if (convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.list_favorite, parent, false)
        }

        val titleText = convertView!!.titleTextView as TextView
        titleText.text = mFavoriteArrayList[position].title

        val nameText = convertView.nameTextView as TextView
        nameText.text = mFavoriteArrayList[position].name

        val bytes = mFavoriteArrayList[position].imageBytes
        if (bytes.isNotEmpty()){
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val imageView = convertView.imageView as ImageView
            imageView.setImageBitmap(image)
        }

        val genreText = convertView.genreTextView as TextView

        var genreName = "ジャンル"
        when (mFavoriteArrayList[position].genre){
            1 -> {
                genreName = "趣味"
            }
            2 -> {
                genreName = "生活"
            }
            3 -> {
                genreName = "健康"
            }
            4 -> {
                genreName = "コンピュータ"
            }
        }
        genreText.text = "< ジャンル : " + genreName + " >"

        return convertView

    }

    fun setFavoriteArrayList(FavoriteArrayList: ArrayList<Favorite>){
        mFavoriteArrayList = FavoriteArrayList
    }
}