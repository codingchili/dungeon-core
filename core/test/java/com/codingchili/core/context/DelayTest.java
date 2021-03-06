package com.codingchili.core.context;

import io.vertx.core.Promise;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.exception.SystemNotInitializedException;
import com.codingchili.core.testing.ContextMock;

/**
 * Verifies that the STARTUP_DELAY system is working, is required for some tests.
 */
@RunWith(VertxUnitRunner.class)
public class DelayTest {
    private static CoreContext context;

    @BeforeClass
    public static void setUp() {
        context = new ContextMock();
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }
    @Test
    public void testDelayFuture(TestContext test) {
        Async async = test.async();
        Promise<Void> promise = Promise.promise();
        promise.future().onComplete(result -> async.complete());
        Delay.forMS(promise, 1);
    }

    @Test
    public void testDelayNotInitialized(TestContext test) {
        try {
            Delay.forMS(Promise.promise(), 1);
        } catch (SystemNotInitializedException e) {
            test.assertTrue(e.getMessage().contains(Delay.class.getSimpleName()));
        }
    }
}
