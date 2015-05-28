/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.zookeeper.discovery.dependency;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.Arrays;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

/**
 * @author Marcin Grzejszczak, 4financeIT
 */
public enum LoadBalancerType {
	STICKY, RANDOM, ROUND_ROBIN;

	public static LoadBalancerType fromName(final String strategyName) {
		LoadBalancerType loadBalancerType = (LoadBalancerType) CollectionUtils.find(Arrays.asList(values()), new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				LoadBalancerType input = (LoadBalancerType) o;
				return input.name().equals(defaultIfEmpty(strategyName, EMPTY).toUpperCase());
			}
		});
		if (loadBalancerType == null) {
			return ROUND_ROBIN;
		}
		return loadBalancerType;
	}

}
