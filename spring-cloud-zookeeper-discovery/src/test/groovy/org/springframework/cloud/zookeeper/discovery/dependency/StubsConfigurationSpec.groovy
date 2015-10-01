package org.springframework.cloud.zookeeper.discovery.dependency
import spock.lang.Specification
import spock.lang.Unroll

class StubsConfigurationSpec extends Specification {

	@Unroll
	def "should return empty colon separated dependency notation if empty or invalid path [#path] has been provided"() {
		when:
			StubsConfiguration stubsConfiguration = new StubsConfiguration(path)
		then:
			'' == stubsConfiguration.toColonSeparatedDependencyNotation()
		where:
			path << ['', 'pl/']
	}

	def "should properly parse invalid colon separated path into empty notation"() {
		given:
			String path = 'pl/a'
		when:
			StubsConfiguration stubsConfiguration = new StubsConfiguration(path)
		then:
			'' == stubsConfiguration.toColonSeparatedDependencyNotation()
			'' == stubsConfiguration.stubsGroupId
			'' == stubsConfiguration.stubsArtifactId
			'' == stubsConfiguration.stubsClassifier
	}

	def "should parse the path into group, artifact and classifier"() {
		given:
			String path = 'pl/a'
		when:
			StubsConfiguration stubsConfiguration = new StubsConfiguration(new StubsConfiguration.DependencyPath(path))
		then:
			'pl:a:stubs' == stubsConfiguration.toColonSeparatedDependencyNotation()
			'pl' == stubsConfiguration.stubsGroupId
			'a' == stubsConfiguration.stubsArtifactId
			'stubs' == stubsConfiguration.stubsClassifier
	}

	def "should properly set group, artifact and classifier"() {
		when:
			StubsConfiguration stubsConfiguration = new StubsConfiguration('pl', 'a', 'superstubs')
		then:
			'pl:a:superstubs' == stubsConfiguration.toColonSeparatedDependencyNotation()
			'pl' == stubsConfiguration.stubsGroupId
			'a' == stubsConfiguration.stubsArtifactId
			'superstubs' == stubsConfiguration.stubsClassifier
	}
}
