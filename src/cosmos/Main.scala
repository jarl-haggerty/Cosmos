/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cosmos

import felidae.Game
import felidae.agents.AgentLoader

object Main {

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
      val game = new Game("Demo", new AgentLoader)
      game.run("Level1.xml")
  }

}
