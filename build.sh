#!/bin/sh

build_phoneme_host_qtopia()
{
    export JDK_DIR=/usr/java/j2sdk1.4.2_18

    export COMPONENTS_DIR=/home/mike/ezx/phoneme_feature-mr3
    export BUILD_OUTPUT_DIR=$COMPONENTS_DIR/build_output_qtopia

    export TOOLS_DIR=$COMPONENTS_DIR/tools
    export TOOLS_OUTPUT_DIR=$BUILD_OUTPUT_DIR/tools

    export QTOPIA_SDK_DIR=/opt/QtopiaHost
    export QTOPIA_SDK_MOC=$QTOPIA_SDK_DIR/qtopiacore/target/bin/moc
    export QTOPIA_TARGET_PREFIX=/opt/QtopiaHostImage

    export DOXYGEN_CMD=doxygen

    #export VERBOSE_BUILD=true
    #export USE_VERBOSE_MAKE=true

    make -C $COMPONENTS_DIR/midp/build/linux_qtopia_gcc GCC_VERSION=-4.1 \
    	USE_MULTIPLE_ISOLATES=true SUBSYSTEM_LCDUI_MODULES=chameleon \
    	USE_NATIVE_AMS=true USE_RAW_AMS_IMAGES=false USE_VERIFY_ONCE=true \
    	\
    	PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl \
    	CLDC_DIST_DIR=$BUILD_OUTPUT_DIR/cldc/linux_i386/dist \
    	MIDP_OUTPUT_DIR=$BUILD_OUTPUT_DIR/midp \
    	USE_ABSTRACTIONS=true ABSTRACTIONS_DIR=$COMPONENTS_DIR/abstractions \
    	USE_VERBOSE_MAKE=false USE_DEBUG=true USE_RMS_TREE_INDEX=true

    exit 0

    make -C $COMPONENTS_DIR/midp/build/linux_qtopia_gcc GCC_VERSION=-4.1 \
    	USE_MULTIPLE_ISOLATES=true SUBSYSTEM_LCDUI_MODULES=chameleon \
    	USE_NATIVE_AMS=true USE_RAW_AMS_IMAGES=false USE_VERIFY_ONCE=true \
    	\
    	PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl \
    	CLDC_DIST_DIR=$BUILD_OUTPUT_DIR/cldc/linux_i386/dist \
    	MIDP_OUTPUT_DIR=$BUILD_OUTPUT_DIR/midp \
    	USE_ABSTRACTIONS=true ABSTRACTIONS_DIR=$COMPONENTS_DIR/abstractions \
    	USE_VERBOSE_MAKE=false USE_DEBUG=false USE_RMS_TREE_INDEX=true

    exit 0

    make -C $COMPONENTS_DIR/pcsl NETWORK_MODULE=bsd/qtopia \
    	PCSL_PLATFORM=linux_i386_gcc \
   	PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl

    make -C $COMPONENTS_DIR/cldc/build/linux_i386 \
    	GCC_VERSION=-4.1 \
    	ENABLE_PCSL=true PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl \
   	ENABLE_ISOLATES=true \
    	JVMWorkSpace=$COMPONENTS_DIR/cldc \
    	JVMBuildSpace=$BUILD_OUTPUT_DIR/cldc

    make -C $COMPONENTS_DIR/midp/build/linux_qtopia_gcc GCC_VERSION=-4.1 \
    	USE_MULTIPLE_ISOLATES=true SUBSYSTEM_LCDUI_MODULES=chameleon \
    	USE_NATIVE_AMS=true USE_RAW_AMS_IMAGES=false USE_VERIFY_ONCE=true \
    	\
    	PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl \
    	CLDC_DIST_DIR=$BUILD_OUTPUT_DIR/cldc/linux_i386/dist \
    	MIDP_OUTPUT_DIR=$BUILD_OUTPUT_DIR/midp \
    	USE_ABSTRACTIONS=true ABSTRACTIONS_DIR=$COMPONENTS_DIR/abstractions \
    	USE_VERBOSE_MAKE=true USE_DEBUG=true USE_RMS_TREE_INDEX=true
}

#-----------------------------------------------------------------------------------------------
build_phoneme_host_qte()
{
    #export JDK_DIR=/usr/lib/jvm/java-6-sun-1.6.0.06
    #export JDK_DIR=/usr/lib/jvm/java-1.5.0-sun-1.5.0.16
    export JDK_DIR=/usr/java/j2sdk1.4.2_18

    export COMPONENTS_DIR=/home/mike/ezx/phoneme_feature-mr3
    export BUILD_OUTPUT_DIR=$COMPONENTS_DIR/build_output_qte

    export TOOLS_DIR=$COMPONENTS_DIR/tools
    export TOOLS_OUTPUT_DIR=$BUILD_OUTPUT_DIR/tools

    export QTOPIA_SDK_DIR=/home/mike/ezx/qt2-sdk

    export DOXYGEN_CMD=doxygen

    #export VERBOSE_BUILD=true
    #export USE_VERBOSE_MAKE=true

    make -C $COMPONENTS_DIR/pcsl NETWORK_MODULE=bsd/qte \
    PCSL_PLATFORM=linux_i386_gcc \
    PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl \
    all doc

    make -C $COMPONENTS_DIR/cldc/build/linux_i386 \
    GCC_VERSION=-4.1 \
    ENABLE_PCSL=true PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl \
    ENABLE_ISOLATES=true \
    JVMWorkSpace=$COMPONENTS_DIR/cldc \
    JVMBuildSpace=$BUILD_OUTPUT_DIR/cldc \
    all dox

    make -C $COMPONENTS_DIR/midp/build/linux_qte_gcc GCC_VERSION=-4.1 \
    USE_MULTIPLE_ISOLATES=true SUBSYSTEM_LCDUI_MODULES=platform_widget \
    USE_NATIVE_AMS=true USE_RAW_AMS_IMAGES=false USE_VERIFY_ONCE=true \
    \
    PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl \
    CLDC_DIST_DIR=$BUILD_OUTPUT_DIR/cldc/linux_i386/dist \
    MIDP_OUTPUT_DIR=$BUILD_OUTPUT_DIR/midp \
    USE_ABSTRACTIONS=true ABSTRACTIONS_DIR=$COMPONENTS_DIR/abstractions \
    all docs_all
}

build_phoneme_host_qte_debug()
{
#export JDK_DIR=/usr/lib/jvm/java-6-sun-1.6.0.06
#export JDK_DIR=/usr/lib/jvm/java-1.5.0-sun-1.5.0.16
    export JDK_DIR=/usr/java/j2sdk1.4.2_18

    export COMPONENTS_DIR=/home/mike/ezx/phoneme_feature-mr3
    export BUILD_OUTPUT_DIR=$COMPONENTS_DIR/build_output_qte_debug

    export TOOLS_DIR=$COMPONENTS_DIR/tools
    export TOOLS_OUTPUT_DIR=$BUILD_OUTPUT_DIR/tools

    export QTOPIA_SDK_DIR=/home/mike/ezx/qt2-sdk

    export DOXYGEN_CMD=doxygen

#export VERBOSE_BUILD=true
#export USE_VERBOSE_MAKE=true

    make -C $COMPONENTS_DIR/pcsl NETWORK_MODULE=bsd/qte \
    PCSL_PLATFORM=linux_i386_gcc \
    PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl \
    all doc

    make -C $COMPONENTS_DIR/cldc/build/linux_i386 \
    GCC_VERSION=-4.1 \
    ENABLE_PCSL=true PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl \
    ENABLE_ISOLATES=true \
    JVMWorkSpace=$COMPONENTS_DIR/cldc \
    JVMBuildSpace=$BUILD_OUTPUT_DIR/cldc \
    all dox

    make -C $COMPONENTS_DIR/midp/build/linux_qte_gcc GCC_VERSION=-4.1 \
    USE_MULTIPLE_ISOLATES=true SUBSYSTEM_LCDUI_MODULES=platform_widget \
    USE_NATIVE_AMS=true USE_RAW_AMS_IMAGES=false USE_VERIFY_ONCE=true \
    \
    PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl \
    CLDC_DIST_DIR=$BUILD_OUTPUT_DIR/cldc/linux_i386/dist \
    MIDP_OUTPUT_DIR=$BUILD_OUTPUT_DIR/midp \
    USE_ABSTRACTIONS=true ABSTRACTIONS_DIR=$COMPONENTS_DIR/abstractions \
    USE_DEBUG=true \
    all docs_all
}

build_phoneme_host_qtopia

#===========================================================================================================


exit 0

#make -C $MEHOME/midp/build/linux_qte_gcc \
#  PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl CLDC_DIST_DIR=$BUILD_OUTPUT_DIR/cldc/linux_arm/dist \
#  TOOLS_DIR=$MEHOME/tools TARGET_CPU=arm GNU_TOOLS_DIR=$TOOLCHAIN \
#  USE_MULTIPLE_ISOLATES=true

# Old CLDC Build (broken)

#make -C $JVMWorkSpace/build/linux_arm \
#  MERGE_SOURCE_FILES=false GCC_VERSION=-4.1 ENABLE_COMPILATION_WARNINGS=true \
#  ENABLE_PCSL=true PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl \
#  ENABLE_ISOLATES=true \
#  ENABLE_XSCALE_WMMX_INSTRUCTIONS=true ENABLE_XSCALE_WMMX_TIMER_TICK=true ENABLE_XSCALE_WMMX_ARRAYCOPY=true \
#  ENABLE_SOFT_FLOAT=true ENABLE_ARM_VFP=false \
#  ENABLE_C_INTERPRETER=false \
#  ENABLE_MONET=true

# Building pcsl

# Building for i386
export PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl
export PCSL_PLATFORM=linux_i386_gcc
make -C $MEHOME/pcsl NETWORK_MODULE=bsd/qtopia QTOPIA_SDK_DIR=$QTOPIA_HOST_SDK_DIR

# Building for ARM
export PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl
export PCSL_PLATFORM=linux_arm_gcc
make -C $MEHOME/pcsl NETWORK_MODULE=bsd/qtopia GNU_TOOLS_DIR=$TOOLCHAIN GCC_PREFIX=$TOOLCHAIN_PREFIX

# Building CLDC

# Building for i386
export JVMWorkSpace=$MEHOME/cldc
export JVMBuildSpace=$BUILD_OUTPUT_DIR/cldc
make -C $JVMWorkSpace/build/linux_i386 \
  GCC_VERSION=-4.1 \
  ENABLE_PCSL=true PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl \
  ENABLE_ISOLATES=true \
  ENABLE_MONET=true \

# Building for ARM
export JVMWorkSpace=$MEHOME/cldc
export JVMBuildSpace=$BUILD_OUTPUT_DIR/cldc
make -C $JVMWorkSpace/build/linux_arm \
  GCC_VERSION=-4.1 \
  ENABLE_PCSL=true PCSL_OUTPUT_DIR=$BUILD_OUTPUT_DIR/pcsl \
  ENABLE_ISOLATES=true \
  ENABLE_SOFT_FLOAT=true ENABLE_ARM_VFP=false \
  ENABLE_MONET=true \
  GNU_TOOLS_DIR=$TOOLCHAIN GCC_CROSS_PREFIX=$TOOLCHAIN_PREFIX
