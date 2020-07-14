package sample.scrape;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import sample.articles.Article;
import sample.db.DataBase;
import sample.db.Tables;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebScraper implements AutoCloseable{
    private int LAST_SCRAPPED_PAGE;

    private final ArrayList<Article> articles;
    private final String startTag;            //start of the pane in which the content is on
    private final String endTag;              //end of the pane in which the content is on

    private String siteURL;             //url of the news page
    private Tables table;

    private final WebDriver driver;
    private final Pattern titlePattern; //necessary text to identify the title of the news on the second group
    private final Pattern linkPattern;  //necessary text to identify the URL of the photo on the second group
    private final Pattern photoPattern; //necessary text to identify the hyperlink on the second group

    WebScraper(String siteURL, String startTag, String endTag,
                      String titleRegex, String linkRegex, String photoRegex, Tables table,
                      String windowWidth, String windowHeight) {
        this.articles = new ArrayList<>();
        this.startTag = startTag;
        this.endTag = endTag;
        this.siteURL = siteURL;

        this.table = table;

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless", "--disable-gpu", "--window-size=" + windowWidth + "," + windowHeight,
                "--ignore-certificate-errors", "--silent");
        this.driver = new ChromeDriver(chromeOptions);

        this.titlePattern = Pattern.compile(titleRegex);
        this.linkPattern = Pattern.compile(linkRegex);
        this.photoPattern = Pattern.compile(photoRegex);
        probeForLastItem();

    }

    void scrape() {
        System.out.println("scrapping");
        //====================================== initializing variables ===========================================//
        String title = "", hyperlink = "", photoUrl = "", currentLine, originalURL = this.siteURL;
        Matcher titleMatcher, linkMatcher, photoMatcher;
        boolean onContentPane;
        byte[] bytes;
        Document document;
        BufferedReader bf;
        //====================================== initializing variables ===========================================//
        for (int i = LAST_SCRAPPED_PAGE ; i >=1 ; i--) {
            System.out.println("============ "+ i);
            // hardcode LAST_SCRAPPED_PAGE to the last page when first scrapping a site
            changeURL(originalURL + i);
            try {
                document = Jsoup.connect(siteURL).get();
                bf = new BufferedReader(new StringReader(document.outerHtml()));

                onContentPane = false; //for tracking when the bf is inside the pane that holds the articles
                while (true) {
                    currentLine = bf.readLine();
                    if (currentLine == null) break;
                    titleMatcher = this.titlePattern.matcher(currentLine);
                    linkMatcher = this.linkPattern.matcher(currentLine);
                    photoMatcher = this.photoPattern.matcher(currentLine);

                    if (currentLine.trim().equals(startTag)) onContentPane = true; //start of article
                    if (currentLine.trim().equals(endTag))break;

                    //finds the title and saves it to the String title
                    if (titleMatcher.find() && onContentPane) title = titleMatcher.group(1).trim();
                    //finds the hyperlink
                    if (linkMatcher.find() && onContentPane) hyperlink = linkMatcher.group(1).trim();
                    //finds the image and saves it to photoUrl
                    if (photoMatcher.find() && onContentPane) photoUrl = photoMatcher.group(1);

                    if ((!title.isEmpty()) && (!photoUrl.isEmpty()) && (!hyperlink.isEmpty())) {
                        //gets the bytes from the image
                        //adds to articles and reset the variables for another article
                        System.out.println("CREATING ARTICLE " + title);
                        System.out.println(photoUrl);
                        System.out.println(hyperlink);
                        System.out.println("====================================================");

                        driver.get(photoUrl);
                        bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                        articles.add(new Article(title, hyperlink, photoUrl, bytes));
                        title = "";
                        hyperlink = "";
                        photoUrl = "";
                    }
                }
                bf.close();
                System.out.println("DUMPING ITEMS INTO TABLE " + table.toString());
                Collections.reverse(this.articles); // reverse to match the database orientation
                if (!DataBase.getInstance().dump(this.articles, this.table)) return;  //stop the dumping process
                articles.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                this.siteURL = originalURL;
            }
        }
    }

    private void probeForLastItem(){
        /**
            This method will search the actual site for the last article registered on the database, when it finds
         it will make LAST_SCRAPPED_PAGE equals to the page of the last entry of the database.

            If the method doesn't find the record across 10 pages it will look for the previous record and so on.

            When first scrapping a site comment the reference to this method and hardcode LAST_SCRAPPED_PAGE to the
         page that you want to start the scrapping.
         */
        if (DataBase.getInstance().isTestRun()){
            this.LAST_SCRAPPED_PAGE = 1;
            return;
        }
        //====================================== initializing variables ===========================================//
        String originalURL = siteURL, title, currentLine;
        String lastTitle = DataBase.getInstance().getLastArticleTitle(false, -1, table);
        int safetyOffset = 0;
        boolean matchFound = false;
        Document document = null;
//        BufferedReader bf;
        Matcher titleMatcher;
        //====================================== initializing variables ===========================================//
        for (int i = 1; !matchFound; i++){
            changeURL(originalURL + i);
            try {
                document = Jsoup.connect(siteURL).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert document != null;
            try(BufferedReader bf = new BufferedReader(new StringReader(document.outerHtml()))){
                boolean onContentPane = false; //for tracking when the bf is inside the pane that holds the articles
                while (true) {
                    currentLine = bf.readLine();
                    if (currentLine == null) break;
                    titleMatcher = this.titlePattern.matcher(currentLine);

                    if (currentLine.trim().equals(startTag)) onContentPane = true; //start of article
                    if (currentLine.trim().equals(endTag)) onContentPane = false; //end of article

                    if (titleMatcher.find() && onContentPane) {
                        //finds the title and saves it to the String title
                        title = titleMatcher.group(1);
                        if (title.trim().equals(lastTitle.trim())){
                            //last record on database
                            System.out.println("found match");
                            this.LAST_SCRAPPED_PAGE = i;
                            matchFound = true;
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (i > 10){
                //safety barrier against title updating on the site that would make the code loop infinitely
                safetyOffset++;
                i = 0;
                lastTitle = DataBase.getInstance().getLastArticleTitle(true, safetyOffset, table);
                System.out.println("switched last title to " + lastTitle);
            }
            this.siteURL = originalURL;
        }

    }

    @Override
    public void close() {
        this.driver.close();
    }

    private void changeURL(String url) {
        this.siteURL = url;
    }
}

