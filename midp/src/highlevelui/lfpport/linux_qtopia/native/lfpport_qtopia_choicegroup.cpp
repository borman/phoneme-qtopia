#include <lfpport_choicegroup.h>

#include "lfpport_qtopia_choicegroup.h"


extern "C"
{
  MidpError lfpport_choicegroup_create(MidpItem* cgPtr, 
               MidpDisplayable* ownerPtr,
               const pcsl_string* label, int layout, 
               MidpComponentType choiceType,
               MidpChoiceGroupElement* choices, 
               int numOfChoices,
               int selectedIndex, 
               int fitPolicy)
  {
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_insert(MidpItem* cgPtr,
               int elementNum, 
               MidpChoiceGroupElement element)
  {
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_delete(MidpItem* cgPtr, int elementNum, 
               int selectedIndex)
  {
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_delete_all(MidpItem* cgPtr)
  {
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_set(MidpItem* cgPtr,
            int elementNum, 
            MidpChoiceGroupElement element)
  {
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_set_selected_index(MidpItem* cgPtr,
               int elementNum, 
               jboolean selected)
  {
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_get_selected_index(int* elementNum, 
               MidpItem* cgPtr)
  {
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_set_selected_flags(MidpItem* cgPtr, 
               jboolean* selectedArray,
               int selectedArrayNum)
  {
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_get_selected_flags(int *numSelected,
               MidpItem* cgPtr,
                 jboolean* selectedArray_return,
               int selectedArrayLength)
  {
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_is_selected(jboolean *selected, MidpItem* cgPtr, 
              int elementNum)
  {
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_set_fit_policy(MidpItem* cgPtr, int fitPolicy)
  {
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_set_font(MidpItem* cgPtr, 
                 int elementNum, 
                 PlatformFontPtr fontPtr)
  {
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_dismiss_popup()
  {
    return KNI_OK;
  }
}

#include "lfpport_qtopia_choicegroup.moc"
