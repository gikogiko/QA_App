package jp.techacademy.h.e.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.collection.LLRBNode

class QuestionDetailListAdapter(context: Context, private val mQustion: Question) : BaseAdapter() {
    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater? = null
    private var alreadyFavoriteFlg = false
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDataBaseReference: DatabaseReference


    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + mQustion.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return mQustion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
            }
            val body = mQustion.body
            val name = mQustion.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name

            val favoriteBtnView = convertView.findViewById<View>(R.id.favorite_button) as Button
            favoriteBtnView.visibility = View.INVISIBLE

            // FirebaseAuthのオブジェクトを取得する
            mAuth = FirebaseAuth.getInstance()
            val user = mAuth.currentUser
            mDataBaseReference = FirebaseDatabase.getInstance().reference
            val userRef = mDataBaseReference.child(FavoritePATH).child(user!!.uid)

            if (user != null) {
                favoriteBtnView.visibility = View.VISIBLE
            }

            //お気に入り追加/解除
            excuteFavorite(favoriteBtnView, user,mQustion)

            val bytes = mQustion.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }
        } else {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val answer = mQustion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name
        }

        return convertView
    }

    //お気に入り追加/解除
    private fun excuteFavorite(favoriteBtnView: Button, user: FirebaseUser,question:Question) {
        favoriteBtnView.setOnClickListener() {
            if (alreadyFavoriteFlg) {
                //お気に入り→非お気に入り
                favoriteBtnView.text = "お気に入り追加"
                favoriteBtnView.setBackgroundColor(Color.LTGRAY)

                //FireBase上のお気に入り情報を削除
                deleteFavorite(user,question)

                alreadyFavoriteFlg = false
            } else {
                //非お気に入り→お気に入り
                favoriteBtnView.text = "お気に入り解除"
                favoriteBtnView.setBackgroundColor(Color.GREEN)

                //FireBaseにお気に入り情報を追加
                addFavorite(user,question)

                alreadyFavoriteFlg = true
            }
        }
    }

    private fun addFavorite(user: FirebaseUser,question:Question) {
        //FireBaseにお気に入り情報を追加
        val uid = user.uid
        val questionUid = mDataBaseReference.child(ContentsPATH).child(user.uid).toString()
        val userRef = mDataBaseReference.child(UsersPATH).child(user.uid)
        val data = HashMap<String, String>()

        data["uid"] = uid
        data["questionUid"] = questionUid
        userRef.setValue(data)
    }

    private fun deleteFavorite(user: FirebaseUser,question:Question) {
        //FireBase上のお気に入り情報を削除
        mDataBaseReference.child(UsersPATH).child(user.uid).child(question.questionUid).removeValue()
    }

}