package net.nerdypuzzle.structureviewer.parts;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.views.ViewBase;

import java.io.File;

public class StructureViewPanel extends ViewBase {
    public ScenePanel panel;
    private String name;
    public File nbt;

    public StructureViewPanel(MCreator mcreator, String name, File nbt, int width, int height) {
        super(mcreator);
        this.name = name;
        panel = new ScenePanel(nbt, width, height, mcreator);
        this.add(PanelUtils.totalCenterInPanel(panel));
    }

    @Override
    public final String getViewName() {
        return name;
    }

}
