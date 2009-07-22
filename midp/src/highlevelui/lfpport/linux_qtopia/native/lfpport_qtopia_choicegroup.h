
#include "lfpport_qtopia_textfield.h"

#ifndef _LFPPORT_QTOPIA_CHOICEGROUP_H_
#define _LFPPORT_QTOPIA_CHOICEGROUP_H_

#include <QListWidget>
#include <QList>
#include <QGroupBox>
#include <QPushButton>
#include <QVBoxLayout>
#include "lfpport_qtopia_item.h"
#include "lfpport_qtopia_textfield.h"
#include "lfpport_qtopia_stringimageitem.h"


#define EXCLUSIVE 1
#define MULTIPLE  2
#define IMPLICIT  3
#define POPUP     4
#define TEXT_WRAP_DEFAULT 0
#define TEXT_WRAP_ON      1
#define TEXT_WRAP_OFF     2
#define PREF_IMG_WIDTH    12
#define PREF_IMG_HEIGHT   12


class Choice: public JItem
{
    Q_OBJECT
    public:
        Choice(MidpItem *item, JForm *form);
        virtual ~Choice();
        virtual jboolean  j_event(QEvent *event);
        virtual MidpError getSelectedIndex(int *selectedIndex) = 0;
        virtual MidpError j_insert(int elementNum, const QString str,
                           QPixmap *img, bool selected) = 0;
        virtual void      setSelected(int selectedIndex, bool selected) = 0;
        virtual MidpError j_set(int elemNum, QString text, QPixmap *img, bool selected) = 0;
        virtual MidpError j_delete(int elemNum, int selectedIndex) = 0;
        virtual MidpError j_deleteAll() = 0;
        virtual MidpError j_setSeletedFlags(jboolean *selectedArray, int arrayLength) = 0;
        virtual MidpError j_getSeletedFlags(int *numSelected, jboolean *selectedArray, int arrayLength) = 0;
        virtual MidpError j_isSelected(jboolean *selected, int elemNum) = 0;
    private:
        int fitPolicy;
};




class ChoiceButtonGroup: public Choice
{
    Q_OBJECT
    public:
        ChoiceButtonGroup(MidpItem *item, JForm *form, bool isExlusive, QString title);
        ~ChoiceButtonGroup();
        void j_setLabel(const QString &text);
        MidpError j_insert(int elementNum, const QString str,
                           QPixmap *img, bool selected);
        MidpError j_delete(int elementNum, int selectedIndex);
        MidpError j_deleteAll();
        MidpError j_set(int elemNum, QString text, QPixmap *img, bool selected);
        MidpError j_isSelected(jboolean *selected, int elemNum);
        MidpError getSelectedIndex(int *selectedIndex);
        MidpError j_setSeletedFlags(jboolean *selectedArray, int arrayLength);
        MidpError j_getSeletedFlags(int *numSelected, jboolean *selectedArray, int arrayLength);
        void         setSelected(int selectedIndex, bool selected);
    private:
        QGroupBox *groupBox;
        QVBoxLayout *boxLayout;
        int ChoicesNum;
        int count;
        int currIndex;
        bool exclusive;
};

class List: public Choice
{
        Q_OBJECT
    public:
        List(MidpItem *item, JForm *form, QString title);
        ~List();
        void j_setLabel(const QString &text);
        MidpError j_insert(int elementNum, const QString str,
                               QPixmap *img, bool selected);
        MidpError j_set(int elemNum, QString text, QPixmap *img, bool selected);
        MidpError getSelectedIndex(int *selectedIndex);
        MidpError j_delete(int elemNum, int selectedIndex);
        void      setSelected(int selectedIndex, bool selected);
        MidpError j_setSeletedFlags(jboolean *selectedArray, int arrayLength);
        MidpError j_getSeletedFlags(int *numSelected, jboolean *selectedArray, int arrayLength);
        MidpError j_isSelected(jboolean *selected, int elemNum);
        MidpError j_deleteAll();
    private:
        QListWidget *listWidget;
        QLabel      *ls_label;
};

//class Popup: public Choice
//{
//    Q_OBJECT
//    public:
//        Popup(MidpItem *item, JForm *form, QString title);
//        ~Popup();
//        void j_setLabel(const QString);
//        MidpError j_insert(int elementNum, const QString str, QPixmap *img, bool selected);
//};

#endif // #ifndef _LFPPORT_QTOPIA_CHOICEGROUP_H_
