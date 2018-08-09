//
// Created by ether on 2018/8/9.
//

#include "OpenSLTest.h"


OpenSLTest *OpenSLTest::instance = new OpenSLTest();

void OpenSLTest::init() {
    SLresult result = createEngine();
    if (SL_RESULT_SUCCESS == result) {
        result = RealizeObject(engineObject);
        if (SL_RESULT_SUCCESS == result) {
            result = GetEngineInterface();
        }
    }
}

OpenSLTest::OpenSLTest() {
    isInit = false;
}

OpenSLTest::~OpenSLTest() {

}

OpenSLTest *OpenSLTest::GetInstance() {
    if (!instance->isInit) {
        instance->init();
        instance->isInit = true;
    }
    return instance;
}