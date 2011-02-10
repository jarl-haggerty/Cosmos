/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cosmos.weapons

import cosmos.agents.TruthAndBeauty
import felidae.GameState
import felidae.agents.Agent
import org.jbox2d.common.Vec2

abstract class Weapon(gameState : GameState) extends Agent(null, gameState) {
    var velocity = new Vec2
    def fire(input : Vec2) = {
        velocity = input
        bodyDef.angle = java.lang.Math.atan2(velocity.y, velocity.x).toFloat
        gameState.roster += name -> this
    }

    def build(user : TruthAndBeauty) : Weapon
}
