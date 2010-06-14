(ns hc)
(defn node [whatever interesting-word-count all-files]
  (defn leaf [rfo]
    (str "'" (.replace (str rfo) "'" "") "'"  
	 " : 1")) 
  (defn pair [rfo]
    (str "'"
	 (apply str (.trim (apply str  
				  (map (fn [[word freq]]
					 (let [left-wrapper (if (< freq 0) "(" "")
					       right-wrapper (if (< freq 0) ")" "")]
					   (str left-wrapper (.trim word) right-wrapper " ")))
				       (take interesting-words-count (interesting-words rfo all-files)))))) 
	 "': {"   
	 (node (first rfo) interesting-words-count all-files)
	 " , " 
	 (node (second rfo) interesting-words-count all-files) "}"))
   
  (if (instance? java.io.File whatever)
    (leaf whatever)
    (pair whatever))) 

(def *protovis-json-file* "file:///Users/herdrick/Dropbox/clojure/hierarchical-classifier/visualize/protovis-3.2/flare.js")

(defn write-protovis-file [o]
  (spit *protovis-json-file* 
	(str "var flare = {" o "};")))


