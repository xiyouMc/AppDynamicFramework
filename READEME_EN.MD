The Framework depeny on [Dynamic-load-apk](https://github.com/singwhatiwanna/dynamic-load-apk). Add the way of dynamic load plugins and inspect bundle` service.

Now ,This demo, On the basis of h5core and h5api

#### update log:
>
* 2016/7/6  commit -m "增加懒加载功能"   hash: 2a335dc49654c80fb6779cacefdf3ed712c23a8

#### Features
> - a similar functionality encapsulated into the Application
> - Scheduled rules 、dynamic load apk and inspect service
> - Apk to So, put into project/libs/jniLibs,in order to  when apk compile,the so file can be inspect to data/data/xxxx/lib folder
> - The bundle include the model of api、core.
> - This portal model include bundleList.config. config:

     bundleName=h5core    //directly load
     lazyBundle=h5core.H5Service&H5Api //lazy load


## 一、Framework

- Framework provides the frame of loaing apk,and a load module independently BaseMateinfo

###  Introduce
1. you need to creat Metainfo of extends BaseMetainfo。
2. This Framework provides some Class: BasePluginActivity,BasePluginFragmentActivity,BasePluginService,BaseMateinfo,MarkApplication.

        BasePluginActivity: The base Activity，you must need to extends this class.
        BasePluginFragmentActivity： The base fragment activity
        BasePluginService:The base service
        BaseMateinfo: The base class of inspect service.
        MarkApplication:  The application of model,you can get Context 、findService、start Activity.

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

1、Api包的[build.gradle模版](https://github.com/xiyouMc/AppDynamicFramework/blob/master/API.MD)
2、Core包的 [build.gradle模版](https://github.com/xiyouMc/AppDynamicFramework/blob/master/CORE.MD)


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
