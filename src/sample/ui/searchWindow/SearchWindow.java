package sample.ui.searchWindow;

import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import sample.animateFX.animation.RotateIn;
import sample.articles.ArticleList;
import sample.ui.Main;
import sample.utils.Effects;
import sample.utils.WindowUtils;

import java.util.LinkedHashMap;

public final class SearchWindow {
    @FXML
    private GridPane searchMainPane;
    @FXML
    private JFXTextField searchBar;
    @FXML
    private Label pageLabel, logo;
    @FXML
    private Label contentLabel1, contentLabel2, contentLabel3, contentLabel4,
            contentLabel5, contentLabel6, contentLabel7, contentLabel8;
    @FXML
    private ImageView contentImage1, contentImage2, contentImage3, contentImage4,
            contentImage5, contentImage6, contentImage7, contentImage8;
    @FXML
    private ImageView minimizeButton, maximizeButton, closeButton, nextButton, previousButton, loadingIcon,
            search, logoTube, logoAtom;
    @FXML
    private HBox minimizeBox, maximizeBox, closeBox;
    @FXML
    private JFXProgressBar progressBar;

    private ArticleList mainArticleList;
    private final LinkedHashMap<Label, ImageView> contentContainers = new LinkedHashMap<>();



    public void initialize() {
        contentContainers.put(contentLabel1, contentImage1);
        contentContainers.put(contentLabel2, contentImage2);
        contentContainers.put(contentLabel3, contentImage3);
        contentContainers.put(contentLabel4, contentImage4);
        contentContainers.put(contentLabel5, contentImage5);
        contentContainers.put(contentLabel6, contentImage6);
        contentContainers.put(contentLabel7, contentImage7);
        contentContainers.put(contentLabel8, contentImage8);
        progressBar.setVisible(false);

        mainArticleList = new ArticleList(this.contentContainers, this.pageLabel);
        WindowUtils.setHeadButtonEffects(this.minimizeBox, this.maximizeBox, this.closeBox);
        WindowUtils.addMaximizeButton(this.maximizeButton);
        setImages();
        searchMainPane.getStylesheets().clear();
        searchMainPane.getStylesheets().add(getClass().getClassLoader().getResource("stylesheet.css").toString());

    }

    private void setImages(){
        search.setImage(new Image(getClass().getClassLoader().getResource("icons8_search_48px.png").toExternalForm()));

        logoTube.setImage(new Image(getClass().getClassLoader().getResource("icons8_test_tube_52px.png").toExternalForm()));
        logoAtom.setImage(new Image(getClass().getClassLoader().getResource("icons8_atom_editor_64px.png").toExternalForm()));
        minimizeButton.setImage(new Image(getClass().getClassLoader().getResource("icons8_subtract_32px.png").toExternalForm()));
        maximizeButton.setImage(new Image(getClass().getClassLoader().getResource("icons8_full_screen_50px.png").toExternalForm()));
        closeButton.setImage(new Image(getClass().getClassLoader().getResource("icons8_multiply_50px.png").toExternalForm()));

        nextButton.setImage(new Image(getClass().getClassLoader().getResource("icons8_next_100px_1.png").toExternalForm()));
        previousButton.setImage(new Image(getClass().getClassLoader().getResource("icons8_previous_100px_1.png").toExternalForm()));
    }


    //============================================ Head ====================================================//
    @FXML
    private void onLogoClicked() { Main.showMainWindow();}

    @FXML
    private void onSearchBoxEnter() {
        this.loadingIcon.setImage(new Image(getClass().getClassLoader().getResource("icons8_streaming_64px_2.png").toExternalForm()));
        RotateIn animation = new RotateIn(this.loadingIcon);
        animation.setCycleCount(Integer.MAX_VALUE).play();
        progressBar.setVisible(true);

        String textToSearch = searchBar.getText();

        Thread queryThread = new Thread(() -> {
            mainArticleList.search(textToSearch, this.progressBar);
            animation.stop();
            progressBar.setVisible(false);
            this.loadingIcon.setImage(null);
        });
        queryThread.start();
    }

    @FXML
    private void onMinimize() {
        WindowUtils.minimizeFunction();
    }

    @FXML
    private void onClose() {
        WindowUtils.closeFunction();
    }

    @FXML
    private void onMaximize() {
        WindowUtils.maximizeFunction();
    }
    //============================================ Head ====================================================//

    //============================================ Bottom ====================================================//
    @FXML
    private void next() {
        this.mainArticleList.nextPage();
    }

    @FXML
    private void previous() {
        this.mainArticleList.previousPage();
    }
    //============================================ Bottom ====================================================//

    public void setScene(Scene scene) {
        this.mainArticleList.setScene(scene);
        Effects.cursorExpansionEffect(this.nextButton, scene, 1.2);
        Effects.cursorExpansionEffect(this.previousButton, scene, 1.2);

        Effects.cursorEffect(logo, scene);
    }
}
