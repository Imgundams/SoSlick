import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
//import slick.driver.MySQLDriver.api._
import scala.concurrent._
import scala.util.{Failure, Success}


object Main extends App {
	// Go to the Terminal and type 'sbt' then once done type 'run' to run the code

	// The config string refers to mysqlDB that we defined in application.conf
	val db = Database.forConfig("so_slick")

	// represents the actual table on which we will be building queries on
	val peopleTable = TableQuery[People]

	// schema definition to generate DROP statement for people table
	val dropPeopleCmd = DBIO.seq(peopleTable.schema.drop)

	// schema definition to generate a CREATE TABLE command
	val initPeopleCmd = DBIO.seq(peopleTable.schema.create)


	//runQueryUpdate(3, "Scala James", "Whiteley", 22)

	averagePeoplesAge
	print("Yoooooo")


	def dropDB = {
		//do a drop followed by initialisePeople
		val dropFuture = Future {
			db.run(dropPeopleCmd)
		}
		//Attempt to drop the table, Await does not block here
		Await.result(dropFuture, Duration.Inf).andThen {
			case Success(_) => initialisePeople
			case Failure(error) =>
				println("Dropping the table failed due to: " + error.getMessage)
				initialisePeople
		}
	}


	def initialisePeople = { //initialise people
		val setupFuture = Future {
			db.run(initPeopleCmd)
		} //once our DB has finished initializing we are ready to roll, Await does not block
		Await.result(setupFuture, Duration.Inf).andThen {
			case Success(_) => runQuery
			case Failure(error) =>
				println("Initialising the table failed due to: " + error.getMessage)
		}
	}


	def runQuery = {
		val insertPeople = Future {
			val query = peopleTable ++= Seq(
				(1, "Jack", "Wood", 36),
				(2, "Tim", "Brown", 24),
				(3, "James", "Scala", 22),
				(4, "Jim", "Smith", 36),
				(5, "Dave", "Smith", 24),
				(6, "Dug", "Willow", 36),
				(7, "Thomas", "Dover", 24),
				(8, "Jack", "Wood", 36),
				(9, "Vain", "Brown", 24),
				(10, "Jack", "Wood", 36),
				(11, "Tim", "Brown", 24),
				(12, "Jack", "Wood", 36),
				(13, "Tim", "Not", 24),
				(14, "Jack", "Wood", 36),
				(15, "Tim", "What", 24),
				(16, "Jack", "Wood", 36),
				(17, "Tim", "Will", 24),
				(18, "Jack", "Wood", 36),
				(19, "Tim", "Hello", 24),
				(20, "Jack", "Wood", 36),
				(21, "Tim", "Brown", 24)
			) // insert into `PEOPLE` (`PER_FNAME`,`PER_LNAME`,`PER_AGE`)  values (?,?,?)
			println(query.statements.head) // would print out the query one line up
			db.run(query)
		}
		Await.result(insertPeople, Duration.Inf).andThen {
			case Success(_) => listPeople
			case Failure(error) => println("Something went terribly wrong! " + error.getMessage)
		}
	}


	def listPeople = {
		val queryFuture = Future {
			// simple query that selects everything from People and prints them out
			db.run(peopleTable.result).map(_.foreach {
				case (id, fName, lName, age) => println(s" $id $fName $lName $age")
			})
		}
		Await.result(queryFuture, Duration.Inf).andThen {
			case Success(_) => db.close() //cleanup DB connection
			case Failure(error) => println("Listing people failed due to: " + error.getMessage)
		}
	}

	def countPeople = {
		var count:Int =0
		val queryFuture = Future {
			// simple query that selects everything from People and prints them out
			db.run(peopleTable.result).map(_.foreach {p => p ;count+=1})
		}
		Await.result(queryFuture, Duration.Inf).andThen {
			case Success(_) => println("The people count of the database is "+ count)
				db.close() //cleanup DB connection
			case Failure(error) => println("Listing people failed due to: " + error.getMessage)
		}
	}

	def averagePeoplesAge = {
		var count:Int =0
		var averageAge:Int = 0
		val queryFuture = Future {
			// simple query that selects everything from People and prints them out
			db.run(peopleTable.result).map(_.foreach {p => averageAge+=p._4 ;count+=1})
		}
		Await.result(queryFuture, Duration.Inf).andThen {
			case Success(_) => println("The average age of people in the database is "+ averageAge/count)
				db.close() //cleanup DB connection
			case Failure(error) => println("Listing people failed due to: " + error.getMessage)
		}
	}


	def deleteByID(theId: Int) = {
		val removePeople = Future {
			val query = {
				peopleTable.filter(_.id === theId).delete

			}
			println(query.statements.head) // would print out the query one line up
			db.run(query)map {
				_ > 0
			}
		}
		Await.result(removePeople, Duration.Inf).andThen {
			case Success(_) => listPeople
			case Failure(error) => println("Something went terribly wrong! " + error.getMessage)
		}
	}


	def runQueryUpdate(theId: Int, firstName: String, lastName: String, newAge: Int) = {
		val updatePeople = Future {
			val query = {
				peopleTable.filter(_.id === theId)
					.map(p => (p.fName, p.lName, p.age))
					.update(firstName, lastName, newAge)

			}
			println(query.statements.head) // would print out the query one line up
			db.run(query)map {
				_ > 0
			}
		}
		Await.result(updatePeople, Duration.Inf).andThen {
			case Success(_) => listPeople
			case Failure(error) => println("Something went terribly wrong! " + error.getMessage)
		}
	}

}
