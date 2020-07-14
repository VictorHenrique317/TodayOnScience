package sample.db;

import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import sample.articles.Article;
import sample.utils.Commands;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DataBase {
    private static final DataBase instance = new DataBase();
    private Connection conn;
    private int lastID;
    private boolean isTestRun;

    private List<Article> searchResults;

    private DataBase() {
        String path = System.getProperty("user.home") + "\\Documents\\TodayOnScience\\ArticleDataBase.sqlite3";
        String DataBasePath = "jdbc:sqlite:" + path;

        try{
            File file = new File(path);
            if(!file.exists()){
                System.out.println("database doesnt exist");
                System.out.println(path);

                new File(path.replace("\\ArticleDataBase.sqlite3", "")).mkdir();
                file.createNewFile();
                conn = DriverManager.getConnection(DataBasePath);
                try(Statement statement = conn.createStatement()){
                    for (Tables table : Tables.values())
                    statement.execute(Commands.CREATE_TABLE + "\"" + table.toString() + "\"" + "(\n" +
                            "        \"id\"    INTEGER UNIQUE,\n" +
                            "        \"title\" TEXT UNIQUE,\n" +
                            "        \"link\"  TEXT UNIQUE,\n" +
                            "        \"bytes\" BLOB,\n" +
                            "        PRIMARY KEY(\"id\")\n" +
                            ");");
                    this.isTestRun = true;
                }
            }else{ isTestRun = false;}

            conn = DriverManager.getConnection(DataBasePath);
            searchResults = new ArrayList<>();


        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            for (Tables table: Tables.values()) {
                try (Statement statement = conn.createStatement()){
                    statement.executeQuery(Commands.SELECT_EVERYTHING + table.toString());
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public boolean isTestRun(){
        return isTestRun;
    }

    public static DataBase getInstance() {
        return instance;
    }

    public boolean dump(ArrayList<Article> articles, Tables site) {
        //dumps the list into the database if first item o the list isn't the last database record
        if (articles.size() == 0) {
            System.out.println("empty list");
            return true;
        }
        try (PreparedStatement statement = conn.prepareStatement(Commands.INSERT + site.toString() + Commands.VALUES)) {
            //INSERT INTO table (title, link, bytes) VALUES(?, ?, ?)
            conn.setAutoCommit(false);// beginning of transaction
            for (Article article : articles) {
                statement.setString(1, article.getTitle());
                statement.setString(2, article.getHyperlink());
                statement.setBytes(3, article.getPhotoBytes());
                try {
                    statement.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("Article " +  article.getTitle() + " already exists");
//                    e.printStackTrace();
                }
            }
            updateLastIndex(site);
        } catch (SQLException e) {
            //records already exist
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void updateLastIndex(Tables table) {
        if (table == null) {
            this.lastID = searchResults.size() - 1;
            return;
        }
        try (Statement lastIndexStatement = conn.createStatement()) {
            ResultSet result = lastIndexStatement.executeQuery(Commands.QUERY_MAX_ID + table.toString());
            this.lastID = result.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Article> getArticlesForIndexRange(int pageSystemInitialIndex, int pageSystemFinalIndex, Tables table) {
        /**
         This method will execute the index reversing mechanism and return the articles for the provided Site.
         */
        //maxID - index of page system = index of database
        boolean onSearch = false;
        if (table == null) onSearch = true;

        updateLastIndex(table);
        String tableName = null;
        if (!onSearch) tableName = table.toString();

        ArrayList<Article> articles = new ArrayList<>();
        try {
            conn.setAutoCommit(false);// beginning of transaction
            if (this.lastID - pageSystemInitialIndex <= 0) return articles; // end of database
            //database final index is less than equal to zero

            int databaseFinalIndex = this.lastID - pageSystemInitialIndex;
            int databaseInitialIndex = this.lastID - pageSystemFinalIndex;
            if (databaseInitialIndex < 1) databaseInitialIndex = 1;
            //prevents negative indexes, making possible the query of less articles than the number of containers

            for (int i = databaseInitialIndex; i <= databaseFinalIndex; i++) {
                //iterates and query the entries by the ID's
                //SELECT title, link, byte FROM table WHERE columnName=?
                if (onSearch) {
                    //then get articles from the searchResults arraylist
                    articles.add(this.searchResults.get(i));
                    continue;
                }
                try (PreparedStatement statement = conn.prepareStatement(Commands.SELECT_EVERYTHING + tableName
                        + Commands.WHERE_ID)) {
                    statement.setInt(1, i);
                    ResultSet result = statement.executeQuery();
                    String title = result.getString(1);
                    String link = result.getString(2);

                    byte[] bytes = result.getBytes(3);
                    articles.add(new Article(title, link, bytes));
                    result.next();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        Collections.reverse(articles);
        return articles;
    }

    public void search(String searchText, JFXProgressBar progressBar) {
        /**
         This method will select titles LIKE %searchText%, they will be stored on searchResults and sorted by the field
         id.
         */
        System.out.println("searching");
        StringBuilder sb = new StringBuilder();
        sb.append("% ");
        sb.append(searchText);
        sb.append(" %");
        searchText = sb.toString();

        double rowCounter = 0;
        double rows;
        searchResults.clear();
        try {
            conn.setAutoCommit(false); // beginning of transaction
            for (Tables site : Tables.values()) {// queries all sites but search_ordered for the searchText
                System.out.println("querying table");
                try (PreparedStatement statement = conn.prepareStatement(Commands.SELECT_COUNT + site.toString()+
                        Commands.WHERE_TITLE_LIKE)){
                    statement.setString(1, searchText);
                    rows = statement.executeQuery().getInt(1);
                }
                try (PreparedStatement statement = conn.prepareStatement("SELECT id, title, link, bytes FROM "
                        + site.toString() + Commands.WHERE_TITLE_LIKE)) {
                    statement.setString(1, searchText);
                    ResultSet results = statement.executeQuery();
                    rowCounter = 0;
                    do {
                        rowCounter++;

                        double progress;
                        double tempProgress = ((rowCounter/rows)/Tables.values().length); // progress on the current table
                        double tableProgress = (site.ordinal() / (double) Tables.values().length); //progress already done in other tables;
                        tempProgress += tableProgress;

                        progress = tempProgress;
                        if (rowCounter > rows) {
                            Platform.runLater(()->progressBar.setProgress(progress + 1d/ Tables.values().length));
                            System.out.println(progress + 1d/Tables.values().length);
                            break;
                        }
                        System.out.println("progress " + progress);
                        Platform.runLater(()->progressBar.setProgress(progress));
                        System.out.println( "row counter: " + rowCounter + "\n" +
                                "rows:" + rows + "\n" +
                                "table progress " + site.ordinal() / (double) Tables.values().length + "\n" +
                                "");
                        if(results.isClosed()){
                            // no results found
                            Platform.runLater(()->progressBar.setProgress(progress + 1d/ Tables.values().length));
                            break;
                        }
                        searchResults.add(new Article(results.getInt(1), results.getString(2),
                                results.getString(3), results.getBytes(4)));
                    } while (results.next());


                }
            }
            System.out.println("=============================================================");
            progressBar.setProgress(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(searchResults);
    }

    public int getLastID(Tables site) {
        if (site == null) {
            //then its looking for the lastID of the arraylist
            return (searchResults.size() - 1);
        }
        updateLastIndex(site);
        return this.lastID;
    }

    public List<Article> getLastArticleFromTables(List<Tables> tables){
        List<Article> articles = new ArrayList<>();
        for (Tables table : tables) {
            try (Statement statement = conn.createStatement()) {
                ResultSet result = statement.executeQuery(Commands.SELECT_EVERYTHING + table.toString() +
                        Commands.WHERE_ID_MAX_ID + table.toString() + ")");
                articles.add(new Article(result.getString(1), result.getString(2), result.getBytes(3)));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return articles;
    }

    public String getLastArticleTitle(boolean onSafetyMode, int safetyOffset, Tables site) {
        updateLastIndex(site);
        String title = "";
        if (onSafetyMode) {
            //retrieves title for ID = last item ID - safetyOffset
            try (PreparedStatement statement = conn.prepareStatement(Commands.SELECT_EVERYTHING + site.toString() +
                    Commands.WHERE_ID)) {
                statement.setInt(1, (this.lastID - safetyOffset));
                ResultSet result = statement.executeQuery();
                title = result.getString(1);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (Statement statement = conn.createStatement()) {
                //SELECT title FROM table WHERE id = lastID
                String command = Commands.SELECT_TITLE + site.toString() + Commands.WHERE_ID.replace("?", "") + lastID;
                ResultSet result = statement.executeQuery(command);
                title = result.getString(1);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return title;
    }

    public void closeDataBase() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
