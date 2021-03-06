package jp.techacademy.h.e.qa_app

const val UsersPATH = "users"       // Firebaseにユーザの表示名を保存するパス
const val ContentsPATH = "contents" // Firebaseに質問を保存するバス
const val AnswersPATH = "answers"   // Firebaseに解答を保存するパス
const val FavoritePATH = "favorite"   // Firebaseにお気に入りを保存するパス
const val NameKEY = "name"          // Preferenceに表示名を保存する時のキー
var mFavoriteQuestionUidList = arrayListOf<String>()   //お気に入り登録した質問の質問idを格納するリスト