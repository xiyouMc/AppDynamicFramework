<pre><code>
apply plugin: 'com.android.application'
apply plugin: 'maven'
apply plugin: 'signing'

//定义GroupID和Version，ArtefactID会自动使用Project名
group = 'com.xxxx.mobile'
version = '1.0.1'

android {
    compileSdkVersion 22
    buildToolsVersion '22'

    defaultConfig {
        minSdkVersion 15
        targetSdkVersion 22
        versionCode 1
        versionName "1.0"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }

}

artifacts {
    archives file('build/libs/' + project.name + '.jar')
}

signing {
    required { has("release") && gradle.taskGraph.hasTask("uploadArchives") }
    sign configurations.archives
}

uploadArchives {
    configuration = configurations.archives
    repositories.mavenDeployer {
        beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }
        repository(url: 'http://192.168.1.3:8081/nexus/xxxx/') {//仓库地址
            authentication(userName: "admin",//用户名
                    password: "admin123")//密码
        }

        pom.project {
            name project.name
            packaging 'jar'
            description '容器内核'
            url 'http://192.168.1.3:8081/nexus/xxxxx/'//仓库地址

            developers {
                developer {
                    id 'mc'
                    name 'ma machao'
                    email 'code.mylover@yeah.net'
                }
            }
        }
    }
}
//dependsOn 可根据实际需要增加或更改 dependsOn: ['compileReleaseJava'],
task buildJar(type: Jar) {

    appendix = project.name
    baseName = project.name
    version = "1.0.0"
    classifier = "release"

    //后缀名
    extension = "jar"
    //最终的 Jar 包名，如果没设置，默认为 [baseName]-[appendix]-[version]-[classifier].[extension]
    archiveName = project.name＋".jar"

    //需打包的资源所在的路径集
    def srcClassDir = [project.buildDir.absolutePath + "/intermediates/classes/release"];
    //初始化资源路径集
    from srcClassDir

    //去除路径集下部分的资源
    exclude "com/xxx/mobile/" + project.name + "/BuildConfig.class"
    exclude "com/xxx/mobile/" + project.name + "/BuildConfig\$*.class"
    exclude "**/R.class"
    exclude "**/R\$*.class"
    //只导入资源路径集下的部分资源
    include "com/xxx/mobile/" + project.name + "/**/*.class"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.1.0'
    }
}

allprojects {
    repositories {
        mavenCentral()
        //这里加入自己的maven地址
        maven {
            url "http://192.168.1.3:8081/nexus/xxxx/"
        }
    }
}

dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    compile 'com.android.support:support-v4:22.0.0'
    provided 'com.xxx.mobile:framework:1.0'
    provided 'com.xxx.mobile:xxapi:1.0.1'
}

</code></pre>