Activity生命周期分为**onCreate()**，**onstart()**，**onResume()**，**onPause()**，**onStop()**，**onDestroy()**,**onRestart()** 

- **onCreate()** 表示Activity被创建 
- **onstart()** 表示Activity正在启动，但是没有位于前台
- **onResume()** 表示Activity已经可见，并且位于前台可以与用户进行交互
- **onPause()** 表示Activity正在停止，但是可见，通常被称为暂停
- **onStop()** 表示Activity即将停止
- **onDestroy()** 表示Activity即将被销毁，进行资源的回收
- **onRestart()** 表示Activity正在重新启动，通常室友用户操作引发的，
#### Activity启动流程

```
graph TD
A(Activity启动)-->B
B(onCreate)-->C(onStart)
C-->E(onResume)
E-->F(Activity运行)
F-->D(新Activity启动)
D-->G(onPause)
G-->|返回原Activity|E
G-->|Activity已经不可见|H(onStop)
H-->|高优先级的应用需要内存|L(应用被杀死)
L-->|用户返回原Activity|B
H-->|Activity正在停止或即将被销毁|I(onDestroy)
H-->|用户返回原Activity|J(onRestart)
J-->C
I-->K(Activity被销毁)
```
####  下面分析几种状况
 - **启动一个新的Activity**
 ```
graph LR

      A(onCreate) --> B(onStart)
      B-->C(onResume)
```
 - **用户切换页面或者打开新的Activity**  
```
graph LR
     A[原onPause] --> B[新onCreate]
     B -->C[新onStart]
     C--> D[新onResume]
     D -->E[原onStop]

```
但是新Activity采用透明主题，则不会调用onStop()
 - **用户回到原Activity**  
```
graph LR

      A(onRestart) --> B(onStart)
      B-->C(onResume)
```
 - **back键返回的时候**  
```
graph LR

      A(onPause) --> B(onStop)
      B-->C(onDestroy)
```

##### **在整个Activity生命周期中**  
**onCreate**与**onDestroy**相对应,表示Activity出生与死亡，创建和销毁   
**onstart**与**onStop**相对应，表示Activity虽然可见，但是并没有位于前台  
**onResume**与**onPause**相对应，表示Activity可见并且位于前台  

##### **onStart与onResume和onStop和onPause的区别在哪里呢?**  

**onStart**与**onStop**是从是否可见角度考虑的  
**onResume**和**onPause**是从是否位于前台考虑的  
通常只保留一对即可      

#### 异常状态下生命周期
##### 资源相关配置导致Activity被杀死然后重新创建  
典型为横竖屏切换    
```
graph TD

      A(原Activity) -->|意外终止| B(onSaveInatanceState)
      B-->C(onStop)
      C-->D(onDestroy)
      
      E(新Activity)-->F(onCreate)
      F-->G(onStart)
      G-->H(onRestoreInatanceState)
      
      B-->|重新创建|F
      
```

原Activity被销毁时，onPause，onStop，onDestroy都被被调用，而onSaveInstanceState则会在onStop方法之前被调用，**但不一定在onPause之后被调用**。只有在Activity被异常结束时才会进行这个流程，正常销毁则不会进行。    
当Activity重新创建后，系统会将之前保存的bundle传递给onRestoreInstanceState  
我们可以在onCreate或onRestore中进行数据的恢复，区别是onRestore中的Bundle一定是有值的，不需要判空，而onCreate则不一定，需要进行判空操作。官方建议使用onRestore进行数据恢复  
从时序上来说onRestore会在onStart之后进行。并且除了我们手动保存的数据外，部分view中的某些属性会被自动保存，原因在于view中含有onSaveIn....和OnRestoreIn....方法，具体可以查看源码
#### 资源不足被杀死
**这种情况下我们需要了解Activity的优先级**   

Activity状态| 优先级
---|---
可见并且可以与用户进行交 | 高
可见但是不可以与用户进行交互 | 中
不可见 | 低

被杀死后重新创建的话也会调用onSave....与OnRestore....进行数据的恢复
如果不想重新创建Activity的话，可以在XML中配置**android:ConfigChanges**属性  
这种情况下不会调用onSave...方法，而是调用**onConfigChanged**方法进行数据的恢复