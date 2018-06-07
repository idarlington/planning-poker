package client.view

import client.PlanningPokerApp
import client.PlanningPokerApp.Action
import com.github.lavrov.poker.{Participant, PlanningSession}
import outwatch.Sink
import outwatch.dom.VNode
import outwatch.dom.dsl._

object PlanningSessionView {
  def render(planningSession: PlanningSession, user: Participant, sink: Sink[Action]): VNode = {
    val allGaveEstimates =
      planningSession.players
        .map(_.id)
        .forall(planningSession.estimates.participantEstimates.contains)
    def isPlayer = planningSession.players.contains(user)
    def becomePlayer = sink.redirectMap[Unit](_ =>
        PlanningPokerApp.Action.SendPlanningSessionAction(
          PlanningSession.Action.AddPlayer(user)))
    def becomeObserver = sink.redirectMap[Unit](_ =>
      PlanningPokerApp.Action.SendPlanningSessionAction(
        PlanningSession.Action.AddObserver(user)))
    div(
      header(isPlayer, becomePlayer, becomeObserver),
      div(
        if (isPlayer)
          Some(
            CardsView.render(
              planningSession.estimates.participantEstimates.get(user.id),
              sink.redirectMap { card =>
                PlanningPokerApp.Action.SendPlanningSessionAction(
                  PlanningSession.Action.RegisterEstimate(user.id, card))
              }))
        else
          None
      ),
      div(
        "Players",
        ul(
          planningSession.players.toList.map { participant =>
            val name = participant.name
            val status =
              planningSession.estimates.participantEstimates.get(participant.id)
                .map { card =>
                  if (allGaveEstimates)
                    CardsView.cardSign(card)
                  else
                    "+"
                }
                .getOrElse("-")
            li(name, " ", status)
          }
        ),
        "Observers",
        ul(
          for (u <- planningSession.observers.toList)
          yield
            li(u.name)
        )
      )
    )
  }

  private def header(isPlayer: Boolean, becomePlayer: Sink[Unit], becomeObserver: Sink[Unit]): VNode = {
    val isObserver = !isPlayer
    def btnClasses(isActive: Boolean) =
      "btn btn-sm" :: List(if (isActive) "btn-secondary" else "btn-outline-secondary")
    div(`class` :="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom",
      form(className := "form-inline",
        input(`type` := "text", `class` := "form-control form-control-lg border-0", placeholder := "Enter story description")),
      div(`class` := "btn-toolbar mb-2 mb-md-0",
        div(`class` := "btn-group mr-2",
          button(classNames := btnClasses(isPlayer), onClick(()) --> becomePlayer, "Player"),
          button(classNames := btnClasses(isObserver), onClick(()) --> becomeObserver, "Observer")
        )
      )
    )
  }
}
