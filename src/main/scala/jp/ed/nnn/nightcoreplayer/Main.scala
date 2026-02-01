package jp.ed.nnn.nightcoreplayer

// import java.io.File //動画ファイルをresource配下としたため不要
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.media.{Media, MediaPlayer, MediaView}
import javafx.scene.paint.Color
import javafx.stage.Stage

// --- JavaFX アプリ起動処理 ---
object Main extends App {
  Application.launch(classOf[Main], args: _*)
}

// --- JavaFX アプリの開始地点 ---
class Main extends Application {

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
    val baseBorderPane = new BorderPane() //画面レイアウト用の部品　上下左右中央にUIを配置できる
    baseBorderPane.setStyle("-fx-background-color: Black") //背景色を黒にする
    baseBorderPane.setCenter(mediaView) //BorderPaneクラスの中央にmediaViewをセット
    val scene = new Scene(baseBorderPane, 800, 500) //UI全体のコンテナ ウィンドウサイズを指定
    scene.setFill(Color.BLACK) //背景色を設定
    primaryStage.setScene(scene) //メインウィンドウに、sceneを設定
    primaryStage.show() //実際にウィンドウを画面に表示する
  }
}
