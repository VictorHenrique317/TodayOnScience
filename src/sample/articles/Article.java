package sample.articles;

import java.util.Objects;

public final class Article implements Comparable {
    private int id;

    private final String title;
    private final String hyperlink;
    private final String photoURL;
    private final byte[] photoBytes;

    public Article(String title, String hyperlink, String photoURL, byte[] bytes) {
        this.title = title;
        this.hyperlink = hyperlink;
        this.photoURL = photoURL;
        this.photoBytes = bytes;
    }

    public Article(int id, String title, String hyperlink, byte[] bytes) {
        this.id = id;
        this.title = title;
        this.hyperlink = hyperlink;
        this.photoURL = null;
        this.photoBytes = bytes;
    }

    public Article(String title, String hyperlink, byte[] bytes) {
        this.title = title;
        this.hyperlink = hyperlink;
        this.photoURL = null;
        this.photoBytes = bytes;
    }

    public String getTitle() {
        return title;
    }

    public String getHyperlink() {
        return hyperlink;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public byte[] getPhotoBytes() {
        return photoBytes.clone();
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return title.equals(article.title) &&
                hyperlink.equals(article.hyperlink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, hyperlink);
    }

    @Override
    public int compareTo(Object o) {
        Integer id = ((Article) o).id;
        return ((Integer)this.id).compareTo(id);
    }
}
