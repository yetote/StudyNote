//
// Created by ether on 2018/8/31.
//

#include "VideoOutput.h"

#define LOG_TAG "VideoOutput"

VideoOutput::VideoOutput() {
    renderer = NULL;
    handler = NULL;
    queue = NULL;
    surfaceWindow = NULL;
    forceGetFrame = false;
    surfaceExists = false;
    eglCore = NULL;
    isANativeWindowValid = false;
    renderTexSurface = EGL_NO_SURFACE;

    eglHasDestroyed = false;
}

VideoOutput::~VideoOutput() {

}

bool VideoOutput::initOutput(ANativeWindow *window, int screenWidth, int screenHeight,
                             getTextureCallback produceDataCallback,
                             void *ctx) {
    this->ctx = ctx;
    this->produceDataCallback = produceDataCallback;
    this->screenWidth = screenWidth;
    this->screenHeight = screenHeight;

    if (NULL != window) {
        isANativeWindowValid = true;
    }

    queue = new MessageQueue("video output message queue");
    handler = new VideoOutputHandler(this, queue);

    handler->postMessage(new Message(VIDEO_OUTPUT_MESSAGE_CREATE_EGL_CONTEXT, window));
    pthread_create(&_threadId, 0, threadStartCallback, this);
    return true;
}

bool VideoOutput::createEGLContext(ANativeWindow *window) {
    LOGI("enter VideoOutput::createEGLContext");
    eglCore = new EGLCore();
    LOGI("enter VideoOutput use sharecontext");
    bool ret = eglCore->initWithSharedContext();
    if (!ret) {
        LOGI("create EGL Context failed...");
        return false;
    }
    this->createWindowSurface(window);
    eglCore->doneCurrent();    // must do this before share context in Huawei p6, or will crash
    return ret;
}
void* VideoOutput::threadStartCallback(void *myself) {
    VideoOutput *output = (VideoOutput*) myself;
    output->processMessage();
    pthread_exit(0);
}
void VideoOutput::processMessage() {
    bool renderingEnabled = true;
    while (renderingEnabled) {
        Message* msg = NULL;
        if(queue->dequeueMessage(&msg, true) > 0){
//			LOGI("msg what is %d", msg->getWhat());
            if(MESSAGE_QUEUE_LOOP_QUIT_FLAG == msg->execute()){
                renderingEnabled = false;
            }
            delete msg;
        }
    }
}
/** 当surface创建的时候的调用 **/
void VideoOutput::onSurfaceCreated(ANativeWindow* window) {
    LOGI("enter VideoOutput::onSurfaceCreated");
    if (handler) {
        isANativeWindowValid = true;
        handler->postMessage(new Message(VIDEO_OUTPUT_MESSAGE_CREATE_WINDOW_SURFACE, window));
        handler->postMessage(new Message(VIDEO_OUTPUT_MESSAGE_RENDER_FRAME));
    }
}
void VideoOutput::createWindowSurface(ANativeWindow* window) {
    LOGI("enter VideoOutput::createWindowSurface");
    this->surfaceWindow = window;
    renderTexSurface = eglCore->createWindowSurface(window);
    if (renderTexSurface != NULL){
        eglCore->makeCurrent(renderTexSurface);
        // must after makeCurrent
        renderer = new VideoGLSurfaceRender();
        bool isGLViewInitialized = renderer->init(screenWidth, screenHeight);// there must be right：1080, 810 for 4:3
        if (!isGLViewInitialized) {
            LOGI("GL View failed on initialized...");
        } else {
            surfaceExists = true;
            forceGetFrame = true;
        }
    }
    LOGI("Leave VideoOutput::createWindowSurface");
}