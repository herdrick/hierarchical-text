(ns hc)
(defn node [whatever]
  (defn leaf [rfo]
    (str "{id: 'document-node" (rand) "', name: '" 
	 (apply str (drop-last 2 (apply str (interesting rfo))))  ; drop 2 chars at the end to kill tailing comma and space
	 " : " (.replace (str (rfos-or-file rfo)) "'" "") "', data: {} , children: [] }")) 

  (defn pair [rfo]
    (str "{id: 'pairing-node" (rand) "', name: '" 
	 (apply str (drop-last 2 (apply str (interesting rfo)))) ; drop 2 chars at the end to kill tailing comma and space 
	 "', data: {score: '" (score rfo) "'}" 
	 " , children: [" 
	 (node (first (rfos-or-file rfo))) 
	 " , " 
	 (node (second (rfos-or-file rfo))) "] }"))
  
  (if (instance? java.io.File (rfos-or-file whatever))
    (leaf whatever)
    (pair whatever)))

(def *infovis-js-file* "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/visualize/Spacetree/example1.js")

(defn into-js-file [o]
  (spit *infovis-js-file* 
	(.replaceFirst (slurp *infovis-js-file*) 
		       "(?s)var json =.*;//end json data"    ;note regex flag to ignore line terminators. needed on some platforms but not all. Java is WODE!
		       (str "var json =" o ";//end json data"))))
