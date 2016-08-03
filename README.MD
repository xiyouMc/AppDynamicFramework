本框架是建立在 [Dynamic-load-apk](https://github.com/singwhatiwanna/dynamic-load-apk)进行的上层封装。增加插件动态加载到libs目录和针对模块Service的注入。

本文将以H5Core为插件进行讲解。

##### 更新日志：
>
* 2016/7/6  commit -m "增加懒加载功能"   hash: 2a335dc49654c80fb6779cacefdf3ed712c23a8




# 插件化框架简介
* 插件化是将Apk中功能类似的模块封装到独立的Application中，并根据框架约定好的规则完成Apk的动态加载和Service的注入。
* 本框架是将每一个Apk作为so并使用定制化打包脚本将so文件打到主Project/libs/jniLibs，这样在apk编译的时候就可以将so文件直接装载进data/data/xxxxx/lib目录，支持后续的DexClassLoader加载该文件。
* 每一个模块分为Api和Core，Api作为模块对外提供的接口，Core作为封装好的独立模块，每一个模块做好自己的混淆。注入操作需在Core中定义，下文将介绍这块。
* 主Client增加bundleList.config文件，文件配置：
	
		bundleName=h5core    //直接加载的插件
		lazyBundle=h5core.H5Service&H5Api //懒加载插件
 

## 一、Framework

- Framework提供了一个动态加载apk的框架，并提供一个加载独立模块的BaseMateinfo。

### 简介
1. 开发模块时需要在  module(core)/package name/下定义Metainfo继承自BaseMateinfo。 这样该模块在主Apk安装的时候就会动态将模块的接口注入到框架，后续提供给其它组件调用。
2. 模块提供的主要方法类有:BasePluginActivity,BasePluginFragmentActivity,BasePluginService,BaseMateinfo,MarkApplication.

		BasePluginActivity: 基础的Activity，每一个模块中的Activity都需要继承该类，完成模块中的Activity的代理化。
		BasePluginFragmentActivity： 基础的FragmentActivity，同上。需要继承该类
		BasePluginService: 基础的Service，同上。
		BaseMateinfo: 模块Service注入的基类，其它模块的Core层都需要定义一个Metainfo来继承该类，并完成Service的注入。（后面会介绍如何注入）
		MarkApplication:模块的Application，可以拿到模块的Context，并提供查找Service，启动Activity等方法。


## 二、Activity层

- 为了让proxy全面接管apk中所有activity的执行，需要为activity定义一个基类BaseActivity，在基类中处理代理相关的事情，同时BaseActivity还对是否使用代理进行了判断，如果不使用代理，那么activity的逻辑仍然按照正常的方式执行，也就是说，这个apk既可以按照执行，也可以由宿主程序来执行。
  
  	
### 独立模块架构
* 模块分类：Api和Core,针对不同业务可追加前缀。
* 每一个模块对外提供一个Service供其他模块引用。Service的Interface类放在Api模块，实现类放在Core。实现独立模块的封装。
* Service注册：在Core的根包目录创建MetaInfo类，继承Framework模块的BaseMetaInfo.如下：
	
		public class MetaInfo extends BaseMetaInfo {
    	private static final String TAG = "MetaInfo.Init";
    	public MetaInfo() {
        	Log.d(TAG,"Service init");
        	ServiceDescription serviceDescription = new ServiceDescription();
        	serviceDescription.setInterfaceName(XXService.class.getName());
        	serviceDescription.setClassName(XXServiceImpl.class.getName());
        	services.add(serviceDescription);
    	}
		}
		注解:
		ServiceDescription类是针对Service的描述类，将接口和实现封装在该对象，并将其添加到services列表中。
	以上工作就完成了模块的注入。
   
### 模块之间依赖
* 模块只要是通过Api包的依赖进行访问。由于Api是作为一个Jar存在的，因此可以直接被其它模块依赖，并切记使用 provided来依赖，防止Api的jar包被编译进模块。
* 模块之间访问：主要的类有MarkApplication、MicroApplicationContext。

		比如其他模块访问Core:
		XXService xxservice = MarkApplication.getInstance().getMicroApplicationContext().findServiceByInterface(XXService.class.getName());
		这样就可以拿到容器的Service，从而调用其提供的方法。
  	
### 模块内部资源的访问
* 由于每一个模块作为独立的apk打入主apk,因此访问该apk的上下文不再是该apk的，而是框架层的代理上下文。

		示例：
		1、Resourse获取
			MarkApplication.getInstance().getMicroApplicationContext().getResourcesByBundle("xxcore");
		2、Assets获取
			MarkApplication.getInstance().getMicroApplicationContext().getAssetsByBundle("xxcore");
			
			
			
### Gradle打包命令详解

* gradle build ：编译当前模块。
* gradle buidleJar:针对本模块生成jar包，保存目录在  xxx/build/libs/xxxx.jar
* gradle uploadArchives:上传本项目包到Nexus服务器，提供给其他模块依赖

例子：

1、Api包的build.gradle模版
2、core模块的例子


## 三、依赖关系介绍
* 如今模块化之后，依赖关系的复杂度也相比之前复杂了不少，因此梳理好依赖关系是必须考虑的问题。

### 模块化主要的依赖关系：

	框架主要有Portal、Framework、Module三个模块：
	1、Portal是项目的Launcher目录。
	2、Framework是框架的架构模块。
	3、Module是每一个模块，并分为Api和Core，并且Api作为Android.library、Core作为Android.application.
	4、每一个模块通过依赖其它模块的Api进行组件的调用。并且每一个Core都需要依赖Framework。




  
### 插件apk的开发规范
开发插件apk所需要遵循的规范：

~~1. 不能用this：因为this指向的是当前对象，即apk中的activity，但是由于activity已经不是常规意义上的activity，所以this是没有意义的~~

~~2. 使用that：既然this不能用，那就用that，that是apk中activity的基类BaseActivity中的一个成员，它在apk安装运行的时候指向this，而在未安装的时候指向宿主程序中的代理activity，anyway，that is better than this.~~

~~3. 不能直接调用activity的成员方法：而必须通过that去调用，由于that的动态分配特性，通过that去调用activity的成员方法，在apk安装以后仍然可以正常运行。~~

4. 启动新activity的约束：启动外部activity不受限制，启动apk内部的activity有限制，首先由于apk中的activity没注册，所以不支持隐式调用，其次必须通过BaseActivity中定义的新方法startActivityByProxy和startActivityForResultByProxy，还有就是不支持LaunchMode。
5. 目前暂不支持Service、BroadcastReceiver等需要注册才能使用的组件。


## 四、更新功能
* 2016/7/6 懒加载功能
	
		1、bundleList.config 文件增加lazyBundle字段来标示是否进行懒加载。字段值格式：bundleName.bundleService*bundleService。这样在该插件被调用的时候，框架采取load这个dex。	
		2、优化效果：681kb的so，首次启动懒加载优化100ms。





Thankd for your reading, by Mc...    Thanks Dynamic-load-apk


update

## Contact me

Any further question?

[Email](mailto:tmac694449212@gmail.com) me please!

## License

        Copyright 2016 xiyouMc

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
