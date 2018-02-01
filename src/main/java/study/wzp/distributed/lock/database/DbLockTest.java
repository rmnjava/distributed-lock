package study.wzp.distributed.lock.database;

import org.junit.Test;
import study.wzp.distributed.lock.pojos.User;

import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DbLockTest {

    ExecutorService service = Executors.newFixedThreadPool(200);

    UserDao userDao = new UserDao();


    /**
     * 测试不使用lock积分增减，会出现并发问题
     */
    @Test
    public void testAddScoreNotUseLock() {

        // 先查询一下积分
        User u1 = userDao.getUser(1);

        // 10个线程➕积分，10个线程减积分；最终应该积分不变
        for(int i = 1; i <= 100; i ++) {
            if(i % 2 == 0) { // +积分
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        userDao.incrScore();
                    }
                });
            }else { // -积分
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        userDao.descScore();
                    }
                });
            }

        }

        service.shutdown();
        while(!service.isTerminated()) {
            try {
                service.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        User u2 = userDao.getUser(1);

        assert u1.getScore() == u2.getScore();

    }

    /**
     * 使用for update lock
     *
     * 注意，测试时，必须确保conn.setAutoCommit(false)不然每次执行SQL都会提交，那么就无能做到
     * lock的效果，也就是最终加减结果不是预期
     */
    @Test
    public void testAddScoreUseLock() {
        // 先查询一下积分
        User u1 = userDao.getUser(1);


        // 10个线程➕积分，10个线程减积分；最终应该积分不变
        int j = 0,k = 0;
        for(int i = 1; i <= 20; i ++) {
            if(i % 2 == 0) { // +积分
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        userDao.incrScoreForUpdate();
                    }
                });
                j ++;
            }else { // -积分
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        userDao.descScoreForUpdate();
                    }
                });
                k ++;
            }

        }

        System.out.println(j + "---" + k);
        service.shutdown();
        while(!service.isTerminated()) {
            try {
                service.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        User u2 = userDao.getUser(1);
        assert u1.getScore() == u2.getScore();
    }



}
