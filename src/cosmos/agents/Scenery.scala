/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cosmos.agents

import felidae.agents.Agent
import felidae.GameState
import felidae.graphics.Input
import felidae.graphics.Rectangle
import felidae.graphics.Texture
import java.awt.Color
import java.io.File
import org.jbox2d.collision.shapes.PolygonDef
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.contacts.ContactPoint

class Scenery(code : Map[String, String], gameState : GameState) extends Agent(code, gameState) {
    println("Scenery!")
    lazy val image : Texture = {
        val filePath = "Agents" + File.separator + "Scenery" + File.separator + "Textures" + File.separator + code("CalicoDisplay")
        gameState.game.graphics.loadTexture(filePath)
    }

    val volume = new Rectangle(bodyDef.position.x,
                               bodyDef.position.y,
                               code("Width").toFloat,
                               code("Height").toFloat)
    
    override def initialize : Unit = {
        println("Volume = " + volume)
        val shape = new PolygonDef
        shape.vertices.add(new Vec2(0, 0))
        shape.vertices.add(new Vec2(volume.width, 0))
        shape.vertices.add(new Vec2(volume.width, volume.height))
        shape.vertices.add(new Vec2(0, volume.height))
        shape.density = 1
        shape.friction = 0
        shape.isSensor = true
        body.createShape(shape)
        body.setMassFromShapes
    }
    override def update : Unit = {
        body.setLinearVelocity(new Vec2())
    }
    override def render : Unit = {
        gameState.game.graphics.drawTexture(image, volume)
        gameState.game.graphics.color = Color.white
        gameState.game.graphics.draw(volume)
    }
}
