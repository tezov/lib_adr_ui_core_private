tezovConfig {
    release {
//        proguards.apply {
//            add(File("consumer-rules.pro"))
//        }
    }
    configureAndroidPlugin()
}

android {
    tezovCatalog {
        with("projectVersion") {
            compileSdk = int("defaultCompileSdk")
            compileOptions {
                sourceCompatibility = javaVersion("javaSource")
                targetCompatibility = javaVersion("javaTarget")
            }
            kotlinOptions {
                jvmTarget = javaVersion("jvmTarget").toString()
            }
            defaultConfig {
                minSdk = int("defaultMinCompileSdk")
            }
            buildFeatures {
                compose = true
            }
            composeOptions {
                kotlinCompilerExtensionVersion = string("composeCompiler")
            }
        }
    }
}

dependencies {
    api(project(":lib_adr_core"))
    tezovCatalog {
//api
        with("projectPath.dependencies.adr_compose") {
            api(string("ui"))
            api(string("ui_util"))
            api(string("runtime"))
            api(string("material"))
            api(string("foundation"))
            api(string("animation"))
        }
    }
}