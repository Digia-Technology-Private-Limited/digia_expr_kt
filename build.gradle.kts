plugins {
    kotlin("jvm") version "2.0.0" 
    id("maven-publish")
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"]) // Uses the Java component
            version = "1.0.0-beta.1"
        }
    }
}