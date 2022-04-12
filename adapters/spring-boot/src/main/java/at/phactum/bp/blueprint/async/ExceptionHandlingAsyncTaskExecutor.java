package at.phactum.bp.blueprint.async;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * This executor wraps the task executor to log exceptions thrown during task execution.
 */
@ManagedResource
public class ExceptionHandlingAsyncTaskExecutor extends JmxExposedThreadPoolTaskExecutor implements AsyncTaskExecutor, InitializingBean, DisposableBean {

	private static final long serialVersionUID = 1L;

	static final String EXCEPTION_MESSAGE = "Caught async exception";

	private final Logger log = LoggerFactory.getLogger(ExceptionHandlingAsyncTaskExecutor.class);

	@Override
	public void execute(Runnable task) {
		super.execute(createWrappedRunnable(task));
	}

	@Override
	public void execute(Runnable task, long startTimeout) {
		super.execute(createWrappedRunnable(task), startTimeout);
	}

	private <T> Callable<T> createCallable(final Callable<T> task) {
		return () -> {
			try {
				return task.call();
			} catch (Exception e) {
				handle(e);
				throw e;
			}
		};
	}

	private Runnable createWrappedRunnable(final Runnable task) {
		return () -> {
			try {
				task.run();
			} catch (Exception e) {
				handle(e);
			}
		};
	}

	protected void handle(Exception e) {
		log.error(EXCEPTION_MESSAGE, e);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return super.submit(createWrappedRunnable(task));
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return super.submit(createCallable(task));
	}

}
