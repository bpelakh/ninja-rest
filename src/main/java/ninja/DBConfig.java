package ninja;

/** Shared DB configuration. */
public final class DBConfig {
    /**
     * Connecting to local Postgres by default, modify through rmm.db.url property.
     */
    public final static String DB_URL = System.getProperty("rmm.db.url", "jdbc:postgresql://localhost/ninja");

    /**
     * DB username - override through rmm.db.user property.
     */
    public final static String DB_USER = System.getProperty("rmm.db.user", "boris");

    /**
     * DB password - override through rmm.db.password property.
     * In a real system, would use secure connection provided by container.
     */
    public final static String DB_PWD = System.getProperty("rmm.db.password", "password");

    /** Utility class. */
    private DBConfig() {}
}
