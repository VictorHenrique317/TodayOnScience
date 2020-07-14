package sample;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;

public class Test {
    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("http://www.sci-news.com/news/biology/page/2").get();

        try(BufferedWriter out = new BufferedWriter(new FileWriter(new File("outerHTML.txt")))){
            BufferedReader bf = new BufferedReader(new StringReader(doc.outerHtml()));
            out.write(doc.outerHtml());

//            while (true){
//                String line = bf.readLine();
//                if(line == null) break;
//                System.out.println("writing " + line);
//                try {
//                    out.write(line);
//                }catch (UTFDataFormatException e){
//                    bf.close();
//                    break;
//                }
//            }
        }

    }
}

