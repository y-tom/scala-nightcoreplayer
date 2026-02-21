package jp.ed.nnn.nightcoreplayer

// --- import ---
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control._
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.media.MediaView
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Callback

import jp.ed.nnn.nightcoreplayer.SizeConstants._

// --- JavaFX アプリ起動処理 ---
object Main {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Main], args: _*)
  }
}

// --- JavaFX アプリの開始地点 ---
class Main extends Application {
  override def start(primaryStage: Stage): Unit = {
    val mediaView = new MediaView() //実際に映像を表示するJavaFXのNodeの子クラスのインスタンスをプレイヤーを引数として作成
    // --- 画面作成 ---
    // 再生時間表示用のラベルを作成
    val timeLabel = new Label()
    
    // 動画読み込み前の初期表示の再生時間（再生前なので 00:00:00）
    timeLabel.setText("00:00:00/00:00:00")
    timeLabel.setTextFill(Color.WHITE) // 文字色を白に設定

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
              MoviePlayer.play(row.getItem, tableView, mediaView, timeLabel) // クリックされた行に紐づくMovieを取り出して再生
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

    val deleteActionColumn = new TableColumn[Movie, Long]("削除")
    deleteActionColumn.setCellValueFactory(new PropertyValueFactory("id"))
    deleteActionColumn.setPrefWidth(60)
    deleteActionColumn.setCellFactory(new Callback[TableColumn[Movie, Long], TableCell[Movie, Long]]() {
      override def call(param: TableColumn[Movie, Long]): TableCell[Movie, Long] = {
        new DeleteCell(movies, mediaView, tableView)
      }
    })

    tableView.getColumns.setAll(fileNameColumn, timeColumn, deleteActionColumn)

    // ツールバーに各ボタンを追加
    val toolBar = ToolbarCreator.create(mediaView, tableView, timeLabel, primaryStage)

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

    // Sceneにドラッグ&ドロップのイベントを追加 ->リファクタリングでクラスへ抽出
    // Scene(Observable),setOnDragOver / setOnDragDropped(Listener)
    scene.setOnDragOver(new MovieFileDragOverEventHandler(scene))
    scene.setOnDragDropped(new MovieFileDragDroppedEventHandler(movies))
    primaryStage.setTitle("mp4ファイルをドラッグ&ドロップしてください")
    
    primaryStage.setScene(scene) // メインウィンドウ（Stage）に Scene をセット
    primaryStage.show() //実際にウィンドウを画面に表示する
  }
}
