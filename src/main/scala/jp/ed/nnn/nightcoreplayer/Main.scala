package jp.ed.nnn.nightcoreplayer

// --- import ---
import java.io.File //動画ファイルをresource配下としたため不要だったがドラッグ＆ドロップ機能追加で必要になる
import javafx.application.Application
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.{Label, TableColumn, TableRow, TableView}
import javafx.scene.input.{DragEvent, MouseEvent, TransferMode}
import javafx.scene.layout.{BorderPane, HBox}
import javafx.scene.media.{Media, MediaPlayer, MediaView}
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.{Callback, Duration}

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
  private[this] val tableMinWidth = 300

  override def start(primaryStage: Stage): Unit = {
    val mediaView = new MediaView() //実際に映像を表示するJavaFXのNodeの子クラスのインスタンスをプレイヤーを引数として作成
    // --- 画面作成 ---
    // 再生時間表示用のラベルを作成
    val timeLabel = new Label()
    
    // 動画読み込み前の初期表示の再生時間（再生前なので 00:00:00）
    timeLabel.setText("00:00:00/00:00:00")
    timeLabel.setTextFill(Color.WHITE) // 文字色を白に設定

    // --- ツールバー（再生時間ラベルを配置するための横並びコンテナ） ---
    val toolBar = new HBox(timeLabel) // ツールバー作成
    toolBar.setMinHeight(toolBarMinHeight) // ツールバーの最低の高さを設定
    toolBar.setAlignment(Pos.CENTER) // ツールバー内の要素を中央寄せにする
    toolBar.setStyle("-fx-background-color: Black") // ツールバーの背景色を黒に設定

    // --- TableView(Listener) ---
    val tableView = new TableView[Movie]()
    tableView.setMinWidth(tableMinWidth)

    // movies(Observable)
    val movies = FXCollections.observableArrayList[Movie]()
    tableView.setItems(movies)

    // --- TableViewの行 ---
    // setRowFactory:TableViewが「各行の見た目(TableRow)」を作るときの工場（Factory）を指定 
    tableView.setRowFactory(new Callback[TableView[Movie], TableRow[Movie]]() {
      // TableViewから呼ばれる1行分のTableRowを作って返すメソッド  paramは「どのTableViewに対するRowを作っているかの情報
      override def call(param: TableView[Movie]): TableRow[Movie] = {
        val row = new TableRow[Movie]() // 1行分のUIコンポーネント
        // 行がクリックされたときのイベントリスナの登録と処理
        row.setOnMouseClicked(new EventHandler[MouseEvent] {
          override def handle(event: MouseEvent): Unit = {
            if (event.getClickCount >= 1 && !row.isEmpty) { // クリック回数 >= 1 かつ、空行でないことをチェック
              playMovie(row.getItem, mediaView, timeLabel) // クリックされた行に紐づくMovieを取り出して再生
            }
          }
        })
        row // 作成した行を TableView に返す（これが実際に表示される）
      }
    })

    // --- TableViewの列 --- 
    val fileNameColumn = new TableColumn[Movie, String]("ファイル名")
    fileNameColumn.setCellValueFactory(new PropertyValueFactory("fileName"))
    fileNameColumn.setPrefWidth(160)

    val timeColumn = new TableColumn[Movie, String]("時間")
    timeColumn.setCellValueFactory(new PropertyValueFactory("time"))
    timeColumn.setPrefWidth(80)

    tableView.getColumns.setAll(fileNameColumn, timeColumn)

    // --- BorderPane（レイアウト管理） ---
    val baseBorderPane = new BorderPane() // BorderPane作成　上下左右中央にUIを配置できるレイアウト
    baseBorderPane.setStyle("-fx-background-color: Black") // アプリ全体の背景色を黒にする
    baseBorderPane.setCenter(mediaView) // BorderPaneクラスの中央にmediaView（動画）を配置
    baseBorderPane.setBottom(toolBar) // BorderPane の下部にツールバー（再生時間表示）を配置
    baseBorderPane.setRight(tableView) // BorderPaneの右側にTableViewを配置

    // --- Scene（画面全体をまとめる） ---
    val scene = new Scene(baseBorderPane, mediaViewFitWidth + tableMinWidth, mediaViewFitHeight + toolBarMinHeight) // Scene作成 各々の高さの合計=全体の高さとする
    scene.setFill(Color.BLACK) // Scene 全体の背景色を黒に設定
    mediaView.fitWidthProperty().bind(scene.widthProperty().subtract(tableMinWidth)) //mediaView の幅のプロパティを取得し、その値はオブザーバブルになっている
    mediaView.fitHeightProperty().bind(scene.heightProperty().subtract(toolBarMinHeight)) // Scene の幅に変更があった際に、MediaViewの幅を追従するようにする処理

    // Sceneにドラッグ&ドロップのイベントを追加
    // Scene(Observable),setOnDragOver / setOnDragDropped(Listener)
    scene.setOnDragOver(new EventHandler[DragEvent] {
      override def handle(event: DragEvent): Unit = {
        if (event.getGestureSource != scene && event.getDragboard.hasFiles) {
          event.acceptTransferModes(TransferMode.COPY_OR_MOVE: _*) // 配列（COPY,MOVE)を1個ずつの引数としてばらしてacceptTransferModesに渡す
        }
        event.consume()
      }
    })
    scene.setOnDragDropped(new EventHandler[DragEvent] {
      override def handle(event: DragEvent): Unit = {
        val db = event.getDragboard
        if (db.hasFiles) {
          db.getFiles.toArray(Array[File]()).toSeq.foreach { f =>
            val filePath = f.getAbsolutePath
            val fileName = f.getName
            val media = new Media(f.toURI.toString)
            val time = formatTime(media.getDuration) // NOTE: 講義でも言ってた通り duration はまだ取れないことがある
            val movie = Movie(System.currentTimeMillis(), fileName, time, filePath, media)
            // id重複回避（equalsがid比較なのでcontainsで検知できる）
            while (movies.contains(movie)) {
              movie.setId(movie.getId + 1L)
            }
            movies.add(movie) // ← ここでTableViewが自動更新される
          }
        }
        event.consume()
      }
    })
    primaryStage.setTitle("mp4ファイルをドラッグ&ドロップしてください")
    
    primaryStage.setScene(scene) // メインウィンドウ（Stage）に Scene をセット
    primaryStage.show() //実際にウィンドウを画面に表示する
  }
  // --- 一覧の中のクリックされた動画を再生する処理 ---
  private[this] def playMovie(movie: Movie, mediaView: MediaView, timeLabel: Label): Unit = {
    // すでに再生中の MediaPlayerがある場合は、必ず停止・破棄
    if (mediaView.getMediaPlayer != null) { 
      val oldPlayer = mediaView.getMediaPlayer
      oldPlayer.stop()
      oldPlayer.dispose() // dispose：内部リソース（デコーダ/スレッド等）を解放（これをしないと不具合・重くなる原因になりやすい）
    }
    // 選択された Movie が持つ media（Mediaインスタンス）を使って、新しい MediaPlayerを作成
    val mediaPlayer = new MediaPlayer(movie.media) 
    // 再生中、currentTime（現在の再生位置）が変化するたびに呼ばれるリスナーの登録、処理
    mediaPlayer.currentTimeProperty().addListener(new ChangeListener[Duration] {
      override def changed(
                            observable: ObservableValue[_ <: Duration],
                            oldValue: Duration,
                            newValue: Duration
                          ): Unit =
        timeLabel.setText(formatTime(mediaPlayer.getCurrentTime, mediaPlayer.getTotalDuration))  // 現在の再生時間/動画の総再生時間をフォーマットしてラベルに表示
    })

    // メディアの読み込みが完了（READY）したタイミングで一度だけ呼ばれる処理
    mediaPlayer.setOnReady(new Runnable { // READY後でないと totalDuration が取れないケースがあるため、ここで初回表示も更新する
      override def run(): Unit =
        timeLabel.setText(formatTime(mediaPlayer.getCurrentTime, mediaPlayer.getTotalDuration)) // 総再生時間が取得可能になったタイミングで表示を更新
    })
    mediaView.setMediaPlayer(mediaPlayer) //　作成したMediaPlayerをMediaViewにセット（表示側と再生側を接続）
    mediaPlayer.setRate(1.25) // 再生速度を1.25倍に設定
    mediaPlayer.play() // 再生開始
  }
  
  // --- 再生時間表示 --- フォーマット関数 elapsed:現在の再生位置 duration:動画の総再生時間
  private[this] def formatTime(elapsed: Duration): String =  {
    "%02d:%02d:%02d".format(  // %02d は「2桁で0埋めした整数」を意味するフォーマット指定　例:3→"03"
      // ── 現在の再生時間 ──
      elapsed.toHours.toInt, //時
      elapsed.toMinutes.toInt % 60, //分（60で割った余り）
      elapsed.toSeconds.toInt % 60, //秒（60で割った余り）
    )
  }
  private[this] def formatTime(elapsed: Duration, duration: Duration): String =
    s"${formatTime(elapsed)}/${formatTime(duration)}"
}
