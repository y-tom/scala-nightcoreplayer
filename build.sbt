// ── プロジェクト全体で共通の設定 ──
ThisBuild / version := "1.0.0-SNAPSHOT"           // プロジェクトのバージョン
ThisBuild / organization := "jp.ed.nnn"          // 組織名（パッケージ名に使われることが多い）
ThisBuild / scalaVersion := "3.3.3"             // Scala 3.3.3 を使用

// ── sbt-assembly 用の設定 ──
// assembly（単一 jar にまとめるタスク）を実行するときはテストをスキップ
assembly / test := {}

// エントリーポイント（Main メソッドのあるクラス）
assembly / mainClass := Some("jp.ed.nnn.nightcoreplayer.Main")

// 出力される jar の名前
assembly / assemblyJarName := "nightcoreplayer.jar"

// ── OS 判定用の設定 ──
// JavaFX のバイナリは OS ごとに異なるため、OS 名を取得
val osName = settingKey[String]("osName")

osName := (System.getProperty("os.name") match {
  case name if name.startsWith("Linux")   => "linux"
  case name if name.startsWith("Mac")     => "mac"
  case name if name.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!") // 対応していない OS の場合は例外
})

// ── ライブラリ依存関係 ──
// JavaFX のモジュールを OS に応じて追加
libraryDependencies ++= Seq(
  "org.openjfx" % "javafx-base"     % "21" classifier osName.value,
  "org.openjfx" % "javafx-controls" % "21" classifier osName.value,
  "org.openjfx" % "javafx-fxml"     % "21" classifier osName.value,
  "org.openjfx" % "javafx-graphics" % "21" classifier osName.value,
  "org.openjfx" % "javafx-web"      % "21" classifier osName.value,
  "org.openjfx" % "javafx-media"    % "21" classifier osName.value
)

// ── sbt-assembly のマージ戦略 ──
import sbtassembly.MergeStrategy

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard  // META-INF 以下は無視
  case x => MergeStrategy.first  // その他は最初のものを使う
}
