package sample.utils;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;

public final class Effects {
    public static void expansionEffect(Node node, double expansion){
        node.setOnMouseEntered((e)->{
            node.setScaleX(expansion);
            node.setScaleY(expansion);
        });
        node.setOnMouseExited((e)->{
            node.setScaleX(1.0);
            node.setScaleY(1.0);
        });
    }

    public static void highlightEffect(Node node, String colorHex, String originalColor){
        node.setOnMouseEntered((e) -> {
            node.setStyle("-fx-background-color:" + colorHex);
        });
        node.setOnMouseExited((e) -> {
            node.setStyle("-fx-background-color:" + originalColor);
        });
    }

    public static void cursorEffect(Node node, Scene scene){
        node.setOnMouseEntered((e)->{
            scene.setCursor(Cursor.HAND);
        });
        node.setOnMouseExited((e)->{
            scene.setCursor(Cursor.DEFAULT);
        });
    }

    public static void cursorExpansionEffect(Node node, Scene scene, double expansion){
        node.setOnMouseEntered((e)->{
            scene.setCursor(Cursor.HAND);
            node.setScaleX(expansion);
            node.setScaleY(expansion);
        });
        node.setOnMouseExited((e)->{
            scene.setCursor(Cursor.DEFAULT);
            node.setScaleX(1.0);
            node.setScaleY(1.0);
        });
    }

    public static void setCursorAndHighlightEffects(Label label, Scene scene, String colorHex) {
        Paint originalColor = label.getTextFill();
        label.setOnMouseEntered((e) -> {
            label.setTextFill(Paint.valueOf(colorHex));
            scene.setCursor(Cursor.HAND);
        });
        label.setOnMouseExited((e) -> {
            label.setTextFill(originalColor);
            scene.setCursor(Cursor.DEFAULT);
        });
    }

    public static void removeCursorAndHighlightEffects(Label label){
        label.setOnMouseEntered(null);
        label.setOnMouseExited(null);
    }

}
