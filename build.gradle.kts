plugins {
    java
    application
}

group = "com.pelugestion"
version = "1.12"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // UI — FlatLaf: look-and-feel moderno para Swing (estilo IntelliJ IDEA)
    implementation("com.formdev:flatlaf:3.4")

    // Layouts — MigLayout: sistema de layout flexible y legible
    implementation("com.miglayout:miglayout-swing:11.3")

    // Base de datos — SQLite embebida (sin servidor)
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")

    // Selector de fecha con calendario (Java puro, compatible 32/64 bits)
    implementation("com.github.lgooddatepicker:LGoodDatePicker:11.2.1")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

application {
    mainClass.set("com.pelugestion.App")
}

tasks.test {
    useJUnitPlatform()
}

// Fat JAR: empaqueta todo en un solo .jar para pruebas rapidas en Windows
tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Crea un JAR ejecutable con todas las dependencias incluidas"
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.pelugestion.App"
        // Permite el acceso nativo del driver SQLite sin warnings (Java 21+)
        attributes["Enable-Native-Access"] = "ALL-UNNAMED"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get())
}
