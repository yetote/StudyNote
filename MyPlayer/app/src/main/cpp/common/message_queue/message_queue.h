//
// Created by ether on 2018/8/31.
//

#ifndef MYPLAYER_MESSAGE_QUEUE_H
#define MYPLAYER_MESSAGE_QUEUE_H
#define MESSAGE_QUEUE_LOOP_QUIT_FLAG        19900909
#include "../../util/CommonTools.h"
#include <pthread.h>

class Handler;

class Message {
private:
    int what;
    int arg1;
    int arg2;
    void *obj;
public:
    Message();

    Message(int what);

    Message(int what, int arg1, int arg2);

    Message(int what, void *obj);

    Message(int what, int arg1, int arg2, void *obj);

    ~Message();

    int execute();

    int getWhat() {
        return what;
    };

    int getArg1() {
        return arg1;
    };

    int getArg2() {
        return arg2;
    };

    void *getObj() {
        return obj;
    };
    Handler *handler;
};

typedef struct MessageNode {
    Message *msg;
    struct MessageNode *next;

    MessageNode() {
        msg = NULL;
        next = NULL;
    }
} MessageNode;

class MessageQueue {
    //todo 数据结构，多线程 锁机制  哎，慢慢来吧
    MessageNode *mFirst;
    MessageNode *mLast;
    int mNbPackets;
    bool mAbortRequest;
    pthread_mutex_t mLock;
    pthread_cond_t mCondition;
    const char *queueName;
public:
    MessageQueue();

    MessageQueue(const char *queueNameParam);

    ~MessageQueue();

    /**
     *初始化
     */
    void init();

    /**
     *
     */
    void flush();

    /**
     *
     * @param msg
     * @return
     */
    int enqueueMessage(Message *msg);

    /**
     *
     * @param msg
     * @param block
     * @return
     */
    int dequeueMessage(Message **msg, bool block);

    /**
     * 队列长度
     * @return  队列长度
     */
    int size();

    /**
     *
     */
    void abort();
};

#endif //MYPLAYER_MESSAGE_QUEUE_H
