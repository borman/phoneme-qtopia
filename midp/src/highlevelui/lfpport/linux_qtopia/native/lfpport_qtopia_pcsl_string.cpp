#include "lfpport_qtopia_pcsl_string.h"

#include <midpMalloc.h>
#include <midpUtilKni.h>

QString pcsl_string2QString(const pcsl_string &pstring)
{
  /*
  * Example From the QT toolkit doc for QString.isNull()
  * QString a;          // a.unicode() == 0,  a.length() == 0
  * QString b = "";     // b.unicode() == "", b.length() == 0
  * a.isNull();         // TRUE, because a.unicode() == 0
  * a.isEmpty();        // TRUE, because a.length() == 0
  * b.isNull();         // FALSE, because b.unicode() != 0
  * b.isEmpty();        // TRUE, because b.length() == 0
  */
  QString qstring;
  if (pcsl_string_is_null(&pstring))
  {
    // we want isNull to be true
    qstring = QString::null;
  }
  else if (pcsl_string_length(&pstring) == 0)
  {
    // we want isEmpty to be true
    qstring = "";
  }
  else
  {
    const pcsl_string* const mmstring = &pstring;
    GET_PCSL_STRING_DATA_AND_LENGTH(mmstring)
    qstring.setUtf16((const ushort *)mmstring_data, mmstring_len);
    RELEASE_PCSL_STRING_DATA_AND_LENGTH
  }
  return qstring;
}

MidpError QString2pcsl_string(QString &qstring, pcsl_string &pstring)
{
  pcsl_string_status pe;
  if (qstring.isNull())
  {
    pstring = PCSL_STRING_NULL;
  }
  else if (qstring.isEmpty())
  {
    pstring = PCSL_STRING_EMPTY;
  }
  else
  {
    jint mstring_len = qstring.length();
    jchar* mstring_data = (jchar *)midpMalloc(sizeof(jchar) * mstring_len);
    if (mstring_data == NULL)
    {
      pstring = PCSL_STRING_NULL;
      return KNI_ENOMEM;
    }
    else
    {
      for (int i = 0; i < mstring_len; i++)
      {
        mstring_data[i] = qstring[i].unicode();
      }
      pe = pcsl_string_convert_from_utf16(mstring_data,
                                          mstring_len, &pstring);
      midpFree(mstring_data);
      if (PCSL_STRING_OK != pe)
      {
        return KNI_ENOMEM;
      }
    }
  }
  return KNI_OK;
}
