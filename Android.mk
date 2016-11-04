LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_JAVA_LIBRARIES := \
        android-support-v4 \
        libgson \
        liblitepal \
        libvolley \
        libachartengine \
        libmina \
        libslf4j
        
LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := FPHealth

LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := full
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
                       libgson:libs/gson-2.2.4.jar \
                       liblitepal:libs/litepal-1.3.2-src.jar \
                       libvolley:libs/com.android.volley-2015.05.28.jar \
                       libachartengine:libs/achartengine-1.2.0.jar \
                       libmina:libs/mina-core-2.0.13.jar \
                       libslf4j:libs/slf4j-api-1.7.14.jar
                       
                       
                       
include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
