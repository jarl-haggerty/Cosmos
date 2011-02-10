/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cosmos.agents

import felidae.GameState
import felidae.agents.Agent
import felidae.graphics.Input
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.dynamics.contacts.ContactPoint

class Physics(code : Map[String, String], gameState : GameState) extends Agent(code, gameState) {
    val G = code("G").toFloat

}
