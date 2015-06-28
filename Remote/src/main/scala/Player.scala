package local
import Messages._
import akka.actor._


object Player extends App {

  implicit val system = ActorSystem("LocalSystem")
  val localActor = system.actorOf(Props[PlayerActor], name = "PlayerActor")
  localActor ! LogIn
}


class PlayerActor extends Actor {

  /** Number of player (1 or 2) */
  var playerNumber = 0


  println("Hello, enter server's port number:")
  private val port = Console.readInt()
  // create the remote actor (Akka 2.1 syntax)

  val remote = context.actorFor("akka.tcp://RemoteSystem@127.0.0.1:" + port + "/user/ServerActor")

  def receive = {

    case LogIn => remote ! LogIn

    case Wait => println("Wait for another player!")

    case StartGame => {
      println("Game started!")

      val thread = new Thread(new Runnable {
        override def run() {
          remote ! SpaceMessage(Console.readLine())
        }
      })

      thread.start()
    }

    case GameCurrentNumber(number) => println(number)

    case SetPlayerNumber(number) => {
      playerNumber = number
      println("You are player number " + number)
    }
    case GetPlayerNumber => playerNumber

    case Win(message) => println("You win! " + message)
    case Lose(message) => println("You lose! " + message)

    case msg: String => println(msg)

    case _ => println("Unknown message!")

  }
}
