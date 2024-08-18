/*
 * Copyright (c) 2020-2023. Bernard Bou
 */
package org.treebolic.services

import java.util.concurrent.BlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.FutureTask
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object TaskRunner {

    // E X E C U T O R


    private const val CORE_POOL_SIZE = 5

    private const val MAXIMUM_POOL_SIZE = 128

    private const val KEEP_ALIVE = 1

    private val POOL_WORK_QUEUE: BlockingQueue<Runnable> = LinkedBlockingQueue(10)

    private val THREAD_POOL_EXECUTOR: Executor = ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE.toLong(), TimeUnit.SECONDS, POOL_WORK_QUEUE)

    private val EXECUTOR = THREAD_POOL_EXECUTOR

    private fun <Result> makeFuture(callable: Callable<Result>, callback: (Result) -> Unit): FutureTask<Result> {
        return object : FutureTask<Result>(callable) {
            override fun done() {
                try {
                    val result = get()
                    callback.invoke(result)
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // E X E C U T E


    @JvmStatic
    fun <Result> execute(callable: Callable<Result>, callback: (Result) -> Unit) {
        val future = makeFuture(callable, callback)
        EXECUTOR.execute(future)
    }
}
