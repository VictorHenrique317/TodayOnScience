package sample.ui.mainWindow;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import sample.articles.Article;
import sample.articles.ArticleList;
import sample.db.DataBase;
import sample.db.Tables;
import sample.ui.Main;
import sample.utils.Effects;
import sample.utils.WindowUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class Controller {
    private LinkedHashMap<Label, ImageView> contentContainers;
    private ArticleList mainArticleList;
    private final Tables defaultSite = Tables.space_flight_now;

    @FXML
    private HBox minimizeBox, maximizeBox, closeBox;
    @FXML
    private ImageView minimizeButton, closeButton, maximizeButton, astronautics, search, astronomy, biology, physics,
            logoAtom, logoTube;
    @FXML
    private Label logo, pageLabel, bigLabel, contentLabel1, contentLabel2, contentLabel3, contentLabel4,
            hottestLabel1, hottestLabel2, hottestLabel3, hottestLabel4;
    @FXML
    private ImageView bigImageView, contentImage1, contentImage2, contentImage3, contentImage4,
            hottestImage1, hottestImage2, hottestImage3, hottestImage4;
    @FXML
    private HBox astronauticsBox, searchBox, biologyBox, physicsBox, astronomyBox;
    @FXML
    private ImageView nextButton, previousButton;

    public void initialize() {
        contentContainers = new LinkedHashMap<>();
        contentContainers.put(bigLabel, bigImageView);
        contentContainers.put(contentLabel1, contentImage1);
        contentContainers.put(contentLabel2, contentImage2);
        contentContainers.put(contentLabel3, contentImage3);
        contentContainers.put(contentLabel4, contentImage4);
        this.mainArticleList = new ArticleList(this.contentContainers, this.pageLabel);


        double expansion = 1.2;
        Effects.expansionEffect(astronauticsBox, expansion);
        Effects.expansionEffect(astronomyBox, expansion);
        Effects.expansionEffect(searchBox, expansion);
        Effects.expansionEffect(physicsBox, expansion);
        Effects.expansionEffect(biologyBox, expansion);

        new Thread(() -> {
            //Thread to query information from DB and put on the main list
            mainArticleList.showArticles(this.defaultSite);
            showHottestTopics();
            Platform.runLater(Main::initializeMainStage);
        }).start();

        WindowUtils.setHeadButtonEffects(this.minimizeBox, this.maximizeBox, this.closeBox);
        WindowUtils.addMaximizeButton(this.maximizeButton);
        setImages();
    }

    private void showHottestTopics() {
        List<Article> articles = DataBase.getInstance().getLastArticleFromTables(List.of(Tables.values()));
        List<Image> images = new ArrayList<>();
        for (int i = 0; i < articles.size(); i++) {
            try {
                Image image = SwingFXUtils.toFXImage(ImageIO.read(
                        new ByteArrayInputStream(articles.get(i).getPhotoBytes())), null);
                images.add(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        hottestImage1.setImage(images.get(0));
        hottestImage2.setImage(images.get(1));
        hottestImage3.setImage(images.get(2));
        hottestImage4.setImage(images.get(3));

        //todo remove

        List<Label> labels = List.of(hottestLabel1, hottestLabel2, hottestLabel3, hottestLabel4);
        for (int i = 0; i < labels.size(); i++) {
            labels.get(i).setText(articles.get(i).getTitle());
            openSiteOnClick(labels.get(i), articles.get(i));
        }
//        hottestLabel1.setText(articles.get(0).getTitle());
//        hottestLabel2.setText(articles.get(1).getTitle());
//        hottestLabel3.setText(articles.get(2).getTitle());
//        hottestLabel4.setText(articles.get(3).getTitle());

    }

    private void setImages(){
        nextButton.setImage(new Image(getClass().getClassLoader().getResource("icons8_next_100px_1.png").toExternalForm()));
        previousButton.setImage(new Image(getClass().getClassLoader().getResource("icons8_previous_100px_1.png").toExternalForm()));
        minimizeButton.setImage(new Image(getClass().getClassLoader().getResource("icons8_subtract_32px.png").toExternalForm()));
        maximizeButton.setImage(new Image(getClass().getClassLoader().getResource("icons8_full_screen_50px.png").toExternalForm()));
        closeButton.setImage(new Image(getClass().getClassLoader().getResource("icons8_multiply_50px.png").toExternalForm()));

        search.setImage(new Image(getClass().getClassLoader().getResource("icons8_search_48px.png").toExternalForm()));
        astronautics.setImage(new Image(getClass().getClassLoader().getResource("icons8_space_shuttle_128px.png").toExternalForm()));
        astronomy.setImage(new Image(getClass().getClassLoader().getResource("icons8_earth_planet_128px.png").toExternalForm()));
        physics.setImage(new Image(getClass().getClassLoader().getResource("icons8_physics_128px.png").toExternalForm()));
        biology.setImage(new Image(getClass().getClassLoader().getResource("icons8_dna_helix_100px.png").toExternalForm()));

        logoTube.setImage(new Image(getClass().getClassLoader().getResource("icons8_test_tube_52px.png").toExternalForm()));
        logoAtom.setImage(new Image(getClass().getClassLoader().getResource("icons8_atom_editor_64px.png").toExternalForm()));
    }

    private void openSiteOnClick(Label label, Article article) {
        label.setOnMouseClicked((mouseEvent) -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                try {
                    Desktop.getDesktop().browse(new URL(article.getHyperlink()).toURI());
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //============================================ Head ====================================================//
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

    @FXML
    private void onSearchBoxClicked() {
        Platform.runLater(Main::showSearchWindow);
    }

    @FXML
    private void onAstronomyBoxClick() {
        mainArticleList.showArticles(Tables.universe_today);
    }

    @FXML
    private void onPhysicsBoxClick() {
        mainArticleList.showArticles(Tables.science_news);
    }

    @FXML
    private void onBiologyBoxClick() {
        mainArticleList.showArticles(Tables.scinews);
    }

    @FXML
    private void onAstronauticsBoxClick() {
        mainArticleList.showArticles(Tables.space_flight_now);
    }
    //============================================ Head ====================================================//

    //============================================ Bottom ====================================================//
    @FXML
    private void next() {
        mainArticleList.nextPage();
    }

    @FXML
    private void previous() {
        mainArticleList.previousPage();
    }

    //============================================ Bottom ====================================================//
    public void setScene(Scene scene) {
        this.mainArticleList.setScene(scene);

        //at this point of the code the scene is not null, so I set the effects in this method
        double expansion = 1.2;

        Effects.cursorExpansionEffect(nextButton, scene, expansion);
        Effects.cursorExpansionEffect(previousButton, scene, expansion);
        Effects.cursorExpansionEffect(astronauticsBox, scene, expansion);
        Effects.cursorExpansionEffect(astronomyBox, scene, expansion);
        Effects.cursorExpansionEffect(searchBox, scene, expansion);
        Effects.cursorExpansionEffect(physicsBox, scene, expansion);
        Effects.cursorExpansionEffect(biologyBox, scene, expansion);

        Effects.cursorEffect(logo, scene);

        String highLightColor = "#287BDE";
        Effects.setCursorAndHighlightEffects(hottestLabel1, scene, highLightColor);
        Effects.setCursorAndHighlightEffects(hottestLabel2, scene, highLightColor);
        Effects.setCursorAndHighlightEffects(hottestLabel3, scene, highLightColor);
        Effects.setCursorAndHighlightEffects(hottestLabel4, scene, highLightColor);

    }


}
