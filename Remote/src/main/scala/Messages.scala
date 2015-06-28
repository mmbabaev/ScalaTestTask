package Messages
import akka.actor.ActorRef

/**
 * Created by Mihail on 27.06.15.
 */

case class LogIn()

case class StartGame()
case class StopGame()

case class Wait()

case class GetPlayerNumber()
case class SetPlayerNumber(number: Int)

case class GameCurrentNumber(number: Int)

case class SpaceMessage(message: String)


case class Lose(reason: String)
case class Win(reason: String)

case class StopActor()

trait EndGameReason
case class Faster() extends EndGameReason
case class HurryUp() extends EndGameReason
case class WrongString() extends EndGameReason


case class GameResults(loser: ActorRef, winner: ActorRef, reason: EndGameReason)