package at.phactum.bp.blueprint.async;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@ManagedResource
public class JmxExposedThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

	private static final long serialVersionUID = 1L;

	@ManagedAttribute
	@Override
	public int getCorePoolSize() {
		return super.getCorePoolSize();
	}

	@ManagedAttribute
	@Override
	public int getMaxPoolSize() {
		return super.getMaxPoolSize();
	}

	@ManagedAttribute
	@Override
	public int getKeepAliveSeconds() {
		return super.getKeepAliveSeconds();
	}

	@ManagedAttribute
	@Override
	public int getPoolSize() {
		return super.getPoolSize();
	}

	@ManagedAttribute
	@Override
	public int getActiveCount() {
		return super.getActiveCount();
	}
	
	@ManagedAttribute
	public String getThreadGroupName() {
		return super.getThreadGroup().getName();
	}

}
