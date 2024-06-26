package net.nerdypuzzle.structureviewer.parts;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import net.mcreator.minecraft.MCItem;
import net.mcreator.ui.MCreator;
import org.apache.commons.lang3.StringUtils;
import org.jnbt.CompoundTag;
import org.jnbt.ListTag;
import org.jnbt.NBTInputStream;
import org.jnbt.Tag;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static net.mcreator.util.image.ImageUtils.toBufferedImage;

public class ScenePanel extends JFXPanel {
    private int WIDTH = 1300;
    private int HEIGHT = 900;
    public Scene scene;
    public Group group;
    public Camera camera;

    private final MCreator mcreator;
    private double anchorX, anchorY, anchorAngleX = 0, anchorAngleY = 0;
    private final DoubleProperty angleX;
    private final DoubleProperty angleY;

    public static Color getAverageColor(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        int redSum = 0;
        int greenSum = 0;
        int blueSum = 0;
        int nonEmptyPixels = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                if (alpha != 0) { // Non-empty pixel
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    redSum += red;
                    greenSum += green;
                    blueSum += blue;
                    nonEmptyPixels++;
                }
            }
        }

        if (nonEmptyPixels == 0) {
            return Color.WHITE; // Return white if there are no non-empty pixels
        }

        double avgRed = (double) redSum / nonEmptyPixels;
        double avgGreen = (double) greenSum / nonEmptyPixels;
        double avgBlue = (double) blueSum / nonEmptyPixels;

        return Color.rgb((int) avgRed, (int) avgGreen, (int) avgBlue);
    }

    @Nullable
    private String getKeyForValue(Map<?, ?> mapping, String value) {
        for (Map.Entry<?, ?> entry : mapping.entrySet()) {
            Object key = entry.getKey();
            Object mappedObject = entry.getValue();

            if (mappedObject instanceof String && mappedObject.equals(value)) {
                return (String) key;
            } else if (mappedObject instanceof List) {
                List<?> mappingValuesList = (List<?>) mappedObject;
                if (mappingValuesList.size() > 0 && mappingValuesList.get(0).equals(value)) {
                    return (String) key;
                }
            }
        }
        return null; // Key not found for the given value
    }
     public ScenePanel(File file, int width, int height, MCreator mcreator) {
         group = new Group();
         WIDTH = width - 150;
         HEIGHT = height - 50;
         scene = new Scene(group, WIDTH, HEIGHT, true);
         Paint paint = new Color(1, 1, 1, 0);
         scene.setFill(paint);
         camera = new PerspectiveCamera();
         scene.setCamera(camera);

         this.mcreator = mcreator;

         Tag root = null;
         CompoundTag tag = null;

         String modid = mcreator.getWorkspace().getWorkspaceSettings().getModID();

         try {
             FileInputStream fis = new FileInputStream(file);
             NBTInputStream nbt = new NBTInputStream(fis);
             root = nbt.readTag();
             if (root instanceof CompoundTag c)
                 tag = c;
         } catch (Exception e) {
             e.printStackTrace();
         }

         List<String> block_names = new ArrayList<>();

         int airIndex = 0;
         boolean foundAir = false;
         ListTag palette = (ListTag)tag.getValue().get("palette");
         Iterator var5 = palette.getValue().listIterator();
         while (var5.hasNext()) {
             CompoundTag compound = (CompoundTag)var5.next();
             String blockname = (String) compound.getValue().get("Name").getValue();
             boolean isAir = blockname.equals("minecraft:air");
             if (isAir) {
                 foundAir = true;
             }
             else if (!foundAir) {
                 airIndex++;
             }
             block_names.add(blockname);
         }

         ListTag size = (ListTag)tag.getValue().get("size");
         int sizeX = (int)size.getValue().get(0).getValue();
         int sizeY = (int)size.getValue().get(1).getValue();
         int sizeZ = (int)size.getValue().get(2).getValue();


         ListTag blocks = (ListTag)tag.getValue().get("blocks");
         Iterator var6 = blocks.getValue().listIterator();
         while (var6.hasNext()) {
             CompoundTag block = (CompoundTag)var6.next();
             int state = (int)block.getValue().get("state").getValue();
             if (!foundAir || state != airIndex) {
                 Box box = new Box(5, 5, 5);
                 String value = block_names.get(state);
                 if (value.contains("minecraft:")) {
                     value = StringUtils.upperCase(value);
                     value = value.replace("MINECRAFT:", "Blocks.");
                     Map<?, ?> mapping = mcreator.getWorkspace().getGenerator().getMappings().getMapping("blocksitems");
                     String key = getKeyForValue(mapping, value);
                     if (key != null) {
                         Color avgColor = getAverageColor(toBufferedImage(MCItem.getBlockIconBasedOnName(mcreator.getWorkspace(), key).getImage()));
                         PhongMaterial material = new PhongMaterial(avgColor);
                         box.setMaterial(material);
                     }
                 } else if (value.contains(modid)) {
                     final String regname = value.replace(modid + ":", "");
                     if (mcreator.getWorkspace().getWorkspaceInfo().hasElementsOfBaseType("block")) {
                         Color avgColor = getAverageColor(toBufferedImage(
                         mcreator.getWorkspace().getModElements().parallelStream().filter((modElement -> {
                             return modElement.getRegistryName().equals(regname);
                         })).findFirst().get().getElementIcon().getImage()));
                         PhongMaterial material = new PhongMaterial(avgColor);
                         box.setMaterial(material);
                     }
                 }
                 ListTag pos = (ListTag) block.getValue().get("pos");
                 box.setTranslateX(box.getTranslateX() + ((sizeX * 5) / 2) - (int) pos.getValue().get(0).getValue() * 5);
                 box.setTranslateY(box.getTranslateY() + ((sizeY * 5) / 2) - (int) pos.getValue().get(1).getValue() * 5);
                 box.setTranslateZ(box.getTranslateZ() + ((sizeZ * 5) / 2) - (int) pos.getValue().get(2).getValue() * 5);
                 group.getChildren().add(box);
             }
         }

         group.translateXProperty().set(WIDTH/2);
         group.translateYProperty().set(HEIGHT/2);
         group.translateZProperty().set(-1200);

         angleX = new SimpleDoubleProperty(0);
         angleY = new SimpleDoubleProperty(0);

         Rotate xRotate;
         Rotate yRotate;
         group.getTransforms().addAll(
                 xRotate = new Rotate(0, Rotate.X_AXIS),
                 yRotate = new Rotate(0, Rotate.Y_AXIS)
         );
         xRotate.angleProperty().bind(angleX);
         yRotate.angleProperty().bind(angleY);

         scene.setOnMousePressed(event -> {
             anchorX = event.getSceneX();
             anchorY = event.getSceneY();
             anchorAngleX = angleX.get();
             anchorAngleY = angleY.get();
         });

         scene.setOnMouseDragged(event -> {
             angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
             angleY.set(anchorAngleY + anchorX - event.getSceneX());
         });

         scene.setOnScroll(event -> {
             double movement = -event.getDeltaY();
             group.translateZProperty().set(group.getTranslateZ() + movement);
         });

         this.setScene(scene);
     }
}
