
#include "lfpport_qtopia_pcsl_string.h"


#include "lfpport_qtopia_item.h"
#include <lfpport_choicegroup.h>
#include "lfpport_qtopia_choicegroup.h"
#include "lfpport_qtopia_debug.h"
#include <jdisplay.h>
#include <lfpport_form.h>
#include <jgraphics.h>
//#include <QDebug>
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
    JChoice *cg = NULL;
    switch(choiceType)
    {
    case EXCLUSIVE:
        default:
            cg = new JChoiceButtonGroup(cgPtr, disp->toForm(), true, pcsl_string2QString(*label));
            break;
    case MULTIPLE:
            cg = new JChoiceButtonGroup(cgPtr, disp->toForm(), false, pcsl_string2QString(*label));
            break;
    case IMPLICIT:
            cg = new JList(cgPtr, disp->toForm(), pcsl_string2QString(*label));
            break;
    case POPUP:
			cg = new JPopup(cgPtr, disp->toForm(), pcsl_string2QString(*label));
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
    JChoice *cg = static_cast<JChoice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    cg->j_insert(elementNum, pcsl_string2QString(element.string), JGraphics::immutablePixmap(element.image), element.selected);
    qDebug("lfpport_choicegroup_insert");
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_delete(MidpItem* cgPtr, int elementNum,
               int selectedIndex)
  {
    JChoice *cg = static_cast<JChoice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    cg->j_delete(elementNum, selectedIndex);
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_delete_all(MidpItem* cgPtr)
  {
    JChoice *cg = static_cast<JChoice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
	cg->j_deleteAll();
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_set(MidpItem* cgPtr,
            int elementNum,
            MidpChoiceGroupElement element)
  {
    JChoice *cg = static_cast<JChoice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    cg->j_set(elementNum, pcsl_string2QString(element.string), JGraphics::immutablePixmap(element.image), element.selected);
    qDebug() << "lfpport_choicegroup_set";
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_set_selected_index(MidpItem* cgPtr,
               int elementNum,
               jboolean selected)
  {
    qDebug() << "lfpport_choicegroup_set_sel_index";
    JChoice *cg = static_cast<JChoice *>(cgPtr->widgetPtr);
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
    JChoice *cg = static_cast<JChoice *>(cgPtr->widgetPtr);
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
    JChoice *cg = static_cast<JChoice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    cg->j_setSelectedFlags(selectedArray, selectedArrayNum);
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_get_selected_flags(int *numSelected,
               MidpItem* cgPtr,
                 jboolean* selectedArray_return,
               int selectedArrayLength)
  {
    JChoice *cg = static_cast<JChoice *>(cgPtr->widgetPtr);
    if(!cg)
    {
        return KNI_ENOMEM;
    }
    cg->j_getSelectedFlags(numSelected, selectedArray_return, selectedArrayLength);
    return KNI_OK;
  }

  MidpError lfpport_choicegroup_is_selected(jboolean *selected, MidpItem* cgPtr,
              int elementNum)
  {
    JChoice *cg = static_cast<JChoice *>(cgPtr->widgetPtr);
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

JChoice::JChoice(MidpItem *item, JForm *form)
        : JItem(item, form)
{
}

JChoice::~JChoice()
{
}


jboolean JChoice::j_event(QEvent *event)
{
    (void)event;
    return KNI_OK;
}




JChoiceButtonGroup::JChoiceButtonGroup(MidpItem *item, JForm *form, bool isExlusive, QString title)
                                    :JChoice(item, form)
{
    groupBox = new QGroupBox(this);
	flayout = new QVBoxLayout(this);
    boxLayout = new QVBoxLayout(groupBox);
	flayout->addWidget(groupBox);
    groupBox->setLayout(boxLayout);
    j_setLabel(title);
	//setLayout(flayout);
    exclusive = isExlusive;
    count = 0;
}

JChoiceButtonGroup::~JChoiceButtonGroup()
{
}


void JChoiceButtonGroup::j_setLabel(const QString &text)
{
    groupBox->setTitle(text);
}

MidpError JChoiceButtonGroup::j_insert(int elementNum, const QString str,
                                      QPixmap *img, bool selected)
{
    QAbstractButton *btn = NULL;
    qDebug("Choice::j_insert");
	if(exclusive)
    {
        btn = new QRadioButton;
		qDebug("QRadioButton");
    }
    else
    {
        btn = new QCheckBox;
		qDebug("QCheckBox");
    }
    if(!str.isNull())
    {
		qDebug() << "Set text: " << str ;
        btn->setText(str);	
    }
    if(img != NULL)
    {
		qDebug("Set icon");
        btn->setIcon(QIcon(*img));
    }
	qDebug("Button created");
	qDebug("Setup button");
    btn->setChecked(selected);
    QString objName;
    objName.setNum(elementNum, 10);
    btn->setObjectName(objName);
    boxLayout->addWidget(btn);
	qDebug() << "insert button";
	++count;
	connect(btn, SIGNAL(clicked()), this, SLOT(selectedButton()));
    return KNI_OK;
}

MidpError JChoiceButtonGroup::j_set(int elemNum, QString text, QPixmap *img, bool selected)
{
    QString objName;
    objName.setNum(elemNum, 10);
    QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
    if(!btn)
    {
        return KNI_ENOMEM;
    }
    else
    {
        btn->setChecked(selected);
		if(img != NULL)
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

MidpError JChoiceButtonGroup::j_delete(int elementNum, int selectedIndex)
{
    QString objName;
	objName.setNum(elementNum, 10);
    QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
    btn->~QAbstractButton();
    --count;
	if(elementNum != (count-1))
	{
		for(int i = (elementNum + 1); i < count; ++i)
		{
			objName.setNum(i, 10);
			btn = groupBox->findChild<QAbstractButton *>(objName);
			btn->setObjectName(objName.setNum((i-1), 10));
			if((i-1) == selectedIndex)
			{
				btn->setChecked(true);
			}
		}
	}
    return KNI_OK;
}

MidpError JChoiceButtonGroup::j_deleteAll()
{
    for(int i = count; i >= 0; i--)
    {
        j_delete(i, 0);
    }
    return KNI_OK;
}

MidpError JChoiceButtonGroup::j_isSelected(jboolean *selected, int elemNum)
{
		QString objName;
		objName.setNum(elemNum, 10);
        QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
        *selected = btn->isChecked();
        return KNI_OK;
}

MidpError JChoiceButtonGroup::getSelectedIndex(int *selectedIndex)
{
    *selectedIndex = -1;
    for(int i = 0; i < count; ++i)
	{
		QString objName;
		objName.setNum( i, 10);
		QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
		if(btn == NULL)
		{
			return KNI_OK;
		}
		if(btn->isChecked())
		{
			qDebug() << "getSelectedIndex():" << i;
			*selectedIndex = i;
		}
	}
    return KNI_OK;
}

MidpError JChoiceButtonGroup::j_getSelectedFlags(int *numSelected, jboolean *selectedArray, int arrayLength)
{
	*numSelected = 0;
	if(exclusive)
	{
		for(int i = 0; i < count; i++)
		{
			QString objName;
			objName.setNum(i, 10);
			QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
			if(btn == NULL)
			{
				return KNI_ENOMEM;
			}
			if(btn->isChecked())
			{
				selectedArray[i] = true;
				*numSelected = 1;
			}
			else
			{
				selectedArray[i] = false;
			}
		}
	}
	else
	{
		for(int i = 0; i < count; i++)
		{
			QString objName;
			objName.setNum(i, 10);
			QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
			if(btn == NULL)
			{
				return KNI_ENOMEM;
			}
			selectedArray[i] = btn->isChecked();
			if(selectedArray[i])
			{
				*numSelected++;
			}
		}
	}
	for(int i = count; i < arrayLength; i++)
	{
		selectedArray[i] = false;
	}
    return KNI_OK;
}

MidpError JChoiceButtonGroup::j_setSelectedFlags(jboolean *selectedArray, int arrayLength)
{
    (void)arrayLength;
    for(int i = 0; i <= count; i++)
	{ 
        QString objName;
		objName.setNum(i, 10);
        QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
		if(exclusive)
		{
			if(selectedArray[i])
			{
				setSelected(i, true);
				return KNI_OK;
			}
		}
		else
		{
			btn->setChecked(selectedArray[i]);
		}
    }
    return KNI_OK;
}


void JChoiceButtonGroup::setSelected(int selectedIndex, bool selected)
{
	QString objName;
	objName.setNum( selectedIndex, 10);
    QAbstractButton *btn = groupBox->findChild<QAbstractButton *>(objName);
    btn->setChecked(selected);
}

void JChoiceButtonGroup::focusInEvent(QFocusEvent *event)
{
	MidpFormFocusChanged(this);
	qDebug() << "JChoiceButtonGroup: focus changed";
}

void JChoiceButtonGroup::selectedButton()
{
	QAbstractButton *btn = static_cast<QAbstractButton *>(QObject::sender());
	if(btn != NULL)
	{
		QString objName;
		objName = btn->objectName();
		int i = objName.toInt();
		MidpFormItemPeerStateChanged(this, i);
	}
}

//JChoiceButtonGroup widget end
//------------------------------------------------------------------------------


//------------------------------------------------------------------------------
//List widget start
JList::JList(MidpItem *item, JForm *form, QString title)
        :JChoice(item, form)
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
	connect(listWidget, SIGNAL(currentRowChanged(int)), this, SLOT(selectedRow(int)));
}

JList::~JList()
{
   disconnect(listWidget, SIGNAL(currentRowChanged(int)), this, SLOT(selectedRow(int))); 
}

void JList::j_setLabel(const QString &text)
{
    if(ls_label)
    {
        ls_label->setText(text);
    }
}

MidpError JList::j_insert(int elementNum, const QString str, QPixmap* img, bool selected)
{
     QListWidgetItem *listItem = new QListWidgetItem(listWidget);
    if(!str.isNull())
    {
        listItem->setText(str);
        qDebug() << str;
    }
//  if(!img->isNull())
//  {
//      listItem->setIcon(QIcon(*img));
//  }
    listItem->setSelected(selected);
    listWidget->insertItem(elementNum, listItem);
    return KNI_OK;
}


MidpError JList::j_set(int elemNum, QString text, QPixmap *img, bool selected)
{
    listWidget->takeItem(elemNum);
    QListWidgetItem *listItem = new QListWidgetItem(listWidget);
    if(!text.isNull())
    {
        listItem->setText(text);
    }
//  if(!img->isNull())
//  {
//      listItem->setIcon(QIcon(*img));
//  }
    listItem->setSelected(selected);
    listWidget->insertItem(elemNum, listItem);
    qDebug() << "List j_set";
    return KNI_OK;
}

MidpError JList::getSelectedIndex(int *selectedIndex)
{
	*selectedIndex = listWidget->currentRow();
    return KNI_OK;
}

void JList::setSelected(int selectedIndex, bool selected)
{
    listWidget->setCurrentRow(selectedIndex);
    listWidget->currentItem()->setSelected(selected);
}

MidpError JList::j_delete(int elemNum, int selectedIndex)
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

MidpError JList::j_deleteAll()
{
    for(int i = (listWidget->count()-1); i >= 0; i--)
    {
        j_delete(i, 0);
    }
    return KNI_OK;
}

MidpError JList::j_setSelectedFlags(jboolean *selectedArray, int arrayLength)
{
    (void)arrayLength;
    int n = listWidget->count();
    for(int i = 0; i < n; i++)
    {
        setSelected(i, selectedArray[i]);
    }
    return KNI_OK;
}
MidpError JList::j_getSelectedFlags(int *numSelected, jboolean *selectedArray, int arrayLength)
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

MidpError JList::j_isSelected(jboolean *selected, int elemNum)
{
    listWidget->setCurrentRow(elemNum);
    *selected = listWidget->currentItem()->isSelected();
    return KNI_OK;
}

void JList::focusInEvent(QFocusEvent *event)
{
	MidpFormFocusChanged(this);
	qDebug() << "JList: focus changed" ;
}

void JList::selectedRow(int elemNum)
{
	qDebug() << "Selected row: " << listWidget->currentRow();
	MidpFormItemPeerStateChanged(this, elemNum);
}

//list widget end
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
//popup widget start
JPopup::JPopup(MidpItem *item, JForm *form, QString title)
    :JChoice(item, form)
{
	itemCount = 0;
	popup = new QComboBox(this);
	QVBoxLayout *layout = new QVBoxLayout(this);
	layout->addWidget(popup);
	connect(popup, SIGNAL(currentIndexChanged(int)), this, SLOT(elementSelected(int)));
}

JPopup::~JPopup()
{
	popup->hide();
	disconnect(popup, SIGNAL(currentIndexChanged(int)), this, SLOT(selectedIndex(int)));
}

void JPopup::j_setLabel(const QString &text)
{

}

MidpError JPopup::j_insert(int elementNum, const QString str, QPixmap *img, bool selected)
{
	popup->insertItem(itemCount, str);
	if(img != NULL)
	{
		popup->setItemIcon(itemCount, QIcon(*img));
	}
	++itemCount;
	return KNI_OK;
}

MidpError JPopup::j_set(int elemNum, QString text, QPixmap *img, bool selected)
{
	if(!text.isNull())
	{
		popup->setItemText(elemNum, text);
	}
	if(img != NULL)
    {
	  	popup->setItemIcon(elemNum, QIcon(*img));
	}
	if(selected)
	{
		popup->setCurrentIndex(elemNum);
	}
	return KNI_OK;
}

MidpError JPopup::getSelectedIndex(int *selectedIndex)
{
	*selectedIndex = popup->currentIndex();
	return KNI_OK;
}

void JPopup::setSelected(int selectedIndex, bool selected)
{
	if(selected)
	{
		popup->setCurrentIndex(selectedIndex);
	}
}

MidpError JPopup::j_delete(int elemNum, int selectedIndex)
{
	popup->setCurrentIndex(selectedIndex);
	popup->removeItem(elemNum);
	return KNI_OK;
}

MidpError JPopup::j_deleteAll()
{
	int n = (popup->count() - 1);
	for(int i = n; i >= 0; i++)
	{
		j_delete(i,0);
	}
	return KNI_OK;
}

MidpError JPopup::j_setSelectedFlags(jboolean *selectedArray, int arrayLength)
{
	(void)arrayLength;
	for(int i = 0; i < popup->count(); i++)
	{
		if(selectedArray[i])
		{
			setSelected(i, selectedArray[i]);
			return KNI_OK;
		}
	}
	setSelected(0, true);
	return KNI_OK;
}

MidpError JPopup::j_getSelectedFlags(int *numSelected, jboolean *selectedArray, int arrayLength)
{
	for(int i = 0; i < arrayLength; i++)
	{
		selectedArray[i] = false;
	}
	if(popup->currentIndex() > 0)
	{
		selectedArray[popup->currentIndex()] = true;
		*numSelected = 1;
	}
	else
	{
		*numSelected = 0;
	}
	return KNI_OK;
}

MidpError JPopup::j_isSelected(jboolean *selected, int elemNum)
{
	*selected = (popup->currentIndex() == elemNum) ? true : false;
	return KNI_OK;
}

void JPopup::focusInEvent(QFocusEvent *event)
{
		MidpFormFocusChanged(this);
		qDebug() << "JPopup: focus changed";
}

void JPopup::elementSelected(int elemNum)
{
	qDebug() << "Selected element number: " << elemNum;
	MidpFormItemPeerStateChanged(this, elemNum);
}

//popup widget end
//------------------------------------------------------------------------------

#include "lfpport_qtopia_choicegroup.moc"
#include "midp_global_status.h"

