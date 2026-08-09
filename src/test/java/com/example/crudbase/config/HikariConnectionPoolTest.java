package com.example.crudbase.config;

import com.example.crudbase.repository.ResultRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
class HikariConnectionPoolTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResultRepository resultRepository;

    @Test
    void shouldConfigureHikariDataSourceWithTheProjectDefaults() {
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);
        HikariDataSource hikari = (HikariDataSource) dataSource;

        assertThat(hikari.getPoolName()).isEqualTo("CrudBaseHikariPool");
        assertThat(hikari.getMaximumPoolSize()).isEqualTo(10);
        assertThat(hikari.getMinimumIdle()).isEqualTo(5);
        assertThat(hikari.getConnectionTimeout()).isEqualTo(20000);
        assertThat(hikari.getIdleTimeout()).isEqualTo(300000);
        assertThat(hikari.getMaxLifetime()).isEqualTo(1200000);
        assertThat(hikari.getLeakDetectionThreshold()).isEqualTo(30000);
    }

    @Test
    void shouldAcquireAndReleaseAConnectionSuccessfully() {
        assertThatCode(() -> {
            try (var connection = dataSource.getConnection()) {
                assertThat(connection.isValid(2)).isTrue();
            }
        }).doesNotThrowAnyException();
    }

    @Test
    void shouldServeMoreConcurrentCallersThanThePoolSizeWithoutConnectionErrors() throws InterruptedException {
        int maximumPoolSize = ((HikariDataSource) dataSource).getMaximumPoolSize();
        int concurrentCallers = maximumPoolSize * 3;

        ExecutorService executor = Executors.newFixedThreadPool(concurrentCallers);
        CountDownLatch allThreadsReady = new CountDownLatch(concurrentCallers);
        CountDownLatch startSignal = new CountDownLatch(1);

        Callable<Long> countResults = () -> {
            allThreadsReady.countDown();
            startSignal.await();
            return resultRepository.count();
        };

        List<Future<Long>> futures = new ArrayList<>();
        for (int i = 0; i < concurrentCallers; i++) {
            futures.add(executor.submit(countResults));
        }

        allThreadsReady.await(5, TimeUnit.SECONDS);
        startSignal.countDown();

        for (Future<Long> future : futures) {
            assertThatCode(() -> future.get(15, TimeUnit.SECONDS)).doesNotThrowAnyException();
        }
        executor.shutdown();
    }
}
