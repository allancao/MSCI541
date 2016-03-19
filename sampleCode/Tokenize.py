
# This function takes a string and breaks it up into "words".  
# It returns an array of these words.
#
# Based on SimpleTokenizer by Trevor Strohman, 
# http://www.galagosearch.org/
#
# text is the string to tokenize and tokens is a list to which tokens will be appended
def Tokenize( text, tokens ):
    text = text.lower() 

    start = 0 
    i = 0

    for currChar in text:
        if not currChar.isdigit() and not currChar.isalpha() :
            if start != i :
                token = text[start:i]
                tokens.append( token )
                
            start = i + 1

        i = i + 1

    if start != i :
        tokens.append(text[start:i])

def TokenizeStrings( strings, tokens ):
    for string in strings:
        Tokenize( string, tokens )


    
    
