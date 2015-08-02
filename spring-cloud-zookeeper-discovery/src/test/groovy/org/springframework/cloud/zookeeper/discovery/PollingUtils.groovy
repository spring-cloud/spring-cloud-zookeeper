package org.springframework.cloud.zookeeper.discovery

trait PollingUtils {
	Closure willPass(Closure closure) {
		return {
			try {
				closure()
			} catch (Exception e) {
				throw new AssertionError("Exception occurred while evaluating closure", e)
			}
		}
	}
	
}
