package jp.ed.nnn.nightcoreplayer

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.stage.Stage

object Main extends App {
  Application.launch(classOf[Main], args: _*)
}

class Main extends Application {

  override def start(primaryStage: Stage): Unit = {
    val path = "/Users/soichiro_yoshimura/vagrant/ubuntu64/workspace/download/video.mp4"
    val media = new Media(new File(path).toURI.toString)
    val mediaPlayer = new MediaPlayer(media)
    mediaPlayer.setRate(1.25)
    mediaPlayer.play()
    val mediaView = new MediaView(mediaPlayer)
    val baseBorderPane = new BorderPane() //画面レイアウト用の部品　上下左右中央にUIを配置できる
    val scene = new Scene(baseBorderPane, 800, 500) //UI全体のコンテナ ウィンドウサイズを指定
    scene.setFill(Color.BLACK) //背景色を設定
    primaryStage.setScene(scene) //メインウィンドウに、sceneを設定
    primaryStage.show() //実際にウィンドウを画面に表示する
  }
}
