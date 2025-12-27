plugins {
    java
    application
    jacoco // aggiunto plugin per la copertura del codice
    id("info.solidsoft.pitest") version "1.15.0"

}



jacoco {
    toolVersion = "0.8.12"
}

// --- Configurazione PITest (Mutation Testing) ---
configure<info.solidsoft.gradle.pitest.PitestPluginExtension> {
    // Usa il plugin per JUnit 5 (fondamentale per il tuo progetto)
    junit5PluginVersion.set("1.2.1")

    // Indica quali classi mutare (il tuo pacchetto base)
    targetClasses.set(listOf("se.chalmers.ju2jmh.*"))

    // Escludi i test stessi dalla mutazione (opzionale ma consigliato)
    targetTests.set(listOf("se.chalmers.ju2jmh.*"))

    // Ottimizzazioni
    threads.set(4) // Usa 4 core per fare prima
    outputFormats.set(listOf("HTML")) // Report leggibile
    timestampedReports.set(false) // Sovrascrivi sempre lo stesso report
    verbose.set(true)
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