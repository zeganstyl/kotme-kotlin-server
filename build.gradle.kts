plugins {
    kotlin("jvm") version "1.5.21"
    kotlin("plugin.serialization") version "1.5.20"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val kotlinVersion = "1.5.21"
val ktorVersion = "1.6.2"
val exposedVersion = "0.32.1"

dependencies {
    implementation(project(":kotme-common"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")

    implementation("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion")

    implementation("com.h2database:h2:1.4.200")
    implementation("org.postgresql:postgresql:42.2.19")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jodatime:$exposedVersion")

    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-freemarker:$ktorVersion")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")

    implementation("com.zaxxer:HikariCP:4.0.2")

    implementation("org.mindrot:jbcrypt:0.4")

    implementation("org.slf4j:slf4j-simple:1.7.30")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}

val main = "com.kotme.MainKt"

tasks.jar {
    archiveVersion.set("")

    manifest {
        attributes(
            mapOf(
                "Main-Class" to main
            )
        )
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.create("stage") {
    dependsOn("build", "clean")
}

tasks.build.get().mustRunAfter(tasks.clean.get())
