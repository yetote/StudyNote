//
// Created by ether on 2018/8/31.
//

#ifndef MYPLAYER_HANDLER_H
#define MYPLAYER_HANDLER_H


#include "message_queue.h"

class Handler {
private:
    MessageQueue *mQueue;
public:
    Handler(MessageQueue *mQueue);

    ~Handler();

    int postMessage(Message *msg);

    virtual void handleMessage(Message *msg) {};
};


#endif //MYPLAYER_HANDLER_H
