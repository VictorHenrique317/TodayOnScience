package sample.utils;

public final class Commands {
    public static final String INSERT = "INSERT INTO ";
    public static final String VALUES = " (title, link, bytes) VALUES (?, ?, ?)";
    public static final String QUERY_MAX_ID = "SELECT MAX(id) FROM ";
    public static final String SELECT_EVERYTHING = "SELECT title, link, bytes FROM ";
    public static final String SELECT_STAR = "SELECT * FROM ";
    public static final String WHERE_ID = " WHERE id=?";
    public static final String SELECT_TITLE = "SELECT title FROM ";
    public static final String SELECT_COUNT = "SELECT count() FROM ";
    public static final String WHERE_TITLE = " WHERE title=?";
    public static final String WHERE_TITLE_LIKE = " WHERE title LIKE ?";
    public static final String WHERE_ID_MAX_ID = " WHERE id= (SELECT MAX(id) FROM ";
    public static final String DROP_VIEW_IF_EXISTS = "DROP VIEW IF EXISTS ";
    public static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";
    public static final String CREATE_VIEW = "CREATE VIEW ";
    public static final String CREATE_TABLE = "CREATE TABLE ";
    public static final String AS = " AS ";
    public static final String ORDER_BY = " ORDER BY ";
    public static final String DELETE_FROM = "DELETE FROM ";

}
