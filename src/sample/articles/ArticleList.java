package sample.articles;

import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import sample.db.DataBase;
import sample.db.Tables;
import sample.scrape.SiteLoader;
import sample.utils.Effects;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public final class ArticleList {
    private final LinkedHashMap<Label, ImageView> contentContainers;
    private int initialIndex;
    private int finalIndex;
    private SimpleIntegerProperty currentPage;
    private int pages;
    private Scene scene;
    private Tables table;

    public ArticleList(LinkedHashMap<Label, ImageView> contentContainers, Label pageLabel) {
        this.contentContainers = contentContainers;
        this.initialIndex = 0;
        this.finalIndex = this.initialIndex + (contentContainers.size() - 1);
        this.currentPage = new SimpleIntegerProperty(1);
        this.table = null; // search option
        pageLabel.textProperty().bind(this.currentPage.asString());
    }

    public void showArticles(Tables table) {
        /**
         This method will populate the content containers with articles provided by the articles parameter.
         If its null the method will query the database for articles based on the indices and pages fields from this class
         */
        while (!SiteLoader.isFinished()) {
            //  just waits until data is fully dumped into the database
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("CREATING LIST");
        if (this.table != table) {
            //the table has been changed
            this.table = table;
            restartPageSystem(); //sets the indices back to the start
        }
        this.table = table;

        ArrayList<Article> articles;
        if (table == null) {
            //Then query the database based on the page system
            articles = DataBase.getInstance().getArticlesForIndexRange(this.initialIndex, this.finalIndex, null);
        } else {
            articles = DataBase.getInstance().getArticlesForIndexRange(this.initialIndex, this.finalIndex, table);
        }
        if (articles.size() == 0) {
            //end of data base, rollback page changes
            currentPage.set(currentPage.intValue() - 1);
            return;
        }

        clearContainers();
        ArrayList<Label> keyList = new ArrayList<>(contentContainers.keySet()); //labels of the containers
        Label currentLabel;
        try {
            for (int i = 0; i < contentContainers.size(); i++) {
                currentLabel = keyList.get(i); //gets the current label container
                Image image = null;
                Article currentArticle = articles.get(i);
                try {
                    //creates the image from the bytes
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(currentArticle.getPhotoBytes());
                    image = SwingFXUtils.toFXImage(ImageIO.read(inputStream), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ImageView currentImage = contentContainers.get(currentLabel); // gets the image container from the current label
                currentImage.setImage(image); //sets the image of the container
                currentLabel.setText(currentArticle.getTitle()); // sets the title of the container
                setMouseEffects(currentLabel);
                currentLabel.setOnMouseClicked((e) -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        //left click, open the article link
                        try {
                            String link = currentArticle.getHyperlink();
                            Desktop.getDesktop().browse(new URL(link).toURI());
                        } catch (IOException | URISyntaxException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        } catch (IndexOutOfBoundsException e) {
            //dumb text
        }

        System.out.println("Initial index " + initialIndex);
        System.out.println("Last index " + finalIndex);
        System.out.println("Current page " + currentPage);
    }

    public void search(String textToSearch, JFXProgressBar progressBar) {
        /**
            This function will make the database search the user input, the database will search the user input and create
         an arraylist containing the results
         */
        DataBase.getInstance().search(textToSearch, progressBar);
        Platform.runLater(() -> {
            restartPageSystem();
            showArticles(null);
        });
    }

    private void setMouseEffects(Label label) {
        Effects.setCursorAndHighlightEffects(label, this.scene, "#287BDE");
    }


    private void clearContainers() {
        for (Map.Entry<Label, ImageView> currentEntry : contentContainers.entrySet()) {
            currentEntry.getKey().setText("");
            Effects.removeCursorAndHighlightEffects(currentEntry.getKey());
            currentEntry.getKey().setOnMouseClicked(null);
            currentEntry.getValue().setImage(null);
        }
    }

    private void restartPageSystem() {
        double pageCalc = Math.ceil((double) DataBase.getInstance().getLastID(this.table) / contentContainers.size());
        this.pages = (int) pageCalc;
        if (this.table == null) {
            System.out.println("============ there are " + pages + " pages on search page ");
        } else {
            System.out.println("============ there are " + pages + " pages on site " + this.table.toString());
        }
        if (pages >= 1) {
            //there is content
            currentPage.set(1);
            initialIndex = 0;
            finalIndex = (contentContainers.size() - 1) + initialIndex;
        }
    }

    public void previousPage() {
        //l + F0/l = p
        //so f0= p*l -l
        int length = contentContainers.size();
        if (currentPage.intValue() - 1 > 0) {
            //there is a previous page
            currentPage.set(currentPage.intValue() - 1);
            initialIndex = (currentPage.intValue() * length) - length;
            finalIndex = initialIndex + (length - 1);
            showArticles(this.table);
        }
    }

    public void nextPage() {
        //l + F0/l = p
        //so f0= p*l -l
        int length = contentContainers.size();
        if (currentPage.intValue() + 1 <= pages) {
            //there is a next page
            currentPage.set(currentPage.intValue() + 1);
            initialIndex = (currentPage.intValue() * length) - length;
            finalIndex = initialIndex + (length - 1);
            showArticles(this.table);
        }
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        for (Label label : contentContainers.keySet()) {
            setMouseEffects(label);
        }
    }
}
