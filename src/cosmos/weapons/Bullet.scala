/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cosmos.weapons

import cosmos.agents.Planet
import cosmos.agents.TruthAndBeauty
import felidae.GameState
import java.awt.Color
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonDef
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Mat22
import org.jbox2d.common.Vec2
import util.control.Breaks.breakable
import util.control.Breaks.break
import org.jbox2d.dynamics.contacts.ContactPoint
import scala.collection.immutable.Queue
import scala.concurrent.ops.spawn
import cosmos.agents.Camera

class Bullet(gameState : GameState, val user : TruthAndBeauty = null) extends Weapon(gameState) {
    def build(user : TruthAndBeauty) = new Bullet(gameState, user)
    val width = .1f
    val length = .2f
    var course = Queue.empty[Vec2]
    lazy val planets = (gameState.roster.values filter {_.isInstanceOf[Planet]}) map {_.asInstanceOf[Planet]}
    zPosition = 0f
    if(user != null) {
        bodyDef.position = user.body.getWorldCenter.sub(new Vec2(length/2, width/2))
    }
    var chartingVelocity = new Vec2
    var chartingPoint = new Vec2
    var solved = false
    bodyDef.angularDamping = 3

    override def initialize = {
        gameState.simulationFilter.verifyCollisions(this)
        gameState.simulationListener.subscribe(this, false)
        val shapeDef = new PolygonDef
        shapeDef.vertices.add(new Vec2(0, 0))
        shapeDef.vertices.add(new Vec2(length, width/2))
        shapeDef.vertices.add(new Vec2(0, width))
        shapeDef.density = 500/(.5f*width*length)
        shapeDef.friction = 0
        shapeDef.restitution = 1
        body.createShape(shapeDef)
        body.setMassFromShapes
        
        body.applyImpulse(velocity.mul(body.getMass), body.getWorldCenter)
    }
    
    def impact(point : Vec2) : Boolean = {
        for(planet <- planets) {
            if(planet.body.getShapeList.asInstanceOf[CircleShape].getRadius*planet.body.getShapeList.asInstanceOf[CircleShape].getRadius >
               planet.body.getShapeList.asInstanceOf[CircleShape].getLocalPosition.add(planet.body.getPosition).sub(point).lengthSquared) {
                return true
            }
        }
        return false
    }

    override def update = {
        val gravity = (new Vec2 /: planets.map{_.getForce(this)})(_.add(_))
        body.applyForce(gravity, body.getWorldCenter)

        var temp = Vec2.dot(Mat22.createRotationalTransform(body.getAngle).mul(new Vec2(0, 1)), body.getLinearVelocity.mul(1/body.getLinearVelocity.length))
        //if(Vec2.dot(Mat22.createRotationalTransform(body.getAngle).mul(new Vec2(1, 0)), body.getLinearVelocity.mul(1/body.getLinearVelocity.length)) < 0) {
            if(temp <= 0) temp = -1// else temp = 1
        //}
        body.applyTorque(temp*500)
        
        if(course.isEmpty) {
            chartingVelocity = body.getLinearVelocity
            chartingPoint = body.getWorldCenter
            course += chartingPoint
            while(course.length < 6000 && !solved) {
                val nextForce = (new Vec2 /: planets.map{_.getForce(chartingPoint, body.getMass)})(_.add(_))
                chartingVelocity = chartingVelocity.add(nextForce.mul(1/60f/body.getMass))
                chartingPoint = chartingPoint.add(chartingVelocity.mul(1/60f))
                if(impact(chartingPoint)) solved = true
                course += chartingPoint
            }
        }else{
            course = course.dequeue._2
            if(!solved) {
                val nextForce = (new Vec2 /: planets.map{_.getForce(chartingPoint, body.getMass)})(_.add(_))
                chartingVelocity = chartingVelocity.add(nextForce.mul(1/60f/body.getMass))
                chartingPoint = chartingPoint.add(chartingVelocity.mul(1/60f))
                if(impact(chartingPoint)) solved = true
                course += chartingPoint
            }
        }
    }

    override def render = {
        gameState.game.graphics.color = Color.red
        gameState.game.graphics.draw(body)
        gameState.game.graphics.draw(course)
    }

    override def verifyCollision(who : Shape) : Boolean = !who.getBody.getUserData.isInstanceOf[TruthAndBeauty] && !who.getBody.getUserData.isInstanceOf[Bullet]
    override def handleCollision(point : ContactPoint) : Unit = if(!point.shape2.getBody.getUserData.isInstanceOf[Camera]) destroy
}
