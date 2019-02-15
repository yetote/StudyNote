//
// Created by ether on 2018/10/11.
//

#ifndef NDKPLAYER_BLOCKQUEUE_H
#define NDKPLAYER_BLOCKQUEUE_H

#include <queue>
#include <mutex>
#include <android/log.h>
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"BlockQueue",FORMAT,##__VA_ARGS__);

enum popResult {
    POP_OK, POP_STOP, POP_UNEXPECTED
};

template<typename T>
class BlockQueue : public std::queue<T> {
public:

    void push(const T & value){
        std::lock_guard<decltype(mLock)> lock(mLock);
        queue.push(value);
        LOGE("QUEUE大小为%ld",queue.size());
        mCond.notify_one();
    }
    void push(const T &&value) {
        std::lock_guard<decltype(mLock)> lock(mLock);
        queue.push(std::move(value));
        LOGE("QUEUE大小为%ld",queue.size());
        mCond.notify_one();
    }

    popResult pop(T &out) {
        std::unique_lock<decltype(mLock)> lock(mLock);
        if (isStop && queue.empty()) return POP_STOP;
        if (queue.empty()) mCond.wait(lock);
        if (isStop && queue.empty()) return POP_STOP;
        if (queue.empty()) return POP_UNEXPECTED;
        out = (std::move(queue.front()));
        queue.pop();
        return POP_OK;
    }

    void stop() {
        std::lock_guard<decltype(mLock)> lock(mLock);
        isStop = true;
        mCond.notify_all();
    }
    virtual ~BlockQueue() = default;
private:
    std::mutex mLock;
    std::condition_variable mCond;
    std::queue<T> queue;
    bool isStop = false;
};


#endif //NDKPLAYER_BLOCKQUEUE_H
