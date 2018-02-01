package study.wzp.distributed.lock.database;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class JdbcInstance {

    private static JdbcConfig config;

    /**
     * 静态内部类实现单例
     */
    private static class JdbcInstanceHelper {
        static JdbcInstance instance = new JdbcInstance();
    }

    private JdbcInstance() {
        init();
    }

    // 初始化驱动和加载配置文件
    private void init() {
        initConfig();
        initDriver();
    }

    // 初始化配置
    private void initConfig() {
        config = new JdbcConfig("/jdbc.properties");
    }

    // 加载驱动
    private void initDriver() {
        try {
            Class.forName(config.driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static JdbcInstance getInstance() {
        return JdbcInstanceHelper.instance;
    }


    private class JdbcConfig {

        String driver;

        String url;

        String username;

        String password;

        String propFilePath;

        public JdbcConfig(String propFilePath) {
            this.propFilePath = propFilePath;
            parseConfig();
        }

        public JdbcConfig(String driver, String url, String username, String password) {
            this.driver = driver;
            this.url = url;
            this.username = username;
            this.password = password;
        }

        private void parseConfig() {
            Properties properties = new Properties();
            try {
                properties.load(this.getClass().getResourceAsStream(this.propFilePath));
                this.driver = properties.getProperty("db.driver");
                this.url = properties.getProperty("db.url");
                this.username = properties.getProperty("db.username");
                this.password = properties.getProperty("db.password");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public Connection createConn() {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(config.url, config.username, config.password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }

    public PreparedStatement createStmt(String sql, Connection conn) {
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stmt;
    }

    public ResultSet getResultSet(PreparedStatement stmt) {
        ResultSet rs = null;

        try {
            rs = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rs;
    }

    // 关闭容器
    public void close(AutoCloseable... closeables) {

        try {
            for (AutoCloseable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    interface JdbcQueryCallback<T> {

        public T doQuery(ResultSet rs) throws SQLException;

    }

    interface JdbcUpdateCallback<T> {
        public T doUpdate(PreparedStatement pstmt) throws SQLException;
    }

    /**
     * 执行操作模版
     */
    public <T> T executeQuery(String sql, JdbcQueryCallback<T> callback, Object... args) {
        T t = null;
        Connection conn = this.createConn();
        PreparedStatement stmt = this.createStmt(sql, conn);
        ResultSet rs = null;
        try {
            conn.setAutoCommit(false);
            setParameters(stmt, args);
            rs = stmt.executeQuery();
            t = callback.doQuery(rs);
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            this.close(rs, stmt, conn);
        }
        return t;
    }

    /**
     * 执行操作模版
     */
    public <T> T executeUpdate(String sql, JdbcUpdateCallback<T> callback, Object... args) {
        T t = null;
        Connection conn = this.createConn();
        PreparedStatement stmt = this.createStmt(sql, conn);
        try {
            conn.setAutoCommit(false);
            setParameters(stmt, args);
            t = callback.doUpdate(stmt);
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            this.close(stmt, conn);
        }
        return t;
    }



    private void setParameters(PreparedStatement stmt, Object... args) {
        try{
            int i = 1;
            for (Object arg : args) {
                stmt.setObject(i, arg);
                i ++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }



}
