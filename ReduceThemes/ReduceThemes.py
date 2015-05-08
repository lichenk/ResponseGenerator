import csv
import nltk
from nltk.corpus import wordnet as wn
LIMIT = 10000000000000
def is_theme(word): # Is it a theme?
    return word[0].islower()

def is_noun(word):
    wordtype = nltk.pos_tag(nltk.word_tokenize(word))
    return wordtype[0][1] == "NN"

def getsim(word1, word2):
    try:
        w1 = wn.synset(word1+".n.01")
        w2 = wn.synset(word2+".n.01")
        return w1.path_similarity(w2)
    except nltk.corpus.reader.wordnet.WordNetError:
        return 0


def reduce_synonyms(word_list):
    return {}
    n = len(word_list)
    weights = []
    for i in xrange(0,n):
        for j in xrange(0,i):
            weights.append((getsim(word_list[i], word_list[j]),word_list[i], word_list[j]))
    weights.sort(reverse=True)
    d = {}
    for i in xrange(len(word_list)-LIMIT):
        #should prioritize word with higher frequency
        (_,x,y) = weights[i]
        if x not in d:
            if y in d:
                d[x] = d[y]
            else:
                d[x] = y
            word_list.remove(x)
        elif y not in d:
            if x in d:
                d[y] = d[x]
            else:
                d[y] = x
            word_list.remove(y)
    for (k,v) in d.items():
        d["(Reply theme) " + k] = "(Reply theme) " + v
    return d

def get_final_dict(reduced_dict):
    with open('EmailData.csv', 'rb') as input_csv:
        fieldnames = input_csv.readline().split(",")
        transform_dict = dict((k,v) for (v,k) in enumerate(fieldnames))
        for (field, idx) in transform_dict.iteritems():
            if field in reduced_dict:
                transform_dict[field] = transform_dict[reduced_dict[field]]
        final_dict = {}
        for (field, idx) in transform_dict.iteritems():
            if field not in final_dict:
                final_dict[field] = [idx]
            else:
                final_dict[field].append(idx)
    return (fieldnames, final_dict)

def make_csv(reduced_dict):
    (fieldnames, final_dict) = get_final_dict(reduced_dict)
    reduced_list = [field for field in fieldnames if field not in reduced_dict]
    with open("EmailData.csv","rb") as source:
        rdr= csv.reader(source)
        with open("Staged.csv","wb") as result:
            wtr= csv.writer(result)
            for r in rdr:
                row = []
                if r[final_dict['Has Reply'][0]] == 'false':
                    continue
                for field in reduced_list:
                    orig_idx = final_dict[field]
                    if (r[orig_idx[0]] == 'true' or r[orig_idx[0]] == 'false'):
                        value = any([('true' == r[idx]) for idx in orig_idx])
                    else:
                        value = r[orig_idx[0]]
                    row.append(str(value))
                wtr.writerow(row)
    with open("TestData.csv","rb") as source:
        rdr = csv.reader(source)
        with open("FinalTest.csv","wb") as result:
            wtr= csv.writer(result)
            for r in rdr:
                row = []
                if r[final_dict['Has Reply'][0]] == 'false':
                    continue
                for field in reduced_list:
                    orig_idx = final_dict[field]
                    if (r[orig_idx[0]] == 'true' or r[orig_idx[0]] == 'false'):
                        value = any([('true' == r[idx]) for idx in orig_idx])
                    else:
                        value = r[orig_idx[0]]
                    row.append(str(value))
                wtr.writerow(row)

def remove_unary():
    with open("Staged.csv","rb") as source:
        fieldnames = source.readline().split(',')
    hasFalse = [False] * len(fieldnames)
    hasTrue = [False] * len(fieldnames)
    with open("Staged.csv","rb") as source:
        rdr= csv.reader(source)
        for r in rdr:
            for (idx, val) in enumerate(r):
                hasFalse[idx] = hasFalse[idx] or (val == 'False')
                hasTrue[idx] = hasTrue[idx] or (val == 'True')
    keepidx = []
    for idx, name in enumerate(fieldnames):
        if ("(Reply theme) " not in name) or (hasFalse[idx] and hasTrue[idx]):
            keepidx.append(idx)
        else:
            print "Excluded:", name, hasFalse[idx], hasTrue[idx]
    with open("Staged.csv","rb") as source:
        rdr= csv.reader(source)
        with open("Final.csv","wb") as result:
            wtr= csv.writer(result)
            for r in rdr:
                row = [v for (i,v) in enumerate(r) if i in keepidx]
                wtr.writerow(row)


def main():
    with open('EmailData.csv', 'rb') as csvfile:
        fieldnames = csvfile.readline().split(",")
        theme_list = [word for word in fieldnames if is_theme(word)]
        noun_list = [word for word in theme_list if is_noun(word)]
        reduced_dict = reduce_synonyms(noun_list)
    make_csv(reduced_dict)
    remove_unary()
def init():
    nltk.download('maxent_treebank_pos_tagger')
    nltk.download('punkt')
    nltk.download('wordnet')
init()
main()
