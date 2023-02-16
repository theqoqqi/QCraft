import leveldb
import sys
import re
import json
import codecs

pattern = re.compile("^@source(.*)$")

db = leveldb.LevelDB(sys.argv[1:][0])

for k,v in db.RangeIter():
    m = pattern.match(k)
    if m:
        name = re.sub("[\W\b]", "_", m.groups()[0].strip())
        full_name = "%s.user.js" % name

        print("Writing to %s" % full_name)

        content = json.JSONDecoder(encoding='UTF-8').decode(v)['value']

        with codecs.open(full_name, 'w', 'utf-8') as text_file:
            text_file.write(content)