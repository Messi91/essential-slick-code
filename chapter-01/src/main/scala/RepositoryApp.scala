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

object RepositoryApp extends App {

  val db = Database.forConfig("chapter01")

  val messages = TableQuery[MessageTable]

  def freshTestData = Seq(
    Message("Dave", "Hello, HAL. Do you read me, HAL?"),
    Message("HAL", "Affirmative, Dave. I read you."),
    Message("Dave", "Open the pod bay doors, HAL."),
    Message("HAL", "I'm sorry, Dave. I'm afraid I can't do that.")
  )

  def createSchema: Unit = {
    exec(messages.schema.create)
  }

  def printCreationStatements: Unit = {
    val createStatements = messages.schema.createStatements.mkString
    println("Creation statements = " + createStatements)
  }

  def filterMessages: Unit = {
    val halSays = messages.filter(_.sender === "HAL")
    println("Filtered messages = " + exec(halSays.result))
  }

  def insertMessages(data: Seq[Message]): Unit = {
    println("Inserted messages (before) = " + exec(insert))
    val insert: DBIO[Option[Int]] = messages ++= freshTestData
    println("Inserted messages (after) = " + exec(insert))

  }

  def exec[T](program: DBIO[T]): T = Await.result(db.run(program), 2 seconds)

  def apply: Unit = {
    createSchema
    printCreationStatements
    filterMessages
    insertMessages(freshTestData)
  }
}
