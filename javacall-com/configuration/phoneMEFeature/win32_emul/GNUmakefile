#
# Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License version
# 2 only, as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License version 2 for more details (a copy is
# included at /legal/license.txt).
#
# You should have received a copy of the GNU General Public License
# version 2 along with this work; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA
#
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
# Clara, CA 95054 or visit www.sun.com if you need additional
# information or have any questions.
#

ifeq ($(COMPONENTS_DIR),)
$(error COMPONENTS_DIR is not set.)
endif

LIME_DIR ?= $(COMPONENTS_DIR)/lime

ifeq ($(JAVACALL_DIR),)
$(error JAVACALL_DIR is not set.)
endif

ifeq ($(PROJECT_JAVACALL_DIR),)
$(error PROJECT_JAVACALL_DIR is not set.)
endif

include environment.gmk

#For each project location for basic definitions shall be specified
#(location of the used javacall_platform_defs.h)
JAVACALL_PLATFORM_DEFS_DIR=$(JAVACALL_DIR)/implementation/win32_emul

#For each project specified toolchain shall be selected
include $(JAVACALL_DIR)/build/vc/rules.gmk

ifeq ($(BUILD_LIME_LIB),true)
include $(LIME_DIR)/client/build/share/rules_lime.gmk
else
JAVACALL_EXTRA_LIBS::
	$(AT)cp $(LIME_DIR)/sublime.* $(JAVACALL_OUTPUT_LIB_DIR)
endif

#Specific targets can be achieved before and after achievement of common targets
#Specify these targets after including 'rules.gmk' file.

# Copies files from specified sub directories that may be under svn control
# Usage: copy_directory(src_dir, output_dir, sub_dir_list)
define copy_directory
    for i in $(3); do \
        mkdir -p $(2)/$$i; \
        if [ -d "$(1)/.svn" ]; then \
            svn export --force -q $(1)/$$i $(2)/$$i; \
        else \
            cp -rp $(1)/$$i/* $(2)/$$i; \
        fi \
    done
endef


wtk_storage::

ifeq ($(USE_JSR_75),true)
wtk_storage::
	@echo "Copying JSR-75 storage structure..."
	$(AT)$(call copy_directory,$(JSR_75_JC_IMPL_PATH)/$(JSR_75_JC_DIR),\
	$(JAVACALL_OUTPUT_DIR),storage)
endif


pre_target:

post_target: $(JAVACALL_OUTPUT_LIB_DIR)/emulator.res $(LIME_LIB) \
	$(LIME_SHARED_LIB) wtk_storage

source_bundle::
	$(AT)rm -fr $(SOURCE_OUTPUT_DIR)/javacall-com/build/docs
