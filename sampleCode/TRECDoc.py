
import re

def ExtractDocno( docstr ):
    docnostr = ExtractTagContents( docstr, "DOCNO" )
    if docnostr == '':
        raise Exception("missing docno")
    return docnostr.strip()

def ExtractTagContents( docstr, tag ):
    start = docstr.find("<" + tag + ">") 
    end = docstr.find( "</" + tag + ">" )
    if start == -1 or end == -1:
        return ''
    start += len(tag)+2
    return docstr[start:end]
    
# from http://www.calebthorne.com/python/2012/Jun/python-substitute-striptags
def StripTags( docstr ):
    return re.sub(r'<[^>]*?>', '', docstr)


