/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cosmos.agents

import felidae.GameState
import felidae.agents.Agent
import felidae.graphics.Input
import org.jbox2d.collision.shapes.CircleDef
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.contacts.ContactPoint
import felidae.graphics.Rectangle
import java.lang.Math
import java.awt.Color

class Planet(code : Map[String, String], gameState : GameState) extends Agent(code, gameState) {
    lazy val image = gameState.game.graphics.loadTexture("Agents/Scenery/Textures/" + code("CalicoDisplay"))
    var volume = new Rectangle(bodyDef.position.x, bodyDef.position.y, code("Width").toFloat, code("Height").toFloat)
    var mass = code("Mass").toFloat
    lazy val physics : Physics = gameState.roster("Physics").asInstanceOf[Physics]
    var shape : CircleShape = null

    override def initialize : Unit = {
        val shapeDef = new CircleDef
        shapeDef.localPosition = new Vec2(volume.width/2, volume.height/2)
        shapeDef.radius = Math.min(volume.width/2, volume.height/2)
        shapeDef.density = mass/(Math.PI.toFloat*shapeDef.radius*shapeDef.radius)
        shape = body.createShape(shapeDef).asInstanceOf[CircleShape]
        body.setMassFromShapes
    }
    override def update : Unit = {
        volume = new Rectangle(body.getPosition.x, body.getPosition.y, volume.width, volume.height)
    }
    override def render : Unit = {
        gameState.game.graphics.color = Color.blue
        gameState.game.graphics.draw(body)//gameState.game.graphics.drawTexture(image, volume)
    }
    def getForce(who : Agent) : Vec2 = getForce(who.body.getWorldCenter, who.body.getMass)
    def getForce(where : Vec2, otherMass : Float) : Vec2 = {
        val direction = body.getWorldCenter.sub(where)
        val distanceSquared = direction.lengthSquared
        //if(distanceSquared < shape.getRadius*shape.getRadius) return new Vec2(Float.NaN, Float.NaN)
        direction.normalize
        return direction.mul(physics.G*body.getMass*otherMass/distanceSquared)
    }
}
