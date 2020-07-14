package sample.utils;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import sample.ui.Main;

import java.util.ArrayList;

public final class WindowUtils {
    private static final WindowUtils instance = new WindowUtils();
    private static Image minimizeImage;
    private static Image maximizeImage;
    private static final ArrayList<ImageView> buttons = new ArrayList<>();

    private WindowUtils() {
        minimizeImage = new Image(getClass().getClassLoader().getResource("icons8_resize_file_32px_1.png").toExternalForm());
        maximizeImage = new Image(getClass().getClassLoader().getResource("icons8_full_screen_50px.png").toExternalForm());
    }

    public static void minimizeFunction() {
        Main.getMainStage().setIconified(true);
    }

    public static void closeFunction() {
        Platform.exit();
    }

    public static void maximizeFunction() {
        if (!Main.getMainStage().isMaximized()) {
            Main.getMainStage().setMaximized(true);
            buttons.get(0).setImage(minimizeImage);
            buttons.get(1).setImage(minimizeImage);
            return;
        }
        if (Main.getMainStage().isMaximized()) {
            Main.getMainStage().setMaximized(false);
            buttons.get(0).setImage(maximizeImage);
            buttons.get(1).setImage(maximizeImage);
            return;
        }
    }

    public static void addMaximizeButton(ImageView maximize) {
        buttons.add(maximize);
    }

    public static void setHeadButtonEffects(HBox minimizeBox, HBox maximizeBox, HBox closeBox) {
        Effects.highlightEffect(closeBox, "#E81123", "#420A08");
        Effects.highlightEffect(maximizeBox, "#815B5A", "#420A08");
        Effects.highlightEffect(minimizeBox, "#815B5A", "#420A08");
    }
}
