package sample.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sample.db.DataBase;
import sample.scrape.SiteLoader;
import sample.ui.mainWindow.Controller;
import sample.ui.loadingPopUp.LoadingPopUp;
import sample.ui.searchWindow.SearchWindow;

public class Main extends Application {
    private static Parent mainRoot;
    private static Parent searchRoot;
    private static Parent loadingRoot;

    private static Stage mainStage;
    private static Scene mainScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        SiteLoader.load();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(LoadingPopUp.class.getResource("loadingPopUp.fxml"));
        System.out.println("==== " + fxmlLoader.getLocation());
        loadingRoot = fxmlLoader.load();

        FXMLLoader mainFXML = new FXMLLoader();
        System.out.println("before controller");

        mainFXML.setLocation(Controller.class.getResource("mainWindow.fxml"));
        mainRoot = mainFXML.load();
        System.out.println("after controller");

        Controller controller = mainFXML.getController();

        FXMLLoader searchFXML = new FXMLLoader();
        searchFXML.setLocation(SearchWindow.class.getResource("searchWindow.fxml"));
        searchRoot = searchFXML.load();
        SearchWindow searchWindow = searchFXML.getController();

        mainScene = new Scene(loadingRoot);

        controller.setScene(mainScene);
        searchWindow.setScene(mainScene);


        mainStage = primaryStage;
        mainStage.setScene(mainScene);

        mainStage.initStyle(StageStyle.UNDECORATED);
        mainStage.setResizable(false);
        mainStage.show();

    }

    public static void initializeMainStage() {
        mainStage.hide();
        mainStage.setWidth(1500);
        mainStage.setHeight(820);
        mainStage.centerOnScreen();
        mainScene.setRoot(mainRoot);
        mainStage.show();
    }

    public static void showMainWindow() {mainScene.setRoot(mainRoot);}

    public static void showSearchWindow() { mainScene.setRoot(searchRoot);}

    public static Stage getMainStage() { return mainStage;}

    public static void main(String[] args) { launch(args);}

    @Override
    public void stop() { DataBase.getInstance().closeDataBase();}


}
