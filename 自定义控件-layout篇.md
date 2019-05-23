###   **重写onMeasure()计算内部布局**
 - 调用每个子view的onMeasure()，来让子view进行自我测量
 ```
int childWidthSpec, childHeightSpec;
        int selfWidthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int selfWidthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            LayoutParams lp = v.getLayoutParams();
            switch (lp.width) {
                case MATCH_PARENT:
                    if (selfWidthSpecMode == MeasureSpec.EXACTLY || selfWidthSpecMode == MeasureSpec.AT_MOST) {
                        childWidthSpec = MeasureSpec.makeMeasureSpec(selfWidthSpecSize - usedSize, MeasureSpec.EXACTLY);
                    } else {
                        childWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    }
                    break;
                case WRAP_CONTENT:
                    if (selfWidthSpecMode == MeasureSpec.EXACTLY || selfWidthSpecMode == MeasureSpec.AT_MOST) {
                        childWidthSpec = MeasureSpec.makeMeasureSpec(selfWidthSpecSize - usedSize, MeasureSpec.AT_MOST);
                    } else {
                        childWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    }
                    break;
                default:
                    childWidthSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
                    break;
            }
        }
 ```
 - 根据子view测量的尺寸，得出子view的位置，并将位置保存起来
 - 根据子view的位置和尺寸计算出自己的尺寸，并通过setMeasureDimension()保存起来  
 
###  **重写onLayout()摆放子控件**
## MODE
 - MeasureSpec.EXACTLY 
 - MeasureSpec.AT_MOST 
 - MeasureSpec.UNSPECIFIED