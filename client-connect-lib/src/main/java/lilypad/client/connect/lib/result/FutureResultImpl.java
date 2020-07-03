package lilypad.client.connect.lib.result;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import lilypad.client.connect.api.request.RequestException;
import lilypad.client.connect.api.result.FutureResult;
import lilypad.client.connect.api.result.FutureResultListener;
import lilypad.client.connect.api.result.Result;

public class FutureResultImpl<T extends Result> implements FutureResult<T> {

	private final SettableFuture<T> future;
	private final Class<T> resultClass;

	public FutureResultImpl(Class<T> resultClass) {
		this.resultClass = resultClass;
		this.future = SettableFuture.create();
	}
	
	public void registerListener(final FutureResultListener<T> futureResultListener) {
		this.future.addListener(new Runnable() {
			@Override
			public void run() {
				try {
					futureResultListener.onResult(Futures.getDone(future));
				} catch (ExecutionException e) {
					// Swallow it, since FutureResultListener doesn't support getting exceptions. Shrug.
				}
			}
		}, MoreExecutors.directExecutor());
	}

	public T await() throws InterruptedException, RequestException {
		try {
			return this.future.get();
		} catch (ExecutionException e) {
			throw new RequestException(e);
		}
	}

	public T await(long timeout) throws InterruptedException, RequestException {
		try {
			return this.future.get(timeout, TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			throw new RequestException(e);
		} catch (TimeoutException e) {
			return null;
		}
	}

	public T awaitUninterruptibly() throws RequestException {
		try {
			return this.awaitUninterruptibly(Long.MAX_VALUE);
		} catch (TimeoutException e) {
			// no chance this happens
			throw new AssertionError(e);
		}
	}

	public T awaitUninterruptibly(long timeout) throws RequestException, TimeoutException {
		boolean hasBeenInterrupted = false;
		while (true) {
			try {
				T result = this.future.get(timeout, TimeUnit.MILLISECONDS);
				if (hasBeenInterrupted) {
					Thread.currentThread().interrupt();
				}
				return result;
			} catch (ExecutionException e) {
				throw new RequestException(e);
			} catch (InterruptedException e) {
				hasBeenInterrupted = true;
			}
		}
	}

	public void notifyResult(T result) {
		this.future.set(result);
	}

	public void notifyFailure(Throwable throwable) {
		this.future.setException(throwable);
	}
	
	public void cancel() {
		this.future.cancel(false);
	}
	
	public Class<T> getResultClass() {
		return this.resultClass;
	}

}
