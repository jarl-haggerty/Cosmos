/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cosmos.agents

import felidae.GameState
import felidae.agents.Agent
import felidae.graphics.Rectangle
import org.jbox2d.collision.shapes.CircleDef
import org.jbox2d.common.Vec2
import java.awt.Color
import java.lang.Math

class Bulfdsaflet(var velocity : Vec2, position : Vec2, gameState : GameState) extends Agent(Map("Name" -> "Satellite", "X" -> position.x.toString, "Y" -> position.y.toString), gameState) {
    def this(code : Map[String, String], gameState : GameState) = this(new Vec2(code("VelocityX").toFloat, code("VelocityY").toFloat),
                                                                       new Vec2(code("X").toFloat, code("Y").toFloat),
                                                                       gameState)

    var volume = new Rectangle(position.x, position.y, .1f, .1f)
    var course = Array[Vec2](new Vec2)
    lazy val planets = (gameState.roster.values filter {_.isInstanceOf[Planet]}) map {_.asInstanceOf[Planet]}

    override def initialize : Unit = {
        val shapeDef = new CircleDef
        shapeDef.localPosition = new Vec2(volume.width/2, volume.height/2)
        shapeDef.radius = Math.min(volume.width/2, volume.height/2)
        shapeDef.density = 1000/(Math.PI.toFloat*shapeDef.radius*shapeDef.radius)
        shapeDef.restitution = 1
        body.createShape(shapeDef)
        body.setMassFromShapes

        println(body.getMass)
        body.applyImpulse(velocity.mul(body.getMass), body.getWorldCenter)
    }
    override def update = {
        val temp = (new Vec2 /: planets.map{_.getForce(this)})(_.add(_))
        body.applyForce(temp, body.getWorldCenter)
        
        val newCourse = new Array[Vec2](600)
        newCourse(0) = body.getWorldCenter
        var velocity = body.getLinearVelocity
        for(i <- 1 until 600) {
            val nextForce = (new Vec2 /: planets.map{_.getForce(newCourse(i-1), body.getMass)})(_.add(_))
            velocity = velocity.add(nextForce.mul(1/60f/body.getMass))
            newCourse(i) = newCourse(i-1).add(velocity.mul(1/60f))
        }
        course = newCourse
    }
    override def render = {
        gameState.game.graphics.color = Color.red
        gameState.game.graphics.draw(body)
        gameState.game.graphics.draw(course)
    }
}
