package sample.ui.loadingPopUp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sample.animateFX.animation.*;
import sample.scrape.SiteLoader;

import java.util.Objects;

public final class LoadingPopUp {
    @FXML
    private Label siteLoadingLabel;
    @FXML
    private ImageView atomLoading;

    public void initialize(){
        atomLoading.setImage(new Image(getClass().getClassLoader().getResource("icons8_physics_128px.png").toExternalForm()));
        siteLoadingLabel.textProperty().bind(SiteLoader.stateProperty());
        new RotateIn(atomLoading).setCycleCount(Integer.MAX_VALUE).play();
    }
}
