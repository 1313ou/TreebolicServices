/*
 * Copyright (c) 2020. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.services;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskRunner
{
	// E X E C U T O R

	private static final int CORE_POOL_SIZE = 5;

	private static final int MAXIMUM_POOL_SIZE = 128;

	private static final int KEEP_ALIVE = 1;

	private static final BlockingQueue<Runnable> POOL_WORK_QUEUE = new LinkedBlockingQueue<>(10);

	public static final Executor THREAD_POOL_EXECUTOR;

	static
	{
		THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, POOL_WORK_QUEUE);
	}

	private static final Executor EXECUTOR = THREAD_POOL_EXECUTOR;

	// F U T U R E

	interface Callback<Result>
	{
		void call(Result param);
	}

	private static <Result> FutureTask<Result> makeFuture(Callable<Result> callable, Callback<Result> callback)
	{
		return new FutureTask<Result>(callable)
		{
			@Override
			protected void done()
			{
				try
				{
					Result result = get();
					callback.call(result);
				}
				catch (ExecutionException e)
				{
					e.printStackTrace();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		};
	}

	// E X E C U T E

	public static <Result> void execute(Callable<Result> callable, Callback<Result> callback)
	{
		final FutureTask<Result> future = makeFuture(callable, callback);
		EXECUTOR.execute(future);
	}
}
