package com.codingchili.core.Files;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Context.Delay;
import com.codingchili.core.Testing.ContextMock;


/**
 * @author Robin Duda
 *
 * Tests that the FileWatcher emits modified/delete events when a file is
 * created/deleted/modified and that it ignores files outside the specified directory.
 */
@RunWith(VertxUnitRunner.class)
public class FileWatcherIT {
    private static final String FILE_WATCHER_TEST = "FileWatcher";
    private static final String TOUCH_JSON = Strings.testFile(FILE_WATCHER_TEST, "touch.json");
    private static final String NOT_WATCHED_FILE = Strings.testFile("", "touch.json");
    private ContextMock context;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        this.context = new ContextMock(Vertx.vertx());
        Delay.initialize(context);
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void getNotifiedOnModify(TestContext test) {
        Async async = test.async();

        JsonFileStore.writeObject(new JsonObject(), TOUCH_JSON);

        listenFiles(new FileStoreListener() {
                    @Override
                    public void onFileModify(Path path) {
                        async.complete();
                    }
                });

        JsonFileStore.writeObject(new JsonObject(), TOUCH_JSON);
    }

    @Test
    public void getNotifiedOnDelete(TestContext test) {
        Async async = test.async();

        listenFiles(new FileStoreListener() {
            @Override
            public void onFileRemove(Path path) {
                async.complete();
            }
        });

        JsonFileStore.writeObject(new JsonObject(), TOUCH_JSON);

        context.timer(400, event -> {
            test.assertTrue(JsonFileStore.deleteObject(TOUCH_JSON));
        });
    }

    @Test
    public void getNotifiedOnCreate(TestContext test) {
        Async async = test.async();

        JsonFileStore.writeObject(new JsonObject(), TOUCH_JSON);

        listenFiles(new FileStoreListener() {
            @Override
            public void onFileModify(Path path) {
                async.complete();
            }
        });

        JsonFileStore.writeObject(new JsonObject(), TOUCH_JSON);
    }

    @Test
    public void notNotifiedWhenOutsidePath(TestContext test) {
        Async async = test.async();

        listenFiles(new FileStoreListener() {
            @Override
            public void onFileModify(Path path) {
                test.fail("Reacted to file modify event outside watched path.");
            }

            @Override
            public void onFileRemove(Path path) {
                test.fail("Reacted to file delete event outside the watched path.");
            }
        });

        JsonFileStore.writeObject(new JsonObject(), NOT_WATCHED_FILE);
        test.assertTrue(JsonFileStore.deleteObject(NOT_WATCHED_FILE));

        Delay.forMS(async, 400);
    }

    private void listenFiles(FileStoreListener listener) {
        new FileWatcherBuilder(context)
                .onDirectory(Strings.testDirectory(FILE_WATCHER_TEST))
                .rate(() -> 25)
                .withListener(listener)
                .build();
    }
}