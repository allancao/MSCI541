import gzip
import re

HEADLINE_TAG_LENGTH = len('<HEADLINE>')

lines = [];
with gzip.open('latimes.gz', 'rb') as f:
    lines.append(f.readlines())

text = ''
for line in lines:
    text += line

begin = 0
headline_text = ''
while begin != -1:
    begin = text.find('<HEADLINE>')
    end = text.find('</HEADLINE>', begin, len(text))
    print begin
    print end
    if begin != -1 and end != -1:
        temp = text[begin, end]
        temp = re.sub('<.*?>', '', headline_text)
        headline_text = headline_text + ' ' + text

print headline_text

    # while True:
    #     chunk = f.read(10000)
    #     temp += chunk
    #     print chunk
    #     # if chunk == '<':
    #     #     break
    #     #
    #     if not chunk:
    #         # EOF reached, end loop
    #         break
    #     # chunk is up to 1000 characters long

# print len(temp)
