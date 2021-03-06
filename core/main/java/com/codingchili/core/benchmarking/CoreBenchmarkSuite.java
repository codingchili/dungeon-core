package com.codingchili.core.benchmarking;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.codingchili.core.benchmarking.reporting.BenchmarkConsoleReport;
import com.codingchili.core.benchmarking.reporting.BenchmarkHTMLReport;
import com.codingchili.core.context.*;
import com.codingchili.core.storage.*;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Contains system benchmarks..
 */
public class CoreBenchmarkSuite {
    private static final String MAP_BENCHMARKS = "Map benchmarks";
    private int iterations = 15;

    /**
     * Creates a clustered vertx instance on which all registered benchmarks will run.
     *
     * @param promise   callback on completion
     * @param executor executor to invoke this as a command.
     */
    public Void execute(Promise<CommandResult> promise, CommandExecutor executor) {
        executor.getProperty(PARAM_ITERATIONS).ifPresent(iterations ->
                this.iterations = Integer.parseInt(iterations));

        SystemContext.clustered(cluster -> {
            maps(cluster.result(), new BenchmarkConsoleListener()).onComplete(done -> {
                if (done.succeeded()) {
                    createReport(promise, done.result(), executor);
                } else {
                    promise.fail(done.cause());
                }
                cluster.result().close();
            });
        });
        return null;
    }

    private void createReport(Promise<CommandResult> future, List<BenchmarkGroup> result, CommandExecutor executor) {
        Optional<String> template = executor.getProperty(PARAM_TEMPLATE);
        BenchmarkReport report;

        if (executor.hasProperty(PARAM_HTML)) {
            report = new BenchmarkHTMLReport(result);
        } else {
            report = new BenchmarkConsoleReport(result);
        }
        template.ifPresent(report::template);
        report.display();
        future.complete(LauncherCommandResult.SHUTDOWN);
    }

    /**
     * Runs all core map benchmarks.
     *
     * @param context  the core context to run benchmark on
     * @param listener benchmark listener to use
     * @return a future that is completed with the results of the benchmark.
     */
    public Future<List<BenchmarkGroup>> maps(CoreContext context, BenchmarkListener listener) {
        Promise<List<BenchmarkGroup>> promise = Promise.promise();
        BenchmarkGroup group = new BenchmarkGroupBuilder(MAP_BENCHMARKS, iterations);

        Consumer<Class<? extends AsyncStorage>> add = (clazz) -> {
            group.add(new MapBenchmarkImplementation(group, clazz, clazz.getSimpleName()));
        };

        add.accept(JsonMap.class);
        add.accept(PrivateMap.class);
        add.accept(SharedMap.class);
        //add.accept(IndexedMapPersisted.class);
        add.accept(IndexedMapVolatile.class);
        add.accept(HazelMap.class);
        /*add.accept(ElasticMap.class); requires external servers.
        add.accept(MongoDBMap.class);*/

        new BenchmarkExecutor(context)
                .setListener(listener)
                .start(group)
                .onComplete(promise);

        return promise.future();
    }

    /**
     * Set the number of iterations to perform.
     *
     * @param iterations iterations to perform
     * @return fluent
     */
    public CoreBenchmarkSuite setIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }
}
