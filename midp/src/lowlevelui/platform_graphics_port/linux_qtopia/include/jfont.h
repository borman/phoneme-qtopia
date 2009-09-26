#ifndef _JFONT_H_
#define _JFONT_H_

#include <QFont>
#include <QFontMetrics>

#define JFONT_CACHE_SIZE 72 // (8 styles)*(3 faces)*(3 sizes) 

// Provides simplified conversion between MIDP and Qt font specification and font caching
class JFont: public QFont
{
  public:
    static JFont *find(int face, int style, int size);
    static void clearCache();
    
    inline const QFontMetrics *fontMetrics() const { return &metrics; }
  private:
    JFont(int face, int style, int size);
    virtual ~JFont();
    QFontMetrics metrics; // We always need the font to fit screen DPI
    
    // Caching
    static int attrs2id(int face, int style, int size); // Converts bit flags to (8, 3, 3)-based number
    
    int id;    
    static JFont *cache[JFONT_CACHE_SIZE]; // NOTE: hope it is initialised with 0...
};

#endif // _JFONT_H_
