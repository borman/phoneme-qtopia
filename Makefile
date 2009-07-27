# phoneME feature mr3 build makefile for phoneme-qtopia development

###############################
ARCH ?= i386
include config_$(ARCH).gmk
####################
# Make rules

default: build_midp
	
clean: 
	rm -rf $(BUILD_OUTPUT_DIR)

clean_pcsl:
	rm -rf $(BUILD_OUTPUT_DIR)/pcsl
 
clean_cldc:
	rm -rf $(BUILD_OUTPUT_DIR)/cldc
  
clean_midp:
	rm -rf $(BUILD_OUTPUT_DIR)/midp_$(MIDP_UI_MODULE)
 

build_pcsl:
	$(MAKE) -C $(COMPONENTS_DIR)/pcsl \
	$(GLOBAL_CONFIG) \
	NETWORK_MODULE=$(PCSL_NETWORK_MODULE) $(PCSL_CONFIG)
  
build_cldc: build_pcsl build_only_cldc

build_only_cldc:
	$(MAKE) -C $(COMPONENTS_DIR)/cldc/build/$(CLDC_TARGET) JDK_DIR=$(JDK_DIR) \
	$(GLOBAL_CONFIG) \
	$(CLDC_MAKE_CONFIG) $(CLDC_JVM_CONFIG) $(CLDC_PCSL_CONFIG) $(CLDC_FEATURES_CONFIG) $(CLDC_COMPILER_CONFIG)

build_midp: build_cldc build_only_midp

build_only_midp:
	$(MAKE) -C $(COMPONENTS_DIR)/midp/build/linux_qtopia_gcc \
	$(GLOBAL_CONFIG) \
	$(MIDP_PATHS) $(MIDP_FEATURES_CONFIG) $(MIDP_EXTRA_CONFIG) $(MIDP_DEBUG_CONFIG) $(MIDP_COMPILER_CONFIG)

.PHONY: default clean clean_pcsl clean_cldc clean_midp build_pcsl build_cldc build_midp build_only_cldc build_only_midp
  
