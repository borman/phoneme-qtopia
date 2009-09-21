
#include "lfpport_qtopia_pcsl_string.h"


#include "lfpport_qtopia_item.h"
#include <lfpport_choicegroup.h>
#include "lfpport_qtopia_choicegroup.h"
#include "lfpport_qtopia_debug.h"
#include <jdisplay.h>
#include <jimmutableimage.h>
#include <QDebug>
#include <QLabel>
#include <QListWidget>
#include <QListWidgetItem>
#include <QRadioButton>
#include <QCheckBox>

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
    debug_trace();

    JDisplayable *disp = static_cast<JDisplayable *>(ownerPtr->frame.widgetPtr);
    if(disp == NULL)
    {
        return KNI_ENOMEM;
    }
    qDebug() << "Create cg:" << choiceType;
    Choice *cg = NULL;
    switch(choiceType)
    {
    case EXCLUSIVE:
        default:
            cg = new ChoiceButtonGroup(cgPtr, disp->toForm(), true, pcsl_string2QString(*label));
            break;
    case MULTIPLE:
            cg = new ChoiceButtonGroup(cgPtr, disp->toForm(), false, pcsl_string2QString(*label));
            break;
    case IMPLICIT:
            cg = new List(cgPtr, disp->toForm(), pcsl_string2QString(*label));
            break;
    case POPUP:
            break;
    }
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    for(int i = 0; i < numOfChoices; i++)
    {
        QString qlabel;
        lfpport_choicegroup_insert(cgPtr, i, choices[i]);
    }
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_insert(MidpItem* cgPtr,
               int elementNum,
               MidpChoiceGroupElement element)
  {

    Choice *cg = static_cast<Choice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    qDebug() << pcsl_string2QString(element.string);
    cg->j_insert(elementNum, 
                 pcsl_string2QString(element.string), 
                 JIMMutableImage::fromHandle(element.image), 
                 element.selected);
    qDebug() << "lfpport_choicegroup_insert";
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_delete(MidpItem* cgPtr, int elementNum,
               int selectedIndex)
  {
    Choice *cg = static_cast<Choice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    cg->j_delete(elementNum, selectedIndex);
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_delete_all(MidpItem* cgPtr)
  {
    Choice *cg = static_cast<Choice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_set(MidpItem* cgPtr,
            int elementNum,
            MidpChoiceGroupElement element)
  {
    Choice *cg = static_cast<Choice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    cg->j_set(elementNum, 
              pcsl_string2QString(element.string), 
              JIMMutableImage::fromHandle(element.image), 
              element.selected);
    qDebug() << "lfpport_choicegroup_set";
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_set_selected_index(MidpItem* cgPtr,
               int elementNum,
               jboolean selected)
  {
    qDebug() << "lfpport_choicegroup_set_sel_index";
    Choice *cg = static_cast<Choice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    cg->setSelected(elementNum, selected);
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_get_selected_index(int* elementNum,
               MidpItem* cgPtr)
  {

    Choice *cg = static_cast<Choice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    cg->getSelectedIndex(elementNum);
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_set_selected_flags(MidpItem* cgPtr,
               jboolean* selectedArray,
               int selectedArrayNum)
  {
    Choice *cg = static_cast<Choice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    cg->j_setSeletedFlags(selectedArray, selectedArrayNum);
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_get_selected_flags(int *numSelected,
               MidpItem* cgPtr,
                 jboolean* selectedArray_return,
               int selectedArrayLength)
  {
    Choice *cg = static_cast<Choice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    cg->j_getSeletedFlags(numSelected, selectedArray_return, selectedArrayLength);
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_is_selected(jboolean *selected, MidpItem* cgPtr,
              int elementNum)
  {
    Choice *cg = static_cast<Choice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    return cg->j_isSelected(selected, elementNum);
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

Choice::Choice(MidpItem *item, JForm *form)
        : JItem(item, form)
{
}

Choice::~Choice()
{
}


jboolean Choice::j_event(QEvent *event)
{
    (void)event;
    return KNI_OK;
}




ChoiceButtonGroup::ChoiceButtonGroup(MidpItem *item, JForm *form, bool isExlusive, QString title)
                                    :Choice(item, form)
{
    groupBox = new QGroupBox(this);
    boxLayout = new QVBoxLayout(this);
    groupBox->setLayout(boxLayout);
    j_setLabel(title);
    exclusive = isExlusive;
    count = 0;
}

ChoiceButtonGroup::~ChoiceButtonGroup()
{
}


void ChoiceButtonGroup::j_setLabel(const QString &text)
{
    groupBox->setTitle(text);
}

MidpError ChoiceButtonGroup::j_insert(int elementNum, const QString str,
                                      QPixmap *img, bool selected)
{
    QAbstractButton *btn = NULL;
    if(exclusive)
    {
        btn = new QRadioButton(groupBox);
    }
    else
    {
        btn = new QCheckBox(groupBox);
    }
    if(!str.isNull())
    {
        btn->setText(str);
    }
    if(!img->isNull())
    {
        btn->setIcon(QIcon(*img));
    }
    btn->setChecked(selected);
    QString objName;
    objName.setNum(elementNum, 10);
    objName = "btn" + objName;
    btn->setObjectName(objName);
    boxLayout->addWidget(btn);
    ++count;
    return KNI_OK;
}

MidpError ChoiceButtonGroup::j_set(int elemNum, QString text, QPixmap *img, bool selected)
{
    QString objName;
    objName = "btn" + objName.setNum(elemNum, 10);
    QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
    if(!btn)
    {
        return KNI_ENOMEM;
    }
    else
    {
        btn->setChecked(selected);
        if(!img->isNull())
        {
            btn->setIcon(QIcon(*img));
        }
        if(!text.isNull())
        {
            btn->setText(text);
        }
        return KNI_OK;
    }
}

MidpError ChoiceButtonGroup::j_delete(int elementNum, int selectedIndex)
{
    QString objName;
    objName = "btn" + objName.setNum(elementNum, 10);
    QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
    btn->~QAbstractButton();
    --count;
    return KNI_OK;
}

MidpError ChoiceButtonGroup::j_deleteAll()
{
    for(int i = count; i >= 0; i--)
    {
        j_delete(i, 0);
    }
    return KNI_OK;
}

MidpError ChoiceButtonGroup::j_isSelected(jboolean *selected, int elemNum)
{
//    if(exclusive)
//    {
        QString objName;
        objName = "btn" + objName.setNum(elemNum, 10);
//        QRadioButton *btn = groupBox->findChild<QRadioButton *>(objName);
        QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
        *selected = btn->isChecked();
        return KNI_OK;
//    }
//    else
//    {
//
//    }
}

MidpError ChoiceButtonGroup::getSelectedIndex(int *selectedIndex)
{

    *selectedIndex = 0;
    bool selected = false;

    return KNI_OK;
}

MidpError ChoiceButtonGroup::j_getSeletedFlags(int *numSelected, jboolean *selectedArray, int arrayLength)
{
    *numSelected = 0;
    for (int n = 0; n < arrayLength; n++)
    {
        selectedArray[n] = false;
    }
    for(int i = 0; i <= count; i++)
    {   
        QString objName;
        objName = "btn" + objName.setNum( i, 10);
        QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
        if(!btn)
        {
            btn->setChecked(selectedArray[i]);
        }
    }
    return KNI_OK;
}

MidpError ChoiceButtonGroup::j_setSeletedFlags(jboolean *selectedArray, int arrayLength)
{
    (void)arrayLength;
    for(int i = 0; i <= count; i++)
    {
        QString objName;
        objName = "btn" + objName.setNum( i, 10);
        QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
        selectedArray[i] = btn->isChecked();
    }
    return KNI_OK;
}


void ChoiceButtonGroup::setSelected(int selectedIndex, bool selected)
{
        QString objName;
        objName = "btn" + objName.setNum( selectedIndex, 10);
        QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
        btn->setChecked(selected);
}
//------------------------------------------------------------------------------
//List widget start
List::List(MidpItem *item, JForm *form, QString title)
        :Choice(item, form)
{
    qDebug() << title;
    listWidget = new QListWidget(this);
    listWidget->setSelectionMode(QAbstractItemView::SingleSelection);
    QVBoxLayout *layout = new QVBoxLayout(this);
    if(!title.isNull())
    {
        ls_label = new QLabel(title, this);
        layout->addWidget(ls_label);
    }
    layout->addWidget(listWidget);
}

List::~List()
{
    
}

void List::j_setLabel(const QString &text)
{
    if(ls_label)
    {
        ls_label->setText(text);
    }
}

MidpError List::j_insert(int elementNum, const QString str, QPixmap* img, bool selected)
{
    
    QListWidgetItem *listItem = new QListWidgetItem(listWidget);
    if(!str.isNull())
    {
        listItem->setText(str);
        qDebug() << str;
    }
    if(!img->isNull())
    {
        listItem->setIcon(QIcon(*img));
    }
    listItem->setSelected(selected);
    listWidget->insertItem(elementNum, listItem);
    return KNI_OK;
}

MidpError List::j_set(int elemNum, QString text, QPixmap *img, bool selected)
{
    listWidget->takeItem(elemNum);
    QListWidgetItem *listItem = new QListWidgetItem(listWidget);
    if(!text.isNull())
    {
        listItem->setText(text);
    }
    if(!img->isNull())
    {
        listItem->setIcon(QIcon(*img));
    }
    listItem->setSelected(selected);
    listWidget->insertItem(elemNum, listItem);
    qDebug() << "List j_set";
    return KNI_OK;
}

MidpError List::getSelectedIndex(int *selectedIndex)
{
    *selectedIndex = 0;
    bool selected = false;
    while(*selectedIndex < listWidget->count()){
        
        listWidget->setCurrentRow(*selectedIndex);
        selected = listWidget->currentItem()->isSelected();
        if(selected)
        {
            qDebug() << "Yahooooo " << *selectedIndex;
            return KNI_OK;
        }
        *selectedIndex++;
      
    };
    return KNI_OK;
}

void List::setSelected(int selectedIndex, bool selected)
{
    listWidget->setCurrentRow(selectedIndex);
    listWidget->currentItem()->setSelected(selected);
}

MidpError List::j_delete(int elemNum, int selectedIndex)
{
    listWidget->takeItem(elemNum);
    if((selectedIndex > elemNum)&&(selectedIndex < listWidget->count()))
    {
        listWidget->setCurrentRow(selectedIndex-1);
    }
    else
    {
        listWidget->setCurrentRow(selectedIndex);
    }
    listWidget->currentItem()->setSelected(true);
    return KNI_OK;
}

MidpError List::j_deleteAll()
{
    for(int i = (listWidget->count()-1); i >= 0; i--)
    {
        j_delete(i, 0);
    }
    return KNI_OK;
}

MidpError List::j_setSeletedFlags(jboolean *selectedArray, int arrayLength)
{
    (void)arrayLength;
    int n = listWidget->count();
    for(int i = 0; i < n; i++)
    {
        setSelected(i, selectedArray[i]);
    }
    return KNI_OK;
}
MidpError List::j_getSeletedFlags(int *numSelected, jboolean *selectedArray, int arrayLength)
{
  int n;
  int curRow = listWidget->currentRow();
  for (n = 0; n < arrayLength; n++) {
    selectedArray[n] = false;
  }
  n = listWidget->count();
  for(int i = 0; i < n; i++ )
  {
      listWidget->setCurrentRow(i);
      if (listWidget->currentItem()->isSelected())
      {
        selectedArray[n] = true;
        *numSelected = 1;
      } else {
        *numSelected = 0;
      }
  }
  listWidget->setCurrentRow(curRow);
  return KNI_OK;
}

MidpError List::j_isSelected(jboolean *selected, int elemNum)
{
    listWidget->setCurrentRow(elemNum);
    *selected = listWidget->currentItem()->isSelected();
    return KNI_OK;
}
//list widget end
//------------------------------------------------------------------------------



#include "lfpport_qtopia_choicegroup.moc"
#include "midp_global_status.h"
