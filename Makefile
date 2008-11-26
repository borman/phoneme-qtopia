# phoneME feature mr3 build makefile for phoneme-qtopia development

###############################
# Config variables

# System paths
JDK_DIR=/usr/java/j2sdk1.4.2_18

QTOPIA_SDK_DIR = /opt/QtopiaHost
QTOPIA_SDK_MOC = $(QTOPIA_SDK_DIR)/qtopiacore/target/bin/moc
QTOPIA_TARGET_PREFIX = /opt/QtopiaHostImage

DOXYGEN_CMD = doxygen

# Source paths
COMPONENTS_DIR = $(CURDIR)
BUILD_OUTPUT_DIR = $(COMPONENTS_DIR)/build_output_qtopia

TOOLS_DIR = $(COMPONENTS_DIR)/tools
TOOLS_OUTPUT_DIR = $(BUILD_OUTPUT_DIR)/tools

GLOBAL_CONFIG = JDK_DIR=$(JDK_DIR) \
QTOPIA_SDK_DIR=$(QTOPIA_SDK_DIR) \
QTOPIA_SDK_MOC=$(QTOPIA_SDK_MOC) \
QTOPIA_TARGET_PREFIX=$() \
DOXYGEN_CMD=$(DOXYGEN_CMD) \
COMPONENTS_DIR=$(COMPONENTS_DIR) \
BUILD_OUTPUT_DIR=$(BUILD_OUTPUT_DIR) \
TOOLS_DIR=$(TOOLS_DIR) \
TOOLS_OUTPUT_DIR=$(TOOLS_OUTPUT_DIR)

# PCSL
PCSL_NETWORK_MODULE = bsd/qtopia
PCSL_CONFIG = PCSL_PLATFORM=linux_i386_gcc \
              PCSL_OUTPUT_DIR=$(BUILD_OUTPUT_DIR)/pcsl \
							USE_VERBOSE_MAKE=true VERBOSE_BUILD=true
              
# CLDC
CLDC_JVM_CONFIG = JVMWorkSpace=$(COMPONENTS_DIR)/cldc \
                  JVMBuildSpace=$(BUILD_OUTPUT_DIR)/cldc
CLDC_PCSL_CONFIG = ENABLE_PCSL=true \
                   PCSL_OUTPUT_DIR=$(BUILD_OUTPUT_DIR)/pcsl
CLDC_FEATURES_CONFIG = ENABLE_ISOLATES=true
CLDC_COMPILER_CONFIG = GCC_VERSION=-4.2

# MIDP
MIDP_PATHS = PCSL_OUTPUT_DIR=$(BUILD_OUTPUT_DIR)/pcsl \
             CLDC_DIST_DIR=$(BUILD_OUTPUT_DIR)/cldc/linux_i386/dist \
             MIDP_OUTPUT_DIR=$(BUILD_OUTPUT_DIR)/midp
MIDP_FEATURES_CONFIG = USE_MULTIPLE_ISOLATES=true \
                       SUBSYSTEM_LCDUI_MODULES=chameleon \
                       USE_NATIVE_AMS=true USE_RAW_AMS_IMAGES=false \
                       USE_VERIFY_ONCE=true \
                       USE_RMS_TREE_INDEX=true 
MIDP_EXTRA_CONFIG = USE_ABSTRACTIONS=true ABSTRACTIONS_DIR=$(COMPONENTS_DIR)/abstractions
MIDP_DEBUG_CONFIG = USE_DEBUG=true
MIDP_COMPILER_CONFIG = GCC_VERSION=-4.2

####################
# Make rules

default: build_cldc
	
clean:
	rm -rf build_output_qtopia
  
build_pcsl:
	@make -C $(COMPONENTS_DIR)/pcsl \
	$(GLOBAL_CONFIG) \
	NETWORK_MODULE=$(PCSL_NETWORK_MODULE) $(PCSL_CONFIG)
  
#build_cldc: build_pcsl
build_cldc:
	export JDK_DIR=$(JDK_DIR)
	make -C $(COMPONENTS_DIR)/cldc/build/linux_i386 JDK_DIR=$(JDK_DIR) \
	$(GLOBAL_CONFIG) \
	$(CLDC_JVM_CONFIG) $(CLDC_PCSL_CONFIG) $(CLDC_FEATURES_CONFIG) $(CLDC_COMPILER_CONFIG)

build_midp: build_cldc
	make -C $(COMPONENTS_DIR)/midp/build/linux_qtopia_gcc \
	$(GLOBAL_CONFIG) \
	$(MIDP_PATHS) $(MIDP_FEATURES_CONFIG) $(MIDP_EXTRA_CONFIG) $(MIDP_DEBUG_CONFIG) $(MIDP_COMPILER_CONFIG)

.PHONY: default clean build_pcsl build_cldc build_midp
  