package net.nerdypuzzle.structureviewer.registry;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.MCreatorTabs;
import net.nerdypuzzle.structureviewer.parts.StructureViewPanel;

import javax.swing.*;
import java.io.File;

public class PluginEventTriggers {
    public static void modifyMenus(MCreator mcreator) {
        StructureViewPanel panel;

        boolean setPanel = false;

        for (;;) {
            if (mcreator.mcreatorTabs.getCurrentTab() != null) {

                if (mcreator.mcreatorTabs.getCurrentTab().getContent().getClass().getName().equals("net.mcreator.ui.views.NBTEditorView")) {

                    if (!setPanel) {
                        try {
                            for (MCreatorTabs.Tab tab : mcreator.mcreatorTabs.getTabs()) {
                                if (tab.getText().equals(mcreator.mcreatorTabs.getCurrentTab().getText() + " 3D view")) {
                                    setPanel = true;
                                    break;
                                }
                            }
                            if (!setPanel) {
                                JPanel content = mcreator.mcreatorTabs.getCurrentTab().getContent();
                                panel = new StructureViewPanel(mcreator, mcreator.mcreatorTabs.getCurrentTab().getText() + " 3D view", new File(mcreator.getFolderManager().getStructuresDir() + "\\" + mcreator.mcreatorTabs.getCurrentTab().getText()), content.getSize().width, content.getSize().height);
                                panel.showView();
                                setPanel = true;
                                System.out.println("success!");
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            setPanel = true;
                        }
                    }

                }
                else
                    setPanel = false;

            }
            else
                setPanel = false;

        }

    }

}
