plugins {
    java
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    val jUnit4Version: String by rootProject.extra

    compileOnly("junit", "junit", jUnit4Version)
    //dipendenza necessaria per eseguire i test con JUnit 5
    implementation("org.junit.jupiter:junit-jupiter:5.10.0")
    // rendere disponibili le annotazioni JMH per i file di test-input-classes
    compileOnly("org.openjdk.jmh:jmh-core:1.37")

}

tasks.register<Copy>("copySourcesToResources") {
    val mainSourceSet = sourceSets.main.get()
    from("src/main/java")
    into("build/resources/main")
    include("**/*.java")
}

tasks.named("processResources") {
    dependsOn("copySourcesToResources")
}
