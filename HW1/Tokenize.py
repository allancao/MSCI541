import gzip
import re
import time
import math
import csv

BEGIN_TAG_HEADLINE = '<HEADLINE>'
BEGIN_TAG_TEXT = '<TEXT>'
BEGIN_TAG_GRAPHIC = '<GRAPHIC>'

END_TAG_HEADLINE = '</HEADLINE>'
END_TAG_TEXT = '</TEXT>'
END_TAG_GRAPHIC = '</GRAPHIC>'

BEGIN_TAG_DOCNO = '<DOCNO>'
END_TAG_DOC = '</DOC>'

DATA = 'latimes.gz'


def find_all_docno():
    list = []
    with gzip.open(DATA, 'rb') as f:
        for line in f:
            tag = str(line, 'utf-8')
            if '<DOCNO>' in tag:
                docno = re.sub('<.*?>', ' ', tag).strip()
                list.append(docno)
        return list


def tokenize(text_to_tokenize, dictionary):
    temp = re.sub('[^a-zA-Z0-9]', ' ', text_to_tokenize)
    token_list = temp.split()

    if len(token_list) > 0:
        for i in token_list:
            if i in dictionary:
                dictionary[i] += 1
            else:
                dictionary[i] = 1

# Specific doc tf calculations, if need be
#
# def find_docno_text(docno, dictionary):
#     finished = False
#     with gzip.open(DATA, 'rb') as f:
#         while not finished:
#             tag = str(f.readline(), 'utf-8')
#             if docno in tag:
#                 tag = str(f.readline(), 'utf-8')
#                 while END_TAG_DOC not in tag:
#                     if BEGIN_TAG_HEADLINE in tag or BEGIN_TAG_GRAPHIC in tag or BEGIN_TAG_TEXT in tag:
#                         tag = str(f.readline(), 'utf-8')
#                         while END_TAG_HEADLINE not in tag and END_TAG_GRAPHIC not in tag and END_TAG_TEXT not in tag:
#                             legal_text = re.sub('<.*?>', ' ', tag)
#                             legal_text = legal_text.replace("""\\n""", ' ')
#                             tokenize(legal_text, dictionary)
#                             tag = str(f.readline(), 'utf-8')
#                     tag = str(f.readline(), 'utf-8')
#                 finished = True


def find_all_text():
    dictionary = {}
    with gzip.open(DATA, 'rb') as f:
        finished = False
        while not finished:
            tag = str(f.readline(), 'utf-8')

            # Check if docno in line, strip to retrieve docno for categorization
            if BEGIN_TAG_DOCNO in tag:
                docno = re.sub('<.*?>', ' ', tag).strip()
                docno_dict = {}
                tag = str(f.readline(), 'utf-8')

                # Finding docno signifies a document, much like <DOC> tag, continues until </DOC> found,
                # signifying end of document
                while END_TAG_DOC not in tag:

                    # Checks for valid tags to take text from while iterating through doc
                    if BEGIN_TAG_HEADLINE in tag or BEGIN_TAG_GRAPHIC in tag or BEGIN_TAG_TEXT in tag:
                        tag = str(f.readline(), 'utf-8')

                        # Each line is streamed, stripped of tags, cleaned up, etc.
                        # Cleaned text is then tokenized into docno specific dict
                        while END_TAG_HEADLINE not in tag and END_TAG_GRAPHIC not in tag and END_TAG_TEXT not in tag:
                            legal_text = re.sub('<.*?>', ' ', tag)
                            legal_text = legal_text.replace("""\\n""", ' ')
                            legal_text = legal_text.lower()
                            tokenize(legal_text, docno_dict)
                            tag = str(f.readline(), 'utf-8')
                    tag = str(f.readline(), 'utf-8')

                # Docno specific dict is compiled into one global dict
                dictionary[docno] = docno_dict
            if tag is '':
                finished = True
    return dictionary


def calculate_idf(docno, global_dict):
    total_doc = len(global_dict)
    docno_tf = global_dict[docno]
    docno_df = {}

    # Search for every occurence of docno specific term in all docnos
    for key_docno in docno_tf:
        for key_global in global_dict:
            if key_docno in global_dict[key_global]:
                if key_docno in docno_df:
                    docno_df[key_docno] += 1
                else:
                    docno_df[key_docno] = 1

    docno_idf = {}
    for key_docno_df in docno_df:
        docno_idf[key_docno_df] = math.log(total_doc/docno_df[key_docno_df])

    docno_tf_idf = {}
    for key_docno_idf in docno_idf:
        docno_tf_idf[key_docno_idf] = docno_tf[key_docno_idf] * docno_idf[key_docno_idf]

    table = [docno_tf, docno_df, docno_idf, docno_tf_idf]
    ret = {}
    for k in docno_tf:
        ret[k] = tuple(ret[k] for ret in table)

    return ret

docs = ['LA010189-0045', 'LA010189-0051', 'LA010189-0070', 'LA010189-0086']
global_dict = find_all_text()
for doc in docs:
    start_time = time.time()
    ret = calculate_idf(doc, global_dict)
    with open(doc + '_results.csv', 'w') as csvfile:

        writer = csv.writer(csvfile, lineterminator='\n')
        writer.writerow(('Term', 'TF', 'DF', 'IDF', 'TF*IDF'))
        for key in ret:
            split_value = str(ret[key])[1:-1].split(',')
            writer.writerow((key, split_value[0], split_value[1], split_value[2], split_value[3]))

    print("--- %s seconds ---" % (time.time() - start_time))



