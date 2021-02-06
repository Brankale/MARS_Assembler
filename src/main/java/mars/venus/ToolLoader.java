package mars.venus;

import mars.tools.MarsTool;
import mars.tools.bht_simulator.BHTSimulator;
import mars.tools.bitmap_display.BitmapDisplay;
import mars.tools.cache_simulator.CacheSimulator;
import mars.tools.digital_lab_sim.DigitalLabSim;
import mars.tools.float_representation.FloatRepresentation;
import mars.tools.instruction_counter.InstructionCounter;
import mars.tools.instruction_statistics.InstructionStatistics;
import mars.tools.keyboard_display_simulator.KeyboardAndDisplaySimulator;
import mars.tools.mars_bot.MarsBot;
import mars.tools.memory_visualization.MemoryReferenceVisualization;
import mars.tools.mips_xray.MipsXray;
import mars.tools.scavenger_hunt.ScavengerHunt;
import mars.tools.screen_magnifier.ScreenMagnifier;
import mars.venus.actions.ToolAction;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
	
/*
Copyright (c) 2003-2006,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining 
a copy of this software and associated documentation files (the 
"Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, 
distribute, sublicense, and/or sell copies of the Software, and to 
permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be 
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

/**
 * This class provides functionality to bring external Mars tools into the Mars
 * system by adding them to its Tools menu.  This permits anyone with knowledge
 * of the Mars public interfaces, in particular of the Memory and Register
 * classes, to write applications which can interact with a MIPS program
 * executing under Mars.  The execution is of course simulated.
 */
public class ToolLoader {

    private static final String TOOLS_MENU_NAME = "Tools";

    /**
     * Called in VenusUI to build its Tools menu.
     * @return a JMenu if there are tools available, null otherwise
     */
    public JMenu buildToolsMenu() {
        ArrayList<MarsToolClassAndInstance> tools = loadTools();

        if (!tools.isEmpty()) {
            JMenu menu = new JMenu(TOOLS_MENU_NAME);
            menu.setMnemonic(KeyEvent.VK_T);
            for (MarsToolClassAndInstance tool : tools) {
                ToolAction toolAction = new ToolAction(tool.getToolClass(), tool.getToolName());
                menu.add(toolAction);
            }
            return menu;
        } else {
            return null;
        }
    }

    /**
     * To add a new Mars tool just add a new MarsToolClassAndInstance entry to the tools list.
     * @return a list of the available tools
     */
    private ArrayList<MarsToolClassAndInstance> loadTools() {
        // TODO: the instance of the class is needed just to retrieve the title to
        //       display in the "Tools" menu. I don't like the idea of writing the
        //       title here since it should be declared in the corresponding class.
        //       Find a way to retrieve the title without instantiating the class.
        ArrayList<MarsToolClassAndInstance> tools = new ArrayList<>();
        tools.add(new MarsToolClassAndInstance(BHTSimulator.class, new BHTSimulator()));
        tools.add(new MarsToolClassAndInstance(BitmapDisplay.class, new BitmapDisplay()));
        tools.add(new MarsToolClassAndInstance(CacheSimulator.class, new CacheSimulator()));
        tools.add(new MarsToolClassAndInstance(DigitalLabSim.class, new DigitalLabSim()));
        tools.add(new MarsToolClassAndInstance(FloatRepresentation.class, new FloatRepresentation()));
        tools.add(new MarsToolClassAndInstance(InstructionCounter.class, new InstructionCounter()));
        tools.add(new MarsToolClassAndInstance(InstructionStatistics.class, new InstructionStatistics()));
        tools.add(new MarsToolClassAndInstance(KeyboardAndDisplaySimulator.class, new KeyboardAndDisplaySimulator()));
        tools.add(new MarsToolClassAndInstance(MarsBot.class, new MarsBot()));
        tools.add(new MarsToolClassAndInstance(MemoryReferenceVisualization.class, new MemoryReferenceVisualization()));
        tools.add(new MarsToolClassAndInstance(MipsXray.class, new MipsXray()));
        tools.add(new MarsToolClassAndInstance(ScavengerHunt.class, new ScavengerHunt()));
        tools.add(new MarsToolClassAndInstance(ScreenMagnifier.class, new ScreenMagnifier()));
        return tools;
    }

    private static class MarsToolClassAndInstance {
        private final Class<?> marsToolClass;
        private final MarsTool marsToolInstance;

        MarsToolClassAndInstance(Class<?> marsToolClass, MarsTool marsToolInstance) {
            this.marsToolClass = marsToolClass;
            this.marsToolInstance = marsToolInstance;
        }

        public Class<?> getToolClass() {
            return marsToolClass;
        }

        public String getToolName() {
            return marsToolInstance.getName();
        }
    }
}