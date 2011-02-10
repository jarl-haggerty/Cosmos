/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cosmos.agents

import cosmos.widgets.WeaponsWidget
import felidae.GameState
import felidae.agents.Agent
import felidae.graphics.Input
import net.java.games.input.Component
import net.java.games.input.Controller
import org.jbox2d.collision.AABB
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonDef
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Mat22
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.contacts.ContactPoint
import java.awt.Color
import java.lang.Math
import util.control.Breaks.breakable
import util.control.Breaks.break
import cosmos.weapons.Bullet
import cosmos.weapons.Nuke
import cosmos.weapons.Weapon

class TruthAndBeauty(code : Map[String, String], gameState : GameState) extends Agent(code, gameState) {
    var thrusting = false
    var stopping = false
    var rotateLeft = false
    var rotateRight = false
    var thrust = 10000
    var torque = 10000
    var heading = new Vec2(1, 0)
    lazy val planets = (gameState.roster.values filter {_.isInstanceOf[Planet]}) map {_.asInstanceOf[Planet]}
    var course = new Vec2 :: Nil
    val width = 1f
    val length = 2f
    bodyDef.angularDamping = 3
    var calculateCourse = false

    var weapon : Weapon = new Nuke(gameState)

    //val weaponsWidget = new WeaponsWidget(this)
    //gameState.game.graphics.desktop.add(weaponsWidget)
    //weaponsWidget.setVisible(true)
    //weaponsWidget.setPosition(0, 0)

    override def initialize : Unit = {
        val shapeDef = new PolygonDef
        shapeDef.vertices.add(new Vec2(0, 0))
        shapeDef.vertices.add(new Vec2(length, width/2))
        shapeDef.vertices.add(new Vec2(0, width))
        shapeDef.density = 5000/(.5f*width*length)
        shapeDef.friction = 0
        body.createShape(shapeDef)
        body.setMassFromShapes
    }
    override def update : Unit = {
        if(thrusting) {
            body.applyForce(Mat22.createRotationalTransform(body.getAngle).mul(new Vec2(thrust, 0)), body.getWorldCenter)
        }
        if(stopping) {
            body.applyForce(Mat22.createRotationalTransform(body.getAngle).mul(new Vec2(-thrust, 0)), body.getWorldCenter)
        }
        var temp = Vec2.dot(Mat22.createRotationalTransform(body.getAngle).mul(new Vec2(0, 1)), heading)
        if(temp <= 0) temp = -1
        body.applyTorque(temp*5000)

        val gravity = (new Vec2 /: planets.map{_.getForce(this)})(_.add(_))
        body.applyForce(gravity, body.getWorldCenter)

        if(calculateCourse){
            var newCourse = body.getWorldCenter :: Nil
            var velocity = body.getLinearVelocity
            breakable {
                for(i <- 1 until 10000) {
                    val nextForce = (new Vec2 /: planets.map{_.getForce(newCourse.head, body.getMass)})(_.add(_))
                    //if(nextForce.x.isNaN) break
                    velocity = velocity.add(nextForce.mul(1/60f/body.getMass))
                    newCourse = newCourse.head.add(velocity.mul(1/60f)) :: newCourse
                    for(planet <- planets) {
                        if(planet.body.getShapeList.asInstanceOf[CircleShape].getRadius*planet.body.getShapeList.asInstanceOf[CircleShape].getRadius >
                           planet.body.getShapeList.asInstanceOf[CircleShape].getLocalPosition.add(planet.body.getPosition).sub(newCourse.head).lengthSquared) {
                            break
                        }
                    }
                }
            }
            course = newCourse
        }
    }

    override def render : Unit = {
        gameState.game.graphics.color = Color.green
        gameState.game.graphics.draw(body)
        gameState.game.graphics.draw(course)
        if(gameState.game.graphics.viewPort.height > 1000) {
            gameState.game.graphics.color = Color.white
            val center = body.getWorldCenter
            var rotationMatrix = Mat22.createRotationalTransform(body.getAngle)
            val front = body.getWorldCenter.add(rotationMatrix.mul(new Vec2(gameState.game.graphics.viewPort.width/20, 0)))
            rotationMatrix = Mat22.createRotationalTransform(body.getAngle + Math.PI.toFloat/2)
            val left = body.getWorldCenter.add(rotationMatrix.mul(new Vec2(gameState.game.graphics.viewPort.width/40, 0)))
            rotationMatrix = Mat22.createRotationalTransform(body.getAngle - Math.PI.toFloat/2)
            val right = body.getWorldCenter.add(rotationMatrix.mul(new Vec2(gameState.game.graphics.viewPort.width/40, 0)))
            gameState.game.graphics.draw(left :: front :: right :: Nil)
        }
    }

    override def processInput(input : Input) : Unit = {
        input.controller.getType match {
            case Controller.Type.KEYBOARD => {
                input.event.getComponent.getIdentifier match {
                    case Component.Identifier.Key.W => thrusting = input.event.getValue == 1
                    case Component.Identifier.Key.S => stopping = input.event.getValue == 1
                    case Component.Identifier.Key.SPACE => calculateCourse = input.event.getValue == 1
                    case _ => {}                                    
                }
            }
            case Controller.Type.MOUSE => {
                input.event.getComponent.getIdentifier match {
                    case Component.Identifier.Axis.X => {
                        val temp = input.mouse.worldPoint.sub(body.getWorldCenter)
                        temp.normalize
                        heading = temp
                    }
                    case Component.Identifier.Axis.Y => {
                        val temp = input.mouse.worldPoint.sub(body.getWorldCenter)
                        temp.normalize
                        heading = temp
                    }
                    case Component.Identifier.Button.LEFT => {
                        if(input.event.getValue == 0) {
                            val temp = input.mouse.worldPoint.sub(body.getWorldCenter)
                            temp.normalize
                            weapon.build(this).fire(temp.mul(10))
                        }
                    }
                    case _ => {}
                }
                
            }
            case _ => {}
        }
    }
}
