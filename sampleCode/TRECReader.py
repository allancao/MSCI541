
import gzip

class TRECReader:

    gzipfile = None
    currDoc = None

    def OpenFile( self, gzipd_TREC_file):
        self.gzipfile = gzip.open( gzipd_TREC_file, 'rt' )

    def CloseFile(self):
        gzip.close(self.gzipfile)

    def Next(self):
        result = self.currDoc
        self.currDoc = None # set to None to note that "next" doc is gone
        return result 

    def HasNext(self):
        # if we have a currDoc, then it is still true that next exists
        if self.currDoc != None:
            return True

        # okay, since the current doc is None, go and see if we can get another
        str_list = []
        insideDoc = False ;
        for line in self.gzipfile:
            if line.find( "<DOC>" ) == 0:
                insideDoc = True
            elif line.find( "</DOC>" ) == 0:
                insideDoc = False
                str_list.append(line)
                break

            if insideDoc:
                str_list.append(line)

        if ( len( str_list ) > 0 ):
            self.currDoc = ''.join(str_list)
            return True 
        else:
            return False
        





     
