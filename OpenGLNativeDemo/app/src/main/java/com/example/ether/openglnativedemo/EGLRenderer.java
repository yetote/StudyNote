package com.example.ether.openglnativedemo;

import android.content.Context;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import com.example.ether.openglnativedemo.objects.Triangle;
import com.example.ether.openglnativedemo.programs.TriangleProgram;

import java.util.Arrays;

import static android.opengl.EGL14.EGL_ALPHA_SIZE;
import static android.opengl.EGL14.EGL_BLUE_SIZE;
import static android.opengl.EGL14.EGL_BUFFER_SIZE;
import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;
import static android.opengl.EGL14.EGL_DEFAULT_DISPLAY;
import static android.opengl.EGL14.EGL_GREEN_SIZE;
import static android.opengl.EGL14.EGL_NONE;
import static android.opengl.EGL14.EGL_NO_CONTEXT;
import static android.opengl.EGL14.EGL_NO_DISPLAY;
import static android.opengl.EGL14.EGL_OPENGL_ES2_BIT;
import static android.opengl.EGL14.EGL_RED_SIZE;
import static android.opengl.EGL14.EGL_RENDERABLE_TYPE;
import static android.opengl.EGL14.EGL_SURFACE_TYPE;
import static android.opengl.EGL14.EGL_WINDOW_BIT;
import static android.opengl.EGL14.eglChooseConfig;
import static android.opengl.EGL14.eglCreateContext;
import static android.opengl.EGL14.eglCreateWindowSurface;
import static android.opengl.EGL14.eglDestroyContext;
import static android.opengl.EGL14.eglDestroySurface;
import static android.opengl.EGL14.eglGetConfigs;
import static android.opengl.EGL14.eglGetDisplay;
import static android.opengl.EGL14.eglGetError;
import static android.opengl.EGL14.eglInitialize;
import static android.opengl.EGL14.eglMakeCurrent;
import static android.opengl.EGL14.eglSwapBuffers;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;


public class EGLRenderer extends HandlerThread {
    private EGLConfig eglConfig = null;
    private EGLDisplay eglDisplay = EGL_NO_DISPLAY;
    private EGLContext eglContext = EGL_NO_CONTEXT;

    private static final String TAG = "EGLRenderer";
    private Context context;

    EGLRenderer(Context context) {
        super("GLRenderer");
        this.context = context;
    }

    /**
     * 创建egl
     */
    private void createEGL() {
        //初始化显示设备
        eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        int[] version = new int[2];
        if (!eglInitialize(eglDisplay, version, 0, version, 1)) {
            Log.e(TAG, "createEGL: 初始化egl失败" + eglGetError());
        }
        int[] configAttributes = {
                EGL_BUFFER_SIZE, 32,
                EGL_ALPHA_SIZE, 8,
                EGL_RED_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                EGL_NONE
        };
        int[] numConfigs = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        if (!eglChooseConfig(eglDisplay, configAttributes, 0, configs, 0, configs.length, numConfigs, 0)) {
            Log.e(TAG, "createEGL:配置属性信息失败 " + eglGetError());
        }
        eglConfig = configs[0];
        //创建egl上下文
        int[] contextAttributes = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
        };
        eglContext = eglCreateContext(eglDisplay, eglConfig, EGL_NO_CONTEXT, contextAttributes, 0);
        if (eglContext == EGL_NO_CONTEXT) {
            Log.e(TAG, "createEGL:获取上下文信息失败 " + eglGetError());
        }
    }

    /**
     * 销毁EGL环境
     */
    private void destroyEGL() {
        eglDestroyContext(eglDisplay, eglContext);
        eglContext = EGL_NO_CONTEXT;
        eglDisplay = EGL_NO_DISPLAY;
    }

    @Override
    public synchronized void start() {
        super.start();
        new Handler(getLooper()).post(this::createEGL);
    }

    void release() {
        new Handler(getLooper()).post(() -> {
            destroyEGL();
            quit();
        });
    }

    public void renderer(Surface surface, int width, int height) {
        int[] surfaceAttributes = {EGL_NONE};
        EGLSurface eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttributes, 0);
        eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        TriangleProgram program = new TriangleProgram(context);
        Triangle triangle = new Triangle();
        glClearColor(1f, 1f, 0f, 0f);
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        program.useProgram();
        triangle.bindData(program);
        triangle.draw();
        eglSwapBuffers(eglDisplay, eglSurface);
        eglDestroySurface(eglDisplay, eglSurface);
    }
}