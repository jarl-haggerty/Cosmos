/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cosmos.weapons

import cosmos.agents.Planet
import cosmos.agents.TruthAndBeauty
import felidae.GameState
import felidae.graphics.Rectangle
import java.awt.Color
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonDef
import org.jbox2d.collision.shapes.PointDef
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Mat22
import org.jbox2d.common.Vec2
import util.control.Breaks.breakable
import util.control.Breaks.break
import org.jbox2d.dynamics.contacts.ContactPoint
import java.awt.image.BufferedImage
import scala.collection.immutable.Queue
import scala.concurrent.ops.spawn
import cosmos.agents.Camera
import java.lang.Math

object Nuke {
    val explosion = {
        val image = new BufferedImage(512, 512, BufferedImage.TYPE_4BYTE_ABGR)
        for(x <- 0 until 512; y <- 0 until 512) {
            val temp = if((x-255)*(x-255) + (y-255)*(y-255) < 255*255) 255*Math.exp(-(Math.pow((x - 256)/90.0, 2)+Math.pow((y - 256)/90.0, 2))/2) else 0
            val temp2 = (temp.toInt << 24) + (255 << 16) + (255 << 8) + 255
            image.setRGB(x, y, temp2)
        }
        image
    }
}

class Nuke(gameState : GameState, val user : TruthAndBeauty = null) extends Weapon(gameState) {
    def build(user : TruthAndBeauty) = new Nuke(gameState, user)
    //bodyDef.isBullet = true
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
    var epicenter = new Vec2
    var exploding = false
    var triggerTime = 0l
    var explosionPeriod = 10000l
    var maxRadius = 50f
    lazy val explosionTexture = gameState.game.graphics.loadTexture("Nuke Explosion", Nuke.explosion)

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
        //val shapeDef = new PointDef
        //shapeDef.mass = 500
        //shapeDef.localPosition = new Vec2(0, 0)
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

    override def update : Unit = {
        if(exploding) return
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
            course = course enqueue chartingPoint
            while(course.length < 6000 && !solved) {
                val nextForce = (new Vec2 /: planets.map{_.getForce(chartingPoint, body.getMass)})(_.add(_))
                chartingVelocity = chartingVelocity.add(nextForce.mul(1/60f/body.getMass))
                chartingPoint = chartingPoint.add(chartingVelocity.mul(1/60f))
                if(impact(chartingPoint)) solved = true
                course = course enqueue chartingPoint
            }
        }else{
            course = course.dequeue._2
            if(!solved) {
                val nextForce = (new Vec2 /: planets.map{_.getForce(chartingPoint, body.getMass)})(_.add(_))
                chartingVelocity = chartingVelocity.add(nextForce.mul(1/60f/body.getMass))
                chartingPoint = chartingPoint.add(chartingVelocity.mul(1/60f))
                if(impact(chartingPoint)) solved = true
                course = course enqueue chartingPoint
            }
        }
    }

    override def render = {
        if(exploding) {
            if((System.currentTimeMillis-triggerTime) > explosionPeriod) gameState.roster -= this.name
            val radius = maxRadius*Math.sin(Math.PI*(System.currentTimeMillis-triggerTime).toFloat/explosionPeriod).toFloat
            gameState.game.graphics.draw(explosionTexture, new Rectangle(epicenter.x-radius, epicenter.y-radius, 2*radius, 2*radius))
        }else{
            gameState.game.graphics.color = Color.red
            gameState.game.graphics.draw(body)
            gameState.game.graphics.draw(course)
        }
    }

    override def verifyCollision(who : Shape) : Boolean = !who.getBody.getUserData.isInstanceOf[TruthAndBeauty] && !who.getBody.getUserData.isInstanceOf[Nuke]
    override def handleCollision(point : ContactPoint) : Unit = if(!point.shape2.getBody.getUserData.isInstanceOf[Camera]) destroy

    override def destroy = {
        epicenter = body.getWorldCenter
        exploding = true
        triggerTime = System.currentTimeMillis
        gameState.simulation.destroyBody(body)
    }
}
