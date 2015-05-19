package org.springframework.cloud.zookeeper.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.core.env.EnumerablePropertySource;

/**
 * @author Spencer Gibb
 */
public class ZookeeperPropertySource extends EnumerablePropertySource<CuratorFramework>
		implements Lifecycle {
	private static final Logger LOG = LoggerFactory
			.getLogger(ZookeeperPropertySource.class);

	private String context;

	private TreeCache cache;
	private boolean running;

	public ZookeeperPropertySource(String context, CuratorFramework source) {
		super(context, source);
		this.context = context;

		if (!this.context.startsWith("/")) {
			this.context = "/" + this.context;
		}
	}

	@Override
	public void start() {
		try {
			cache = TreeCache.newBuilder(source, context).build();
			cache.start();
			running = true;
            /*
                TODO: race condition since TreeCache.process(..) is invoked asynchronously.
                Methods getProperty and getPropertyNames could be invoked before that TreeCache.process(..) receives
                all the WatchedEvents.
            */
		}
		catch (NoNodeException e) {
			// no node, ignore
		}
		catch (Exception e) {
			LOG.error("Error initializing ZookeperPropertySource", e);
		}
	}

	@Override
	public Object getProperty(String name) {
		String fullPath = context + "/" + name.replace(".", "/");
		ChildData data = cache.getCurrentData(fullPath);
		if (data == null)
			return null;
		return new String(data.getData(), Charset.forName("UTF-8"));
	}

	@Override
	public String[] getPropertyNames() {
		List<String> keys = new ArrayList<>();
		findKeys(keys, context);
		return keys.toArray(new String[0]);
	}

	protected void findKeys(List<String> keys, String path) {
		Map<String, ChildData> children = cache.getCurrentChildren(path);

		if (children == null)
			return;
		for (Map.Entry<String, ChildData> entry : children.entrySet()) {
			ChildData child = entry.getValue();
			if (child.getData()==null || child.getData().length == 0) {
				findKeys(keys, child.getPath());
			}
			else {
				keys.add(child.getPath().replace(context + "/", "").replace('/', '.'));
			}
		}
	}

	@Override
	public void stop() {
		cache.close();
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

}
