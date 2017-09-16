import slick.jdbc.H2Profile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

case class Message(
  sender: String,
  content: String,
  id: Long = 0L
)

final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def sender = column[String]("sender")
  def content = column[String]("content")
  def * = (sender, content, id).mapTo[Message]
}

val db = Database.forConfig("chapter01")

val messages = TableQuery[MessageTable]

def freshTestData = Seq(
  Message("Dave", "Hello, HAL. Do you read me, HAL?"),
  Message("HAL", "Affirmative, Dave. I read you."),
  Message("Dave", "Open the pod bay doors, HAL."),
  Message("HAL", "I'm sorry, Dave. I'm afraid I can't do that.")
)

def exec[T](program: DBIO[T]): T = Await.result(db.run(program), 2 seconds)

exec(messages.schema.create)

messages.schema.createStatements.mkString

val halSays = messages.filter(_.sender === "HAL")

messages ++= freshTestData

messages.filter(_.sender === "HAL").result.statements.mkString

exec(halSays.result)

halSays.map(_.id).result.statements.mkString

exec(halSays.map(_.id).result)

val halSays2 = for {
  message <- messages if message.sender === "HAL"
} yield message

exec(halSays2.result)

val actions: DBIO[Seq[Message]] = (
  messages.schema.create andThen
  (messages ++= freshTestData) andThen
  halSays.result
)

val sameActions: DBIO[Seq[Message]] = (
  messages.schema.create >>
  (messages ++= freshTestData) >>
  halSays.result
)

// Exercise: Bring Your Own Data
val extraneous = Message("Dave", "What if I say 'Pretty please'?")
exec(messages += extraneous)

val daveDialog = messages.filter(_.sender === "Dave")
exec(daveDialog.result)
