//
// Created by ether on 2018/10/11.
//

#ifndef NDKPLAYER_BLOCKQUEUE_H
#define NDKPLAYER_BLOCKQUEUE_H

#include <queue>
#include <mutex>
#include <android/log.h>

using namespace std;
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"BlockQueue",FORMAT,##__VA_ARGS__);

enum popResult {
    POP_OK, POP_STOP, POP_UNEXPECTED
};

template<typename T>
class BlockQueue : public std::queue<T> {
public:

    void push(const T &value) {
        lock_guard<decltype(mLock)> lock(mLock);
        queue.push(value);
        mCond.notify_one();
    }

    void push(const T &&value) {
        lock_guard<decltype(mLock)> lock(mLock);
        queue.push(move(value));
        mCond.notify_one();
    }

    popResult pop(T &out) {
        unique_lock<decltype(mLock)> lock(mLock);
        if (isStop && queue.empty()) return POP_STOP;
        if (queue.empty()) mCond.wait(lock);
        if (isStop && queue.empty()) return POP_STOP;
        if (queue.empty()) return POP_UNEXPECTED;
        out = (move(queue.front()));
        queue.pop();
//        LOGE("QUEUE大小为%ld", queue.size());
        return POP_OK;
    }

    void stop() {
        lock_guard<decltype(mLock)> lock(mLock);
        isStop = true;
        mCond.notify_all();
    }

    virtual ~BlockQueue() = default;

private:
    mutex mLock;
    condition_variable mCond;
    queue<T> queue;
    bool isStop = false;
};


#endif //NDKPLAYER_BLOCKQUEUE_H
