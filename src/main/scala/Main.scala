import java.time.Duration

import com.sun.net.httpserver.Authenticator.Failure

import scala.concurrent.{Await, Future}

/**
	* Created by Administrator on 30/06/2017.
	*/
object Main extends App{





	print("Yoooooo")










	def dropDB={
		val dropFuture = Future{db.run(dropPeopleCmd)}
		Await.result(dropFuture, Duration.Inf).andThen{
			case Success(_) => initialisePeople
				case Failure(error) => println("Bang!")
		}
	}

	def runQuery = {
		val insertPeople= Future {
			val query = peopleTable ++= Seq(
				(10, "Jack", "Wood", 36),
				(20, "Tim", "Brown", 24)
			)
			println(query.statements.head)
			db.run(query)
		}
		Await.result(insertPeople, Duration.Inf).andThen{
			case Success(_) => println("Good Job")
				case Failure(error)=> println("Bad Job")
		}
	}

	def listPeople = {
		val queryFuture = Future{
			db.run(peopleTable.result).map(_.foreach{
				case
			})
		}
	}


}
