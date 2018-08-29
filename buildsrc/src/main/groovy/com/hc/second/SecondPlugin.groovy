package com.hc.second

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

class ApkDistExtension {
    Closure nameMap = null;
    String destDir = null;
}

public class ApkDistPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("==============================")
        println("second plugin   test!")
        println("==============================")

        //插件获取参数
        project.extensions.create('apkdistconf', ApkDistExtension);

        //创建task  名字为  apkdist
        project.task('apkdist') << {
            println("===============================> test")

            def closure = project['apkdistconf'].nameMap;
            closure('wow!')

            println "===============================> " + project['apkdistconf'].destDir
        }

        //更改apk 的生成名字 和 目录
        //Adds an action to execute immediately after this project is evaluated.
        project.afterEvaluate {

            println "gggggggggggggggggggggggggggggggggggggggggggggggggggggg"

            //只可以在 android application 或者 android lib 项目中使用
            if (!project.android) {
                throw new IllegalStateException('Must apply \'com.android.application\' or \'com.android.library\' first!')
            }

            if (project.apkdistconf.nameMap == null || project.apkdistconf.destDir == null) {
                project.logger.info('Apkdist conf should be set!')
                return
            }

            Closure nameMap = project['apkdistconf'].nameMap
            String destDir = project['apkdistconf'].destDir

            //枚举每一个 build variant
            project.android.applicationVariants.all { variant ->
                //variant.name = debug
                //variant.name = release
                def variantData = variant.variantData
                def scope = variantData.scope

                def globalScope = scope.globalScope
                def variantConfiguration = variantData.variantConfiguration
                String archivesBaseName = globalScope.getArchivesBaseName();
                println("===============================> archivesBaseName = "+archivesBaseName);
                String apkBaseName = archivesBaseName + "-" + variantConfiguration.getBaseName()
                println("===============================> apkBaseName = "+apkBaseName);
                File apkDir = new File(globalScope.getBuildDir(), "outputs/apk")
                println("===============================> apkDir = "+apkDir.getAbsolutePath()+", apkDirName = "+apkDir.getName());
                String unsigned = (variantConfiguration.getSigningConfig() == null
                        ? "-unsigned.apk"
                        : ".apk");
                println("===============================> unsigned = "+unsigned);
                String apkName = apkBaseName + unsigned
                println("===============================> apkName = "+apkName);
//                apkFile = new File(apkDir, apkName)
//                File adbFile = globalScope.androidBuilder.sdkInfo.adb;
//                println("===============================> adbFile = "+adbFile.getAbsolutePath()+"   adbFileName = "+adbFile.getName());

                println("===============================> variant.name = " + variant.name+",  variantData = "+variantData+",  scope = "+scope);





                def packageTask = project.tasks.findByName("package${variant.name.capitalize()}")
                // packageTask = task ':app:packageDebug'
                // packageTask = task ':app:packageRelease'
                println("===============================> packageTask = " + packageTask)

                variant.outputs.each {
                    output ->
                        File file = output.outputFile
                        //file = E:\workspace\MyPluginDemo\app\build\outputs\apk\app-debug.apk
                        //file = E:\workspace\MyPluginDemo\app\build\outputs\apk\app-release-unsigned.apk
                        println("===============================> file = " + file.absolutePath)
                        output.outputFile = new File(destDir, nameMap(file.getName()))  //这里调用闭包 nameMap 重新定位生成app 的位置在D盘
                }
            }
        }

        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project p) {

                println "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"

                p.android.applicationVariants.each { variant ->
                    // variant 的值 只有 debug  和 release
                    //variant.name = debug
                    //variant.name = release
                    println("----------------------------------- Action variant.name = " + variant.name)
                    def packageTask = project.tasks.findByName("package${variant.name.capitalize()}")
                    println("----------------------------------- Action packageTask = " + packageTask)

                    if (packageTask == null) {
                        return
                    }

                    packageTask.doFirst {
                        File resourceFile = packageTask.resourceFile
                        if (null != resourceFile) {
                            // E:\workspace\MyPluginDemo\app\build\intermediates\res\resources-release.ap_
                            println("----------------------------------- Action resourceFile = " + resourceFile.absolutePath)
                        }

                        Collection<File> dexFolders = null
                        try {
                            dexFolders = packageTask.dexFolders
                        } catch (MissingPropertyException e) {
                            // api is not public
                        }
                        if (null != dexFolders) {
                            for(File file : dexFolders){
                                // E:\workspace\MyPluginDemo\app\build\intermediates\transforms\dex\release\folders\1000\1f\main
                                println("----------------------------------- Action dexFolders = " + file.absolutePath)
                            }
                        }

                        Collection<File> javaResourceFiles = null
                        try {
                            javaResourceFiles = packageTask.javaResourceFiles
                        } catch (MissingPropertyException e) {
                            // api is not public
                        }
                        if (null != javaResourceFiles) {
                            for(File file : javaResourceFiles){
                                println("----------------------------------- Action javaResourceFiles = " + file.absolutePath)
                            }
                        }


                        Collection<File> jniFolders = null
                        try {
                            jniFolders = packageTask.jniFolders
                        } catch (MissingPropertyException e) {
                            // api is not public
                        }
                        if (null != jniFolders) {
                            for(File file : jniFolders){
                                println("----------------------------------- Action jniFolders = " + file.absolutePath)
                            }
                        }

                        File assets = null;
                        try {
                            assets = packageTask.assets
                        } catch (MissingPropertyException e) {
                            // Android Gradle Plugin version < 2.2.0-beta1
                        }
                        if (null != assets) {
                            // E:\workspace\MyPluginDemo\app\build\intermediates\assets\release
                            println("----------------------------------- Action assets = " + assets.absolutePath)
                        }
                    }
                }
            }
        })
    }
}