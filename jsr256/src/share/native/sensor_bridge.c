/*
 *   
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

#include <javacall_sensor.h>
#include <javacall_memory.h>
#include <kni.h>
#include <sni.h>
#include <jsrop_kni.h>


void free_string_array(javacall_utf16_string* sa, javacall_int32 size) {
  javacall_int32 i;
  
  if (!sa)
    return;
  for (i=0; i<size; ++i){
    javacall_free(sa[i]);
  }
  javacall_free(sa);
}

void null_sensor_info(javacall_sensor_info* info) {
  info->description = NULL;
  info->model = NULL;
  info->quantity = NULL;
  info->context_type = NULL;
  info->properties = NULL;
  info->err_messages = NULL;
  info->err_codes = NULL;
}

void free_sensor_info(javacall_sensor_info* info)
{
  javacall_free(info->description);
  javacall_free(info->model);
  javacall_free(info->quantity);
  javacall_free(info->context_type);
  free_string_array(info->properties, info->prop_size);
  free_string_array(info->err_messages, info->err_size);
  javacall_free(info->err_codes);
}

void null_sensor_channel(javacall_sensor_channel* ch)
{
  ch->name = NULL;
  ch->unit = NULL;
}

void free_sensor_channel(javacall_sensor_channel* ch)
{
  javacall_free(ch->name);
  javacall_free(ch->unit);
}

KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_javame_sensor_SensorRegistry_doGetNumberOfSensors(void)
{
  javacall_result res;
  int count;
  
  res = javacall_sensor_count(&count);
  switch(res){
    case JAVACALL_OK:
      KNI_ReturnInt(count);
    case JAVACALL_FAIL:
      KNI_ReturnInt(0);
  }
  
}

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_javame_sensor_Sensor_doGetSensorModel(void)
{
  int i;
  javacall_result res;
  javacall_sensor_info info;
  jfieldID f = NULL;
  int sensor_number = KNI_GetParameterAsInt(1);
  KNI_StartHandles(4);
  KNI_DeclareHandle(s_model);
  KNI_DeclareHandle(s_class);
  KNI_DeclareHandle(str);
  KNI_DeclareHandle(array);
  KNI_FindClass("com/sun/javame/sensor/SensorModel",s_class);
  KNI_GetParameterAsObject(2, s_model);
  
  null_sensor_info(&info);
  res = javacall_sensor_get_info(sensor_number,&info);
  
  if (res == JAVACALL_OK){
        //Copy all information to s_model object's fields
        
        // String description
        f = KNI_GetFieldID(s_class,"description","Ljava/lang/String;");
        jsrop_jstring_from_utf16_string(info.description, str);
        KNI_SetObjectField(s_model,f,str);
        // String model
        f = KNI_GetFieldID(s_class,"model","Ljava/lang/String;");
        jsrop_jstring_from_utf16_string(info.model, str);
        KNI_SetObjectField(s_model,f,str);
        //String quantity
        f = KNI_GetFieldID(s_class,"quantity","Ljava/lang/String;");
        jsrop_jstring_from_utf16_string(info.quantity, str);
        KNI_SetObjectField(s_model,f,str);        
        //String contextType
        f = KNI_GetFieldID(s_class,"contextType","Ljava/lang/String;");
        jsrop_jstring_from_utf16_string(info.context_type, str);
        KNI_SetObjectField(s_model,f,str);
        //int connectionType
        f = KNI_GetFieldID(s_class,"connectionType","I");
        KNI_SetIntField(s_model,f, (jint) info.connection_type);
        //int maxBufferSize
        f = KNI_GetFieldID(s_class,"maxBufferSize","I");
        KNI_SetIntField(s_model,f, (jint) info.max_buffer_size);
        //boolean availabilityPush
        f = KNI_GetFieldID(s_class,"availabilityPush","Z");
        KNI_SetBooleanField(s_model,f,info.availability_push?KNI_TRUE:KNI_FALSE);
        //boolean conditionPush
        f = KNI_GetFieldID(s_class,"conditionPush","Z");
        KNI_SetBooleanField(s_model,f,info.condition_push?KNI_TRUE:KNI_FALSE);
        //int channelCount
        f = KNI_GetFieldID(s_class,"channelCount","I");
        KNI_SetIntField(s_model,f, (jint) info.channel_count);
        //String[] properties
        f = KNI_GetFieldID(s_class,"properties","[Ljava/lang/String;");
        SNI_NewArray(SNI_STRING_ARRAY, info.prop_size*2, array);
        for (i=0; i< info.prop_size*2;++i){
          jsrop_jstring_from_utf16_string(info.properties[i], str);
          KNI_SetObjectArrayElement(array, i, str);
        }
        KNI_SetObjectField(s_model,f,array);
        //String[] errorMsgs
        f = KNI_GetFieldID(s_class,"errorMsgs","[Ljava/lang/String;");
        SNI_NewArray(SNI_STRING_ARRAY, info.err_size, array);
        for (i=0; i< info.err_size;++i){
          jsrop_jstring_from_utf16_string(info.err_messages[i], str);
          KNI_SetObjectArrayElement(array, i, str);
        }
        KNI_SetObjectField(s_model,f,array);
        //int[] errorCodes
        f = KNI_GetFieldID(s_class,"errorCodes","[I");
        SNI_NewArray(SNI_INT_ARRAY, info.err_size, array);
        for (i=0; i< info.err_size;++i){
          KNI_SetIntField(s_model,f, (jint) info.err_codes[i]);;
        }
        KNI_SetObjectField(s_model,f,array);
  }
  
  free_sensor_info(&info); // safe even if failed (pointers are null)
  
  KNI_EndHandles();
  KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_javame_sensor_ChannelImpl_doGetChannelModel(void)
{
  int i;
  javacall_result res;
  javacall_sensor_channel info;
  jfieldID f;
  int sensor_number = KNI_GetParameterAsInt(1);
  int channel_number = KNI_GetParameterAsInt(2);
  KNI_StartHandles(4);
  KNI_DeclareHandle(ch_model);
  KNI_DeclareHandle(ch_class);
  KNI_DeclareHandle(str);
  KNI_DeclareHandle(larray);
  KNI_FindClass("com/sun/javame/sensor/ChannelModel",ch_class);
  KNI_GetParameterAsObject(3, ch_model);
  
  null_sensor_channel(&info);
  res = javacall_sensor_get_channel(sensor_number, channel_number, &info);
  
  if (res==JAVACALL_OK) {
        //Copy all information to ch_model object's fields
        
        // String name
        f = KNI_GetFieldID(ch_class,"name","Ljava/lang/String;");
        jsrop_jstring_from_utf16_string(info.name, str);
        KNI_SetObjectField(ch_model,f,str);
        // String model
        f = KNI_GetFieldID(ch_class,"unit","Ljava/lang/String;");
        jsrop_jstring_from_utf16_string(info.unit, str);
        KNI_SetObjectField(ch_model,f,str);
        //int dataType
        f = KNI_GetFieldID(ch_class,"dataType","I");
        KNI_SetIntField(ch_model,f, (jint) info.data_type);        
        //int accuracy
        f = KNI_GetFieldID(ch_class,"accuracy","I");
        KNI_SetIntField(ch_model,f, (jint) info.accuracy);
        //int scale
        f = KNI_GetFieldID(ch_class,"scale","I");
        KNI_SetIntField(ch_model,f, (jint) info.scale);
        //int mrangeCount
        f = KNI_GetFieldID(ch_class,"mrangeCount","I");
        KNI_SetIntField(ch_model,f, (jint) info.mrange_count);
        //long[] mrangeArray
        f = KNI_GetFieldID(ch_class,"mrageArray","[J");
        SNI_NewArray(SNI_LONG_ARRAY, info.mrange_count*3, larray);
        for (i=0; i< info.mrange_count*3;++i){
          KNI_SetLongArrayElement(larray, i, (jlong) info.mranges[i]);
        }
        KNI_SetObjectField(ch_model,f,larray);
  }
  
  free_sensor_channel(&info); // safe even if failed (pointers are null)
  
  KNI_EndHandles();
  KNI_ReturnVoid();

}