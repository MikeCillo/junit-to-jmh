plugins {
    java
    application
    jacoco // aggiunto plugin per la copertura del codice
}



jacoco {
    toolVersion = "0.8.12"
}

dependencies {
    val javaparserVersion: String by rootProject.extra
    val jUnitJupiterVersion: String by rootProject.extra
    val jmhVersion: String by rootProject.extra
    val bcelVersion: String by rootProject.extra
    val jUnit4Version: String by rootProject.extra

    implementation(project(":api"))
    implementation("info.picocli", "picocli", "4.6.3")
    implementation("com.github.javaparser", "javaparser-core", javaparserVersion)
    implementation("org.openjdk.jmh", "jmh-core", jmhVersion)
    implementation("org.apache.bcel", "bcel", bcelVersion)
    implementation("junit", "junit", jUnit4Version)
    implementation("com.google.guava", "guava", "31.1-jre")
    implementation("org.freemarker", "freemarker", "2.3.31")
    implementation("commons-io", "commons-io", "2.11.0")

    testImplementation("org.junit.jupiter", "junit-jupiter-api", jUnitJupiterVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-params", jUnitJupiterVersion)
    testImplementation("org.hamcrest", "hamcrest-library", "2.2")
    testImplementation("io.github.java-diff-utils", "java-diff-utils", "4.12")
    testImplementation(project(":converter:test-input-classes"))

    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", jUnitJupiterVersion)
}

application {
    mainClass.set("se.chalmers.ju2jmh.Converter")
}

// --- INIZIO BLOCCO DA COPIARE IN FONDO ---

tasks.named<Test>("test") {
    useJUnitPlatform()
    // Usa il nome stringa per evitare errori se l'accessor non è pronto
    finalizedBy("jacocoTestReport")
}

// Configurazione sicura del report JaCoCo
tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test")) // Esegue prima i test
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.required.set(true) // Genera HTML
    }
}