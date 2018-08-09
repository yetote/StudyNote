//
// Created by ether on 2018/8/9.
//

#ifndef OPENSLDEMO_OPENSLTEST_H
#define OPENSLDEMO_OPENSLTEST_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

class OpenSLTest {
private:
    SLObjectItf engineObject;
    SLEngineItf engineEngine;
    bool isInit;

    /*
     * 创建一个引擎对象接口
     * @return 创建结果
     * */
    SLresult createEngine() {
        SLEngineOption engineOptions[] = {{(SLuint32) SL_ENGINEOPTION_THREADSAFE, (SLuint32) SL_BOOLEAN_TRUE}};
        return slCreateEngine(&engineObject, ARRAY_LEN(engineOptions), engineOptions,
                              0, // no interfaces
                              0, // no interfaces
                              0); // no required
    };

    /*
     * 实例化引擎对象
     * @param
     * @return 创建结果
     * */
    SLresult RealizeObject(SLObjectItf object) {
        return (*object)->Realize(object, SL_BOOLEAN_FALSE);
    };

    /*
     * 获取引擎的方法接口
     * @return 获取结果
     * */
    SLresult GetEngineInterface() {
        return (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    };
    OpenSLTest();
    void init();
    static OpenSLTest* instance;
public:
    static OpenSLTest* GetInstance(); //工厂方法(用来获得实例)
    virtual ~OpenSLTest();
    SLEngineItf getEngine() {
        return engineEngine;
    };
};


#endif //OPENSLDEMO_OPENSLTEST_H
