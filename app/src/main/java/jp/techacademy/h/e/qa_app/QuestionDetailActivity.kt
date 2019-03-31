package jp.techacademy.h.e.qa_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ListView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.internal.FirebaseAppHelper.getUid
import jp.techacademy.h.e.qa_app.R.drawable.btn
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.list_question_detail.*

import java.util.HashMap
import com.google.firebase.database.ValueEventListener



class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mUserRef: DatabaseReference
    private var mFavoriteFlag = false

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        //ログインユーザの情報を取得
        val user = FirebaseAuth.getInstance().currentUser

        //ログイン時のみ「お気に入り追加ボタンを表示」
        if (!isLogin(user)) {
            favorite_button.visibility = View.INVISIBLE
        }else{
            //お気に入りデータ確認
            val dataBaseReference = FirebaseDatabase.getInstance().reference
            dataBaseReference.child(UsersPATH).child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        //val questionUid = dataSnapshot.child(UsersPATH).child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid)

                        val questionUid =  dataSnapshot.getValue()
                        if(questionUid != null){
                            //ボタンの表示を「お気に入り追加済み」の状態に変更
                            favorite_button.text = "お気に入り解除"
                            favorite_button.setBackgroundColor(Color.GREEN)

                            mFavoriteFlag = true
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

        }

        favorite_button.setOnClickListener() {
            // ログイン済みのユーザーを取得する
            val dataBaseReference = FirebaseDatabase.getInstance().reference
            mUserRef = dataBaseReference.child(UsersPATH).child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid)

            if (mFavoriteFlag) {
                //お気に入りを解除
                favorite_button.text = "お気に入り追加"
                favorite_button.setBackgroundColor(Color.LTGRAY)

                //FireBase上の対象の質問を削除
                dataBaseReference.child(UsersPATH).child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid).removeValue()

                //mFavoriteFlagをfalseへ
                mFavoriteFlag = false

            } else {
                //お気に入りに追加
                favorite_button.text = "お気に入り解除"
                favorite_button.setBackgroundColor(Color.GREEN)

                //FireBase上へ質問を登録
                val genre = mQuestion.genre.toString()

                val data = HashMap<String, String>()
                data["genre"] = genre
                mUserRef.setValue(data)


                //mFavoriteFlagをtrueへ
                mFavoriteFlag = true
            }
        }

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する


            if (isLogin(user)) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }
        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef =
            dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid)
                .child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }

    private fun isLogin(user: FirebaseUser?): Boolean {
        if (user != null) {
            return true
        }
        return false
    }
}