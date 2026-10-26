import net.ltgt.gradle.errorprone.errorprone

plugins {
	java
	id("org.springframework.boot") version "4.1.2"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.diffplug.spotless") version "8.10.4"
	id("net.ltgt.errorprone") version "5.1.1"
	id("info.solidsoft.pitest") version "1.19.0"
}

group = "com.bancoexemplo"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
	errorprone("com.google.errorprone:error_prone_core:2.51.0")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-kafka")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	// Starts compose.yaml with bootRun; never packaged in the jar nor used by tests.
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-kafka")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("com.tngtech.archunit:archunit-junit5:1.5.1")
	testImplementation("org.mockito:mockito-junit-jupiter")
	mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
	// Loading Mockito as an agent at startup; attaching it later makes the JDK print a warning on every run.
	// The agent extends the boot classpath, which turns class data sharing off with a warning; turning it off here is silent.
	jvmArgs("-javaagent:${mockitoAgent.asPath}", "-Xshare:off")
}

spotless {
	java {
		palantirJavaFormat("2.102.0")
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	// A new compiler warning fails the build, so "no new warnings" needs no one to remember it.
	options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
	options.errorprone {
		// Comparing or building money the wrong way changes an amount without failing anywhere.
		error("BigDecimalEquals", "BigDecimalLiteralDouble")
	}
}

pitest {
	pitestVersion = "1.30.1"
	junit5PluginVersion = "1.2.3"
	// The plugin would add a JUnit 5 launcher, which breaks the JUnit 6 that Spring Boot brings.
	addJUnitPlatformLauncher = false
	// The mutation runs start their own JVMs, which do not inherit the test task's arguments.
	jvmArgs = listOf("-javaagent:${mockitoAgent.asPath}", "-Xshare:off")
	targetClasses = setOf("com.bancoexemplo.custody.*.domain.*", "com.bancoexemplo.custody.*.application.*")
	mutationThreshold = 80
	// Passes while no business module exists yet; the threshold applies once there is code to mutate.
	failWhenNoMutations = false
	timestampedReports = false
	outputFormats = setOf("HTML", "XML")
}
