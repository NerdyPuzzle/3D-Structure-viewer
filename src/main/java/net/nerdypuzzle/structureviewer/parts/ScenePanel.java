package net.nerdypuzzle.structureviewer.parts;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import org.jnbt.CompoundTag;
import org.jnbt.ListTag;
import org.jnbt.NBTInputStream;
import org.jnbt.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

public class ScenePanel extends JFXPanel {
    private int WIDTH = 1300;
    private int HEIGHT = 900;
    public Scene scene;
    public Group group;
    public Camera camera;

    private double anchorX, anchorY, anchorAngleX = 0, anchorAngleY = 0;
    private final DoubleProperty angleX;
    private final DoubleProperty angleY;
     public ScenePanel(File file, int width, int height) {
         group = new Group();
         WIDTH = width - 150;
         HEIGHT = height - 50;
         scene = new Scene(group, WIDTH, HEIGHT, true);
         Paint paint = new Color(1, 1, 1, 0);
         scene.setFill(paint);
         camera = new PerspectiveCamera();
         scene.setCamera(camera);

         Tag root = null;
         CompoundTag tag = null;

         try {
             FileInputStream fis = new FileInputStream(file);
             NBTInputStream nbt = new NBTInputStream(fis);
             root = nbt.readTag();
             if (root instanceof CompoundTag c)
                 tag = c;
         } catch (Exception e) {
             e.printStackTrace();
         }

         int airIndex = 0;
         boolean foundAir = false;
         ListTag palette = (ListTag)tag.getValue().get("palette");
         Iterator var5 = palette.getValue().listIterator();
         while (var5.hasNext()) {
             CompoundTag compound = (CompoundTag)var5.next();
             boolean isAir = compound.getValue().get("Name").getValue().equals("minecraft:air");
             if (isAir) {
                 foundAir = true;
                 break;
             }
             airIndex++;
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

         group.addEventHandler(ScrollEvent.SCROLL, event -> {
             double movement = event.getTextDeltaY();
             group.translateZProperty().set(group.getTranslateZ() + movement);
         });

         this.setScene(scene);
     }
}
