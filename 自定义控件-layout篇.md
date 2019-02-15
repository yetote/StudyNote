1.重写onMeasure()计算内部布局
     - 调用每个子view的onMeasure()，来让子view进行自我测量
     - 根据子view测量的尺寸，得出子view的位置，并将位置保存起来
     - 根据子view的位置和尺寸计算出自己的尺寸，并通过setMeasureDimension()保存起来  
2.重写onLayout()摆放子控件  
