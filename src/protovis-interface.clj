(ns user)
(defn node [whatever interesting-words-count word-idx]
  (defn leaf [pof]
    (str "'" (.replace (str pof) "'" "") "'"  
	 " : 1")) 
  (defn pair [pof]
    (str "'"
	 (apply str (.trim (apply str  
				  (map (fn [[word freq]]
					 (let [left-wrapper (if (< freq 0) "(" "")
					       right-wrapper (if (< freq 0) ")" "")]
					   (str left-wrapper (.trim word) right-wrapper " ")))
				       (take interesting-words-count (interesting-words pof word-idx)))))) 
	 "': {"   
	 (node (first pof) interesting-words-count word-idx)
	 " , " 
	 (node (second pof) interesting-words-count word-idx) "}"))
  
  (if (instance? java.io.File whatever)
    (leaf whatever)
    (pair whatever))) 

(def *protovis-json-file* "file:///Users/herdrick/Dropbox/clojure/hierarchical-classifier/visualize/protovis-3.2/flare.js")

(defn write-protovis-file [o]
  (spit *protovis-json-file* 
	(str "var flare = {" o "};")))


