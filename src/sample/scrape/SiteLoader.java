package sample.scrape;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import sample.db.Tables;

import java.util.ArrayList;
import java.util.List;

public final class SiteLoader {
    private static boolean finished = false;
    private static SimpleStringProperty State;

    public static void load() {
        new Thread(() -> {
            //Thread to scrape the sites
            System.out.println("loading site loader");
            State = new SimpleStringProperty();
            State.set("...");

            SiteTemplate scinews = new SiteTemplate(
                    "sciNews",
                    "http://www.sci-news.com/news/biology/page/",
                    Tables.scinews,
                    "<div class=\"post-wrapper-archive\">",
                    "<div id=\"pag\">",
                    "<h2><a href=\".+?\" rel=\"bookmark\"> (.+?) </a></h2>",
                    "<h2><a href=\"(.+?)\" rel=\"bookmark\"> .+? </a></h2>",
                    "<a href=\".+?\" rel=\"bookmark\"> <img width=\"\\d+?\" height=\"\\d+?\" src=\"(.+?)\" class=\".+?\" alt=\".+?\">\\s*?</a>",
                    "195",
                    "110"
            );
            scinews.load();

            SiteTemplate science_news = new SiteTemplate(
                    "scienceNews",
                    "https://www.sciencenews.org/topic/physics/page/",
                    Tables.science_news,
                    "<div class=\"river-with-sidebar__content___1yzT3\">",
                    "<nav class=\"pagination__wrapper___2qtdg\".+",
                    "<h3 class=\"post-item-river__title___J3spU\"> <a href=\".+?\">(.+?)</a> </h3>",
                    "<h3 class=\"post-item-river__title___J3spU\"> <a href=\"(.+?)\">",
                    "<img width=\".+?\" height=\".+?\" src=\"(.+?)\" class=\".+?\"",
                    "150",
                    "85"
                    );
            science_news.load();

            SiteTemplate universeToday = new SiteTemplate(
                    "universeToday",
                    "https://www.universetoday.com/page/",
                    Tables.universe_today,
                    "<main id=\"main\" class=\"site-main\" role=\"main\">",
                    "</main>",
                    " <h3 class=\"entry-title\"><a href=.+? rel=\"bookmark\">(.+?)</a>",
                    "<h3 class=\"entry-title\"><a href=\"(.+?)\"",
                    "<div class=\"post-thumbnail\">.+?<img width=.+? height=.+? src=\"(.+?)\"",
                    "580",
                    "326"
            );
            universeToday.load();

            SiteTemplate spaceFlightNow = new SiteTemplate(
                    "spaceFlightNow",
                    "https://spaceflightnow.com/category/news-archive/page/",
                    Tables.space_flight_now,
                    "<div id=\"main-content\" class=\"mh-loop mh-content\">",
                    "<div class=\"mh-footer-mobile-nav\">",
                    "<h3 class=\"entry-title mh-loop-title\"> <a href=.+?>(.+?)</a> </h3>",
                    "<h3 class=\"entry-title mh-loop-title\"> <a href=\"(.+?)\"",
                    "<img width=\"326\" height=\"245\" src=\"(https:.+?)\"",
                    "326",
                    "245"
            );
            spaceFlightNow.load();
                finished = true;
        }).start();
    }


    public static boolean isFinished() {
        return finished;
    }

    public static SimpleStringProperty stateProperty() {
        return State;
    }

    private static final class SiteTemplate {
        private final String stateName;
        private final String link;

        private final String startTag;
        private final String endTag;
        private final String titleRegex;
        private final String linkRegex;
        private final String photoRegex;
        private final Tables table;

        private final String windowWidth;
        private final String windowHeight;

        private boolean siteFinished;
        SiteTemplate(String stateName, String link, Tables table, String startTag,
                     String endTag, String titleRegex, String linkRegex, String photoRegex,
                     String windowWidth, String windowHeight) {
            this.stateName = stateName;
            this.link = link;
            this.startTag = startTag;
            this.endTag = endTag;
            this.titleRegex = titleRegex;
            this.linkRegex = linkRegex;
            this.photoRegex = photoRegex;
            this.table = table;
            this.windowWidth = windowWidth;
            this.windowHeight = windowHeight;
            this.siteFinished = false;
        }

        private void load() {
            Platform.runLater(()-> State.set(this.stateName));

                try (WebScraper scrapper = new WebScraper(
                        this.link,
                        this.startTag,
                        this.endTag,
                        this.titleRegex,
                        this.linkRegex,
                        this.photoRegex,
                        this.table,
                        this.windowWidth,
                        this.windowHeight
                )) {
                    scrapper.scrape();
                    this.siteFinished = true;
                }
        }
        private boolean isSiteFinished(){   return siteFinished;}
    }
}
