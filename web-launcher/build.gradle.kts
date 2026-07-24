plugins {
    application
}

group = "com.joetr.lexikon"
version = rootProject.version

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass.set("com.joetr.lexikon.weblauncher.Main")
    applicationName = "lexikon-web-launcher"
}

val syncWebDistribution by tasks.registering(Sync::class) {
    dependsOn(rootProject.tasks.named("wasmJsBrowserDistribution"))
    from(rootProject.layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))
    into(layout.buildDirectory.dir("generated/web-resources/web"))
}

sourceSets.main {
    resources.srcDir(layout.buildDirectory.dir("generated/web-resources"))
}

tasks.processResources {
    dependsOn(syncWebDistribution)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}
