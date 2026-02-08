package jp.ed.nnn.nightcoreplayer

// import java.io.File //動画ファイルをresource配下としたため不要
import javafx.application.Application
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.{BorderPane, HBox}
import javafx.scene.media.{Media, MediaPlayer, MediaView}
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Duration

// --- JavaFX アプリ起動処理 ---
object Main {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Main], args: _*)
  }
}

// --- JavaFX アプリの開始地点 ---
class Main extends Application {
  //　MediaView（動画表示用コンポーネント）のサイズを定数として切り出す
  private[this] val mediaViewFitWidth = 800
  private[this] val mediaViewFitHeight = 450
  private[this] val toolBarMinHeight = 50

  override def start(primaryStage: Stage): Unit = {
    // --- resources 配下の動画ファイルを読み込む ---
    val mediaUrl = getClass.getResource("/media/video.mp4").toExternalForm //絶対パスではなくresourcesから読む形に変更
    // --- Media / MediaPlayer / MediaView の生成 ---
    val media = new Media(mediaUrl) //urlからMediaインスタンス作成
    val mediaPlayer = new MediaPlayer(media) //MediaPlayerインスタンス作成
    mediaPlayer.setRate(1.25) // プレイヤーでの再生速度を 1.25 倍に設定
    mediaPlayer.play()
    val mediaView = new MediaView(mediaPlayer) //実際に映像を表示するJavaFXのNodeの子クラスのインスタンスをプレイヤーを引数として作成
    // --- 画面作成 ---
    // 再生時間表示用のラベルを作成
    val timeLabel = new Label()
    // 再生中、currentTime（現在の再生位置）が変化するたびに呼ばれるリスナーを登録
    mediaPlayer.currentTimeProperty().addListener(new ChangeListener[Duration] {
      // 再生時間が更新されるたびに実行される処理
      override def changed(observable: ObservableValue[_ <: Duration], oldValue: Duration, newValue: Duration): Unit =
        // 現在の再生時間 / 動画の総再生時間 をフォーマットしてラベルに表示
        timeLabel.setText(formatTime(mediaPlayer.getCurrentTime, mediaPlayer.getTotalDuration))
    })
    // メディアの読み込みが完了したタイミングで一度だけ呼ばれる処理
    mediaPlayer.setOnReady(new Runnable {
      // 総再生時間が取得可能になったタイミングで表示を更新
      override def run(): Unit =
        timeLabel.setText(formatTime(mediaPlayer.getCurrentTime, mediaPlayer.getTotalDuration))
    })
    // 動画読み込み前の初期表示の再生時間（再生前なので 00:00:00）
    timeLabel.setText("00:00:00/00:00:00")
    timeLabel.setTextFill(Color.WHITE) // 文字色を白に設定

    val toolBar = new HBox(timeLabel) // 再生時間ラベルを配置するための横並びコンテナ（ツールバー）を作成
    toolBar.setMinHeight(toolBarMinHeight) // ツールバーの最低の高さを設定
    toolBar.setAlignment(Pos.CENTER) // ツールバー内の要素を中央寄せにする
    toolBar.setStyle("-fx-background-color: Black") // ツールバーの背景色を黒に設定

    val baseBorderPane = new BorderPane() //画面全体のレイアウトを管理するBorderPaneを作成　上下左右中央にUIを配置できるレイアウト
    baseBorderPane.setStyle("-fx-background-color: Black") //アプリ全体の背景色を黒にする
    baseBorderPane.setBottom(toolBar) // BorderPane の下部にツールバー（再生時間表示）を配置
    baseBorderPane.setCenter(mediaView) //BorderPaneクラスの中央にmediaView（動画）をセット

    val scene = new Scene(baseBorderPane, mediaViewFitWidth, mediaViewFitHeight + toolBarMinHeight) // 画面全体をまとめるSceneを作成 MediaViewの高さ＋ツールバーの高さを、全体の高さとする
    scene.setFill(Color.BLACK) // Scene 全体の背景色を黒に設定
    mediaView.fitWidthProperty().bind(scene.widthProperty()) //mediaView の幅のプロパティを取得し、その値はオブザーバブルになっている
    mediaView.fitHeightProperty().bind(scene.heightProperty().subtract(toolBarMinHeight)) // Scene の幅に変更があった際に、MediaViewの幅を追従するようにする処理


    primaryStage.setScene(scene) // メインウィンドウ（Stage）に Scene をセット
    primaryStage.show() //実際にウィンドウを画面に表示する
  }

  // 再生時間表示用のフォーマット関数 elapsed:現在の再生位置 duration:動画の総再生時間
  private[this] def formatTime(elapsed: Duration, duration: Duration): String =  {
    "%02d:%02d:%02d/%02d:%02d:%02d".format(  // %02d は「2桁で0埋めした整数」を意味するフォーマット指定　例:3→"03"
      // ── 現在の再生時間（左側）──
      elapsed.toHours.toInt, //時
      elapsed.toMinutes.toInt % 60, //分（60で割った余り）
      elapsed.toSeconds.toInt % 60, //秒（60で割った余り）
      // ── 動画の総再生時間（右側）──
      duration.toHours.toInt, //時
      duration.toMinutes.toInt % 60, //分（60で割った余り）
      duration.toSeconds.toInt % 60) //秒（60で割った余り）
  }

}
