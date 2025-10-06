pluginManagement {
    repositories {
        // แหล่งปลั๊กอินของ Gradle + Android
        gradlePluginPortal()
        google()
        mavenCentral()
        // mavenLocal() // ใช้เฉพาะกรณีทดสอบไลบรารีในเครื่อง
    }
}

dependencyResolutionManagement {
    // ให้ใช้ repo จากไฟล์นี้เป็นหลัก (กันสับสนจากการประกาศซ้ำในโมดูล)
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        // mavenLocal() // เปิดเมื่อจำเป็นเท่านั้น
    }
}

rootProject.name = "PhysioARV5"
include(":app")
