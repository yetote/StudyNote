## malloc 和 new
**malloc** 和**new**在c++里都可以分配内存但是**malloc** 和**new**也有很大的区别  
##### malloc
 - 只是分配了内存，并不会执行对象的构造方法
##### new 
 - 除分配内存外，还执行了对象的构造方法  
### free和delete
##### free
 - 释放内存，不会执行对象的析构方法  
##### delete 
 - 释放内存，会执行对象的析构方法  
### delete 和delete [] 
对于简单类型(int/long/char...)没有区别  
##### delete 
 - 只会调用a指针指向的对象的析构函数
 - 和new配套使用  
##### delete []
 - 会用a指针指向的数组中的每个元素的析构函数
 - 和new []配套使用