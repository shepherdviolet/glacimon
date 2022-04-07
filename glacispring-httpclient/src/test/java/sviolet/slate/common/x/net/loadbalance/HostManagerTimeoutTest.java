package sviolet.slate.common.x.net.loadbalance;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * LoadBalancedHostManager测试案例
 * 1.配置固定不变
 * 2.模拟网络请求, 并在一定时间后发生超时(超时后阻断)
 *
 * 测试要点:
 * 1.无需后端
 * 2.观察当状态变为bad时, 后端2是否有效阻断(5秒内), 且后续每隔一段时间只有一次重新尝试(即不会一直有大量的错误尝试)
 * 3.观察当状态变为ok时, 后端2是否迅速恢复(5秒内)
 */
public class HostManagerTimeoutTest {

    static final int HOST_NUM = 4;
    static boolean bad = false;

    private static AtomicInteger host1Count = new AtomicInteger(0);
    private static AtomicInteger host2Count = new AtomicInteger(0);
    private static AtomicInteger badCount = new AtomicInteger(0);

    public static void main(String[] args) {

        final LoadBalancedHostManager manager = new LoadBalancedHostManager();
        manager.setHosts("1,2");

        for (int i = 0; i < 100 ; i++) {
            final int id = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    long startTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - startTime < 60 * 1000L) {

                        LoadBalancedHostManager.Host host = manager.nextHost();
                        String url = host.getUrl();

                        if ("1".equals(url)) {
                            host1Count.incrementAndGet();
                        } else {
                            host2Count.incrementAndGet();
                        }

                        if ("2".equals(url) && bad) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                            }
                            host.feedback(false, 5000, 4);
                            badCount.incrementAndGet();
                        } else {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                            }
                            host.feedback(true, 5000, 4);
                        }

                    }
                }
            }).start();
        }

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 65 * 1000L) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            System.out.println("1:" + host1Count.get() + ", 2:" + host2Count + ", bad:" + badCount.get());

            if (System.currentTimeMillis() - startTime < 2 * 1000L) {
                bad = false;
                System.out.println("good");
            } else if (System.currentTimeMillis() - startTime < 30 * 1000L) {
                bad = true;
                System.out.println("bad");
            } else {
                bad = false;
                System.out.println("good");
            }
        }

    }


}
