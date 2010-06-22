(ns user)

(defn pof->js [top-pof interesting-words-count]
  (prn "pof->fs " top-pof)
  (defn node [pof]
    (defn leaf [pof]
      (prn "leaf " pof)
      (str "'" (.replace (str pof) "'" "") "'"  
	   " : 1")) 
    (defn pair [pof]
      (prn "pair " pof)
      (str "'"
	   (apply str (.trim (apply str  
				    (map (fn [[word freq]]
					   (let [left-wrapper (if (< freq 0) "(" "")
						 right-wrapper (if (< freq 0) ")" "")]
					     (str left-wrapper (.trim word) right-wrapper " ")))
					 (take interesting-words-count (interesting-words pof top-pof)))))) 
	   "': {"   
	   (node (first pof))
	   " , " 
	   (node (second pof)) "}"))
    (prn "node " pof)
    (if (instance? java.io.File pof)
      (leaf pof)
      (pair pof)))
  (node top-pof))
  
(def *protovis-json-file* "file:///Users/herdrick/Dropbox/clojure/hierarchical-classifier/visualize/protovis-3.2/flare.js")

(defn write-protovis-file [o]
  (spit *protovis-json-file* 
	(str "var flare = {" o "};")))


