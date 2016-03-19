
from TRECReader import *
from TRECDoc import *
from Tokenize import *
import time

start = time.time()

tr = TRECReader()
tr.OpenFile("../latimes.gz" )

tokenDF = {} # keep track of document frequency

numDocs = 0 
while tr.HasNext():
    numDocs = numDocs + 1
    doc = tr.Next() # fetch the next doc
    docno = ExtractDocno( doc ) 

    tokens = [] 
    TokenizeStrings( [StripTags( ExtractTagContents( doc, "HEADLINE" ) ),
                      StripTags( ExtractTagContents( doc, "SUBJECT" ) ),
                      StripTags( ExtractTagContents( doc, "TEXT" ) ),
                      StripTags( ExtractTagContents( doc, "GRAPHIC" ) )] , tokens )
    docLength = len(tokens)

    # find out how many times each token occurs
    tokenTF = {}
    for token in tokens:
        if tokenTF.get(token) == None:
            tokenTF[token] = 1
        else:
            tokenTF[token] = tokenTF[token] + 1

    # need to record how many docs each token appears in
    for token in tokenTF:
        if tokenDF.get(token) == None:
            tokenDF[token] = 1
        else:
            tokenDF[token] = tokenDF[token] + 1

end = time.time()
print( 'Processing took {0} seconds.'.format(end - start) ) 
print( 'There are {0} docs in the collection.'.format(numDocs) )
print( 'There are {0} words in the vocabulary.'.format( len(tokenDF.keys()) ) )



    
    
