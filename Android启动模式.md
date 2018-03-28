Android启动模式分为四种，分别为standard,singleTop
在了解启动模式模式，我们先来了解下任务栈的概念
任务栈是指Activity实例存在的环境，有一个参数叫做TaskAiffinty,这个参数标识了一个Activity的任务栈的名字，通常为Activity的包名,任务栈分为前台任务栈与后台任务栈，后台任务栈的Activity是暂停状态的。用户可通过切换状态来将后台任务栈切换到前台
**standard**表示标准模式，也是Android系统默认的启动模式，表示每启动一个新的Activity都会常见一个当前的Activity实例，在这种模式下，谁启动了这个Activity，这个Activity就进入到谁的任务栈中，一个任务栈可以有多个不同的Activity实例，一个Activity实例也可以存在多个任务栈中
**singleTop** 栈顶复用模式
在这种模式下，如果Activity位于栈顶，那么当再次启动Activity的时候，该Activity不会被创建同时他的 **onNewIntent()** 会被调用
**singleTask**栈内复用模式 在该模式下，如果Activity位于**同一个**任务栈内，那么当再次启动该Activity的时候，该Activity就不会被创建
**注意**因为singleTask自带clearTop效果，当该Activity没有位于栈顶时，会将在他之上的Activity全部弹出栈。
![singleTask](https://user-gold-cdn.xitu.io/2018/3/28/1626d26f800d49c2?w=899&h=535&f=png&s=12030)
**singleInstance** 单实例模式 z和是一种加强的singleTask模式,相比较singleTask，这种模式加强了Activity只能单独存在一个任务栈内
如何个Activity指定启动模式呢，一种是通过AndroidMenifest为Activity指定启动模式，
```
android:launchMode="singleTask"
```
另一种是通过代码的方式指定
```
 Intent i=new Intent();
 i.setClass(MainActivity.this,SecondActivity.class);
 i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 startActivity(i);
```
两种方法各有局限性，但是第二种优先度比第一种高，可以根据场景的不同灵活使用
下面介绍下常用的Activity的flags
FLAG_ACTIVITY_SINGLE_TOP
这个flag是为Activity指定singleTop启动模式
和xml配置效果相同
FLAG_ACTIVITY_NEW_TASK
指定启动模式为singleTask，和xml配置效果一样
FLAG_ACTIVITY_CLEAR_TOP
此标记的Activityz在启动时，位于他之上的Activity都要出栈，而且singleTask自带该效果
FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
具有此标记的Activity不会出现在历史列表里，它等同于xml中配置
```
 android:excludeFromRecents="true"
```