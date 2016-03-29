#coding=utf-8
import Stemmer
import MySQLdb
ipAddr = '140.116.112.164'
userName = 'iim_project'
password = '1qaz2wsx3eDC'
dbName = 'iim_project'

### connect database
db = MySQLdb.connect(ipAddr, userName, password, dbName);
cursor = db.cursor()

### fetch words
cursor.execute('use iim_project')
cursor.execute('select word from ES2002a_grammer')

### retrieve words
rawWords = cursor.fetchall()
wordsList = []
for aWordTuple in rawWords:
  aWordList = list(aWordTuple)
  aWordStr = aWordList[0]
  wordsList.append(aWordStr)

### stemming the word
stemmer = Stemmer.Stemmer('english')
stemmedWords = []
for aWord in wordsList:
  stemmedStr = stemmer.stemWord(aWord)
  stemmedWords.append(stemmedStr)

### save to database
for index in range(len(stemmedWords)):
  cursor.execute("update ES2002a_grammer set stemmedWord = %s where id = %d", stemmedWords[index], index + 1)
  db.commit()

db.close()
