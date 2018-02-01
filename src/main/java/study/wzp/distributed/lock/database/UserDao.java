package study.wzp.distributed.lock.database;

import study.wzp.distributed.lock.pojos.User;

import java.sql.ResultSet;

public class UserDao {

    private static JdbcInstance instance = JdbcInstance.getInstance();

    public User getUser(int id) {
        return instance.executeQuery("SELECT * FROM user WHERE id = ?", (rs)-> {
            User user = null;
            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt(1));
                user.setName(rs.getString(2));
                user.setScore(rs.getDouble(3));
            }
            return user;
        }, id);
    }

    public void incrScore() {
        instance.executeUpdate("SELECT * FROM user WHERE id = 1", (stmt) -> {

            ResultSet rs = stmt.executeQuery();
            if (rs == null || !rs.next()){
                return null;
            }

            double score = rs.getDouble(3);
            stmt.executeUpdate("update user set score=" + (score + 1) + " where id = 1");
            return null;
        });
    }

    public void descScore() {
        instance.executeUpdate("SELECT * FROM user WHERE id = 1", (stmt) -> {

            ResultSet rs = stmt.executeQuery();
            if (rs == null || !rs.next()){
                return null;
            }

            double score = rs.getDouble(3);
            stmt.executeUpdate("update user set score=" + (score - 1) + " where id = 1");

            return null;
        });
    }

    public void incrScoreForUpdate() {
        instance.executeUpdate("SELECT * FROM user WHERE id = 1 FOR UPDATE", (stmt) -> {
            ResultSet rs = stmt.executeQuery();
            if (rs == null || !rs.next()){
                return null;
            }

            double score = rs.getDouble(3);
            stmt.executeUpdate("update user set score=" + (score + 1) + " where id = 1");
            System.out.println(Thread.currentThread() +"score:" + score + ": +update user set score=" + (score + 1) + " where id = 1");

            return null;
        });
    }

    public void descScoreForUpdate() {
        instance.executeUpdate("SELECT * FROM user WHERE id = 1 FOR UPDATE", (stmt) -> {

            ResultSet rs = stmt.executeQuery();
            if (rs == null || !rs.next()){
                return null;
            }

            double score = rs.getDouble(3);
            stmt.executeUpdate("update user set score=" + (score - 1) + " where id = 1");
            System.out.println(Thread.currentThread() +"score:" + score + ": -update user set score=" + (score - 1) + " where id = 1");

            return null;
        });
    }

}
