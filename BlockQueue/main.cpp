#include <iostream>
#include <thread>
#include "BlockQueue.h"
#include "windows.h"

BlockQueue<int> blockQueue;

void produceTask() {
    for (int i = 0; i < 10; ++i) {
        blockQueue.push(i);
        std::cout << "生产者生产了产品" << i << std::endl;
        Sleep(1000);
    }
}

void consumeTask() {
    int data;
    while (true) {
        popResult res = blockQueue.pop(data);
        if (res == POP_STOP) break;
        if (res == POP_UNEXPECTED) continue;
        std::cout << "消费者消费了了产品" << data << std::endl;
        Sleep(500);
    }
}

int main() {

    std::thread produceThread(produceTask);
    std::thread consumeThread(consumeTask);
    produceThread.join();
    std::cout << "生产者停止生产\n" << std::endl;
    blockQueue.stop();
    std::cout << "队列接受停止通知\n" << std::endl;
    consumeThread.join();
    std::cout << "消费者停止消费\n" << std::endl;
    return 0;
}