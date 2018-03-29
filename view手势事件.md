# View的手势事件
## MotionEvent
**ACTION_DOWN** 手指按下  
**ACTION_UP** 手指松开  
**ACTION_MOVE** 手指滑动  
#### 分析几种常见的情况
##### 手指按下然后松开   
DOWN->UP  
##### 手指在屏幕上滑动  
DOWN->MOVE->MOVE->...->MOVE->UP  

**MotionEvent**提供了两种方法获得点击事件的xy坐标，分别为**getX(getY)**/**getRawX(getRawY)**
区别在于**get**是相对于当前view的xy坐标，**getRaw**是相对于屏幕左上角的xy坐标  
## TouchSlop
是系统能识别的最小滑动距离，可以通过
```
 ViewConfiguration.get(MainActivity.this).getScaledEdgeSlop()   
```
来获得，
![TouchSlop](https://user-gold-cdn.xitu.io/2018/3/29/16272450e98b682a?w=799&h=22&f=png&s=4970)  
可以看出我的TouchSlop为36，
## VelocityTracker速度追踪
用于追踪手指在滑动中的速度，
```
 VelocityTracker velocityTracker = VelocityTracker.obtain();
 velocityTracker.addMovement(event);
```
然后我们便可以获得速度了
```
//设置单位时间
 velocityTracker.computeCurrentVelocity(1000);
 int xVelocity = (int) velocityTracker.getXVelocity();
 int yVelocity = (int) velocityTracker.getYVelocity();
 Log.e(TAG, "onTouch: x=" + xVelocity + "\n"
                + "y=" + yVelocity);
```
单位时间是指在多少时间内手指滑动的距离，这里是1s。  
注意，速度也有正负值，向右(向上)速度即为负值，这和Android的坐标系有关。  
当我们不用的时候要回收内存
```
velocityTracker.clear();
velocityTracker.recycle();
```
## GestureDetector
手势检测，用于检测用户的单机滑动双击长按等手势  
使用方法是先实例化一个GesrureDetector,并实现其中的方法
```
 gestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                Log.e(TAG, "onDown: " );
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                Log.e(TAG, "onShowPress: " );
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.e(TAG, "onSingleTapUp: " );
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.e(TAG, "onScroll: " );
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.e(TAG, "onLongPress: " );
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.e(TAG, "onFling: " );
                return false;
            }
        });

```
然后在OnTouchEvent接管View的OnTouchEvent事件
```
 boolean consume = gestureDetector.onTouchEvent(event);
 return consume;
```
这样就可以了  
![GesureDetector](https://user-gold-cdn.xitu.io/2018/3/29/162725af64a95c50?w=229&h=256&f=png&s=6003)  
而且我们还可以通过onDoubleTapListener来监听别的事件，这里我就不贴代码了[onDoubleTapListener用法](https://www.cnblogs.com/ldq2016/p/7000300.html)  
如果是监听滑动行为的话，建议使用**MotionEvent**，如果是监听双击行为的话，使用**GesureDetector**  
## **Scroller**
滑动行为
用来实现view的滑动，有**scrollerTo**和**scrollerBy**两个方法，但是滑动的过程是瞬间完成的，体验并不好，可以配合View的**computeScroll**方法实现弹性滑动
