package jp.ed.nnn.nightcoreplayer

import javafx.scene.media.Media

// ─────────────────────────────────────────────
// Movie : TableView に表示する 1レコード（1動画）を表すクラス
//
// 重要ポイント：
// - Main.scala では movie.setId / movie.getId のような JavaBeans 形式を呼んでいる
// - JavaFX の PropertyValueFactory("fileName") も内部で getFileName() を探す（JavaBeans想定）
// - Scala3 環境では @BeanProperty による自動生成が環境差で効かない場合があるため、
//   ここでは確実に動くように getXxx/setXxx を手書きしている（詰まり解消優先の最小修正）
// ─────────────────────────────────────────────
class Movie {

  // ── 内部フィールド（実体） ──
  // private にして、外からは get/set 経由で触らせる（JavaBeansスタイル）
  private var _id: Long = 0L
  private var _fileName: String = ""
  private var _time: String = ""
  private var _filePath: String = ""
  private var _media: Media = null

  // ── JavaBeans 形式のアクセサ（Main.scala / PropertyValueFactory 向け） ──
  // TableView の列設定で PropertyValueFactory("fileName") とすると、
  // JavaFX は getFileName() を探して値を取得する（＝ここが無いと表示できないことがある）
  def getId: Long = _id
  def setId(v: Long): Unit = _id = v

  def getFileName: String = _fileName
  def setFileName(v: String): Unit = _fileName = v

  def getTime: String = _time
  def setTime(v: String): Unit = _time = v

  def getFilePath: String = _filePath
  def setFilePath(v: String): Unit = _filePath = v

  def getMedia: Media = _media
  def setMedia(v: Media): Unit = _media = v

  // ── Scala っぽい参照も一応残す ──
  // Main.scala 側で movie.media を直接参照しているため、それに合わせて用意する
  // （JavaBeansだけに寄せるなら Main.scala を getMedia に直す手もあるが、逸脱を避ける）
  def media: Media = _media

  // ── equals/hashCode は id ベース（containsチェック等に使う） ──
  // 今回は Movie の同一性を「idが同じかどうか」で判断したいので、idのみで比較する
  // Main.scala では id 重複を避けるために while(movies.contains(movie)) ... をしている
  def canEqual(other: Any): Boolean = other.isInstanceOf[Movie]

  override def equals(other: Any): Boolean = other match {
    case that: Movie =>
      (that canEqual this) &&
        this.getId == that.getId
    case _ => false
  }

  override def hashCode(): Int = {
    // id だけでハッシュを作る（equals と整合）
    getId.hashCode()
  }
}

object Movie {

  // ── Movie(...) で作れるようにするファクトリ ──
  // class Movie は引数なしコンストラクタにして、生成後に setXxx で値を詰める
  // （講義の「JavaBeans的に詰める」流れと相性がよい）
  def apply(id: Long, fileName: String, time: String, filePath: String, media: Media): Movie = {
    val movie = new Movie
    movie.setId(id)
    movie.setFileName(fileName)
    movie.setTime(time)
    movie.setFilePath(filePath)
    movie.setMedia(media)
    movie
  }
}
