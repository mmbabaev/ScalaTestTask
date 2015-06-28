import akka.actor._

import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import Messages._


object Server extends App  {
  val system = ActorSystem("RemoteSystem")
  val remoteActor = system.actorOf(Props[ServerActor], name = "ServerActor")
}


class ServerActor extends Actor {
  var players = new ArrayBuffer[ActorRef](2)
  var game: Game = null
  var gameThread: Thread = null

  def receive = {
    case LogIn => {

      players.append(sender)

      if (players.length != 2) {
        println("Player 1 joined")
        sender ! SetPlayerNumber(1)
        sender ! Wait
      }
      else {
        println("Player 2 joined")
        sender ! SetPlayerNumber(2)
        players(0) ! StartGame
        players(1) ! StartGame

        game = new Game(players(0), players(1), self)
        gameThread = new Thread(game)
        gameThread.start()

        players = new ArrayBuffer[ActorRef](2)
      }
    }

    case SpaceMessage(message) => game.receiveResult(message, sender)

    case GameResults(loser, winner, reason) => {
      reason match {
        case WrongString() => {
          winner ! Win("Your opponent enter wrong string.")
          loser ! Lose("You enter wrong string.")
          gameThread.stop()
        }
        case HurryUp() => {
          winner ! Win("Your opponent enter something too early.")
          loser ! Lose("You enter something too early")
          gameThread.stop()
        }
        case Faster() => {
          println("second")
          winner ! Win("You are faster than another player!")
          loser ! Lose("Your opponent is faster than you!")
        }
      }
      println("Game over")
    }


    case StopGame => gameThread.stop()
  }
}

/**
 * Game entity
 *
 * @param player1 player1 actor reference
 * @param player2 player2 actor reference
 * @param server server actor reference
 */
class Game(player1: ActorRef, player2: ActorRef, server: ActorRef) extends Runnable {
  var currentNumber = 1

  /**
   * Show current game state(number) to both players
   */
  override def run(): Unit = {
    val gen = new Random()
    while (currentNumber != 4) {

      val secsToWait = gen.nextInt(3) + 2
      Thread.sleep(secsToWait * 1000)

      player1 ! GameCurrentNumber(currentNumber)
      player2 ! GameCurrentNumber(currentNumber)
      currentNumber += 1
    }
  }

  /**
   * Handle player's message during the game
   *
   * @param sender player-actor who send the message
   * @param message message of player
   */
  def receiveResult(message: String, sender: ActorRef): Unit = {
    val playerNumber = sender ! GetPlayerNumber
    val player = sender
    val anotherPlayer = if (playerNumber != 1) player1 else player2 //opponent of sender

    if (currentNumber < 3) {
      server ! GameResults(player, anotherPlayer, HurryUp())
      return
    }

    if (message.equals(" ") == false) {
      server ! GameResults(player, anotherPlayer, WrongString())
      return
    }
    println("first")
    server ! GameResults(anotherPlayer, player, Faster())
  }
}