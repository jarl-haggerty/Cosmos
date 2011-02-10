/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cosmos.agents

import felidae.agents.Agent
import felidae.GameState

import felidae.graphics.Input
import felidae.graphics.Rectangle
import net.java.games.input.Component
import net.java.games.input.Controller
import org.jbox2d.collision.shapes.PolygonDef
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.contacts.ContactPoint
import org.jbox2d.util.nonconvex.Polygon

class Camera(code : Map[String, String], gameState : GameState) extends Agent(code, gameState) {
    var trackingName : String = {
        if(code.keySet contains "Tracking") {
            code("Tracking")
        } else {
            null
        }
    }
    var tracking : Agent = null
    var height = code("CameraHeight").toFloat
    var camera : Rectangle = new Rectangle(0, 0, 1, 1)
    var incHeight = false
    var decHeight = false
    var changeRatio = new Vec2
    var shape : Shape = null
    var lookAt : Vec2 = {
        if(code.keySet contains "LookAt") {
            val temp = code("LookAt").split(",")
            new Vec2(temp(0).toFloat, temp(1).toFloat)
        } else {
            null
        }
    }

    override def initialize : Unit = {
        //gameState.simulationListener.subscribe(this, true);
        //gameState.simulationFilter.filterCollisions(this);

        //println("Aspect Ratio = " + height)
        camera = new Rectangle(0, 0, height*gameState.game.graphics.aspectRatio, height)

        if(trackingName != null){
            tracking = gameState.roster(trackingName)
            val dimensions = new Vec2(camera.width/2, camera.height/2)
            bodyDef.position = tracking.body.getWorldCenter.sub(dimensions)
            camera = new Rectangle(bodyDef.position.x, bodyDef.position.y, camera.width, camera.height)
            gameState.simulation.destroyBody(body)
            body = gameState.simulation.createBody(bodyDef)
        } else if(lookAt != null){
            val dimensions = new Vec2(camera.width/2, camera.height/2)
            bodyDef.position = lookAt.sub(dimensions)
            camera = new Rectangle(bodyDef.position.x, bodyDef.position.y, camera.width, camera.height)
            gameState.simulation.destroyBody(body)
            body = gameState.simulation.createBody(bodyDef)
        }

        val shapeDef = new PolygonDef
        shapeDef.vertices.add(new Vec2(0, 0))
        shapeDef.vertices.add(new Vec2(camera.width, 0))
        shapeDef.vertices.add(new Vec2(camera.width, camera.height))
        shapeDef.vertices.add(new Vec2(0, camera.height))
        shapeDef.density = 1
        shapeDef.friction = 0
        shapeDef.isSensor = true
        shape = body.createShape(shapeDef)
        body.setMassFromShapes
    }

    def adjustHeight(heightChange : Float) = {
        val oldPosition = body.getPosition
        gameState.simulation.destroyBody(body)
        bodyDef.position = oldPosition.sub(new Vec2(heightChange/2*gameState.game.graphics.aspectRatio, heightChange/2))
        body = gameState.simulation.createBody(bodyDef)
        val shapeDef = new PolygonDef
        shapeDef.vertices.add(new Vec2(0, 0))
        shapeDef.vertices.add(new Vec2((height + heightChange/2)*gameState.game.graphics.aspectRatio, 0))
        shapeDef.vertices.add(new Vec2((height + heightChange/2)*gameState.game.graphics.aspectRatio, (height + heightChange/2)))
        shapeDef.vertices.add(new Vec2(0, (height + heightChange/2)))
        shapeDef.density = 1
        shapeDef.friction = 0
        shapeDef.isSensor = true
        body.destroyShape(shape)
        shape = body.createShape(shapeDef)
        body.setMassFromShapes

        height += heightChange
    }

    override def update : Unit = {
        body.wakeUp

        if(incHeight) {
            incHeight = false
            adjustHeight(height/20)
        }
        if(decHeight) {
            decHeight = false
            adjustHeight(-height/20)
        }

        if(tracking != null){
            val dimensions = new Vec2(camera.width/2, camera.height/2);
            body.setLinearVelocity(tracking.body.getWorldCenter.sub(dimensions).sub(body.getPosition()).mul(0.1f/gameState.game.delta))
        }else if(lookAt != null){
            val dimensions = new Vec2(camera.width/2, camera.height/2)
            body.setLinearVelocity(lookAt.sub(dimensions).sub(body.getPosition).mul(0.1f/gameState.game.delta))
        }else{
            body.setLinearVelocity(new Vec2)
        }
        //println("Aspect Ratio = " + height)
        camera = new Rectangle(body.getPosition.x, body.getPosition.y, height*gameState.game.graphics.aspectRatio, height)


        return true;
    }
    override def render : Unit = {
        //println(camera)
        gameState.game.graphics.viewPort = camera
    }
    override def processInput(input : Input) : Unit = {
        if(input.controller.getType == Controller.Type.KEYBOARD) {
            if(input.event.getComponent.getIdentifier == Component.Identifier.Key.DOWN && input.event.getValue == 1) {
                incHeight = true
            }else if(input.event.getComponent.getIdentifier == Component.Identifier.Key.UP && input.event.getValue == 1) {
                decHeight = true
            }
        } else if(input.controller.getType == Controller.Type.MOUSE) {
            if(input.event.getComponent.getIdentifier == Component.Identifier.Axis.Z) {
                lookAt = lookAt.add(input.mouse.worldPoint.sub(lookAt).mul(.1f))
                if(input.event.getValue < 0) incHeight = true else decHeight = true
            }
        }
    }
}
