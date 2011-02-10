/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cosmos.widgets

import cosmos.agents.TruthAndBeauty
import cosmos.weapons.Nuke
import cosmos.weapons.Bullet
import cosmos.weapons.Weapon
import de.matthiasmann.twl.BoxLayout
import de.matthiasmann.twl.DialogLayout
import de.matthiasmann.twl.ResizableFrame
import de.matthiasmann.twl.model.SimpleChangableListModel
import de.matthiasmann.twl.ComboBox
import de.matthiasmann.twl.ResizableFrame

class WeaponsWidget(val subject : TruthAndBeauty) extends ResizableFrame {
    setTitle("Weapons")

    val options = new SimpleChangableListModel[Weapon](new Bullet(subject.gameState), new Nuke(subject.gameState))

    val combobox = new ComboBox[Weapon](options)
    combobox.addCallback(new Runnable {
        def run = {
            subject.weapon = options.getEntry(combobox.getSelected)
        }
    })
    combobox.setSelected(0)

    val box = new BoxLayout(BoxLayout.Direction.VERTICAL)
    box.setTheme("/optionsdialog")
    box.add(combobox)
    add(box)

    setTheme("resizableframe-title")
}
