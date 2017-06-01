package com.xpand.l12_svg_map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.xpand.l12_svg_map.entity.PathItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author: 蜗牛
 * @date: 2017/6/1 10:11
 * @desc:
 */

public class MapView extends View {
    private Paint mPaint;

    //手势监听器
    private GestureDetector mDetector;
    //缩放系数
    private float scale=1.3f;
    //保存path对象
    private List<PathItem> pathItems = new ArrayList<>();
    private String TAG="MapView";

    public MapView(Context context) {
        this(context,null);
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE,null);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDetector=new GestureDetector(new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                float x = e.getX()/scale;
                float y = e.getY()/scale;
                for (PathItem pathItem : pathItems) {
                    if (pathItem.isTouch((int) x, (int) y)){
                        pathItem.setSelect(true);
                    }else {
                        pathItem.setSelect(false);
                    }
                }
                invalidate();
                return true;
            }
        });
        parserPaths();
    }

    /**
     * 解析path
     */
    private void parserPaths() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 创建DOM工厂对象
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                try {
                    // DocumentBuilder对象
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    //打开输入流
                    InputStream is = getResources().openRawResource(R.raw.taiwan_maps);
                    // 获取文档对象
                    Document doc = db.parse(is);
                    //获取path元素节点集合
                    NodeList paths = doc.getElementsByTagName("path");
                    PathItem item;
                    for (int i = 0; i < paths.getLength(); i++) {
                        // 取出每一个元素
                        Element personNode = (Element) paths.item(i);
                        //得到android:pathData属性值
                        String nodeValue = personNode.getAttribute("android:pathData");
                        //解析，并创建pathItem
                        item=new PathItem(PathParser.createPathFromPathData(nodeValue));
                        pathItems.add(item);
                    }
                    Log.e(TAG, "蜗牛   itemsCount  " + pathItems.size());
                } catch (Exception e) {
                    Log.e(TAG, "蜗牛   解析出错 " );
                }
            }
        }).start();
        postInvalidate();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.scale(scale,scale);
        for (PathItem pathItem : pathItems) {
            pathItem.draw(canvas,mPaint);
        }
        canvas.restore();
    }
}
