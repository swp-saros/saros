plugins {
  id("saros.gradle.eclipse.plugin")
}

val versionQualifier = ext.get("versionQualifier") as String
val commonsLang = ext.get("commons-lang3") as String

val log4j2ApiVersion = ext.get("log4j2ApiVersion") as String
val log4j2CoreVersion = ext.get("log4j2CoreVersion") as String
val log4j2BridgeVersion = ext.get("log4j2BridgeVersion") as String

configurations {
	/*val bundle by getting {}
	//val bundleApi by getting {}
	//val api by getting {
	//	extendsFrom(bundleApi)
	//}
	//val implementation by getting {
	//	extendsFrom(bundle)
	//}

    // Defined in root build.gradle
    val testConfig by getting {}
    val releaseDep by getting {}

    // Default configuration
    val compile by getting {
        extendsFrom(releaseDep)
    }
    val testCompile by getting {
        extendsFrom(testConfig)
    }
    //val compile by getting {}
    val plain by creating {
        extendsFrom(compile)
    }
}

sarosEclipse {
    manifest = file("META-INF/MANIFEST.MF")
    isCreateBundleJar = true
    isAddPdeNature = true
    pluginVersionQualifier = versionQualifier
    configs = listOf(
    	"bundle", "bundleApi"
    	)
}

dependencies {
    bundle("commons-codec:commons-codec:1.3")
    bundle("commons-io:commons-io:2.0.1")
    bundle("org.apache.commons:commons-lang3:3.8.1")

    bundleApi("javax.jmdns:jmdns:3.4.1")
    bundleApi("xpp3:xpp3:1.1.4c")
    bundleApi("com.thoughtworks.xstream:xstream:1.4.10")
    bundleApi("org.gnu.inet:libidn:1.15")

    bundle(log4j2ApiVersion)
    bundle(log4j2CoreVersion)
    bundle(log4j2BridgeVersion)

    // TODO: use real release. This version is a customized SNAPSHOT
    bundleApi(files("libs/weupnp.jar"))
    // Workaround until we updated to a newer smack version
    bundleApi(files("libs/smack-3.4.1.jar"))
    bundleApi(files("libs/smackx-3.4.1.jar"))
    // Workaround until we can publish and use (without a user token) the repackaged jar in GitHub Packages
    bundleApi(rootProject.files("libs/picocontainer-2.11.2-patched_relocated.jar"))
}

sourceSets {
    main {
        java.srcDirs("src", "patches")
        resources.srcDirs("resources")
    }
    test {
        java.srcDirs("test/junit")
    }
}

tasks {

    val testJar by registering(Jar::class) {
        classifier = "tests"
        from(sourceSets["test"].output)
    }

    // Jar containing only the core code (the default jar is an osgi bundle
    // containing a lib dir with all dependency jars)
    val plainJar by registering(Jar::class) {
        manifest {
            from("META-INF/MANIFEST.MF")
        }
        from(sourceSets["main"].output)
        classifier = "plain"
    }

    artifacts {
        add("testing", testJar)
        add("plain", plainJar)
    }
}
