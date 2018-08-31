//
// Created by ether on 2018/8/31.
//

#include "Handler.h"

Handler::Handler(MessageQueue *queue) {
    this->mQueue = queue;
}

Handler::~Handler() {

}

int Handler::postMessage(Message *msg) {
    msg->handler = this;
//	LOGI("enqueue msg what is %d", msg->getWhat());
    return mQueue->enqueueMessage(msg);
}
