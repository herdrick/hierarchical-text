(ns ordinary)
(defn pof->js [top-pof interesting-words-count]
  (let [all-files (flatten top-pof)
	corpus-freqs (freq-files all-files)]	
    (defn node [pof]
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
					   (take interesting-words-count (interesting-words pof corpus-freqs)))))) 
	     "': {"   
	     (node (first pof))
	     " , " 
	     (node (second pof)) "}"))
      
      (if (instance? java.io.File pof)
	(leaf pof)
	(pair pof)))
    
    (node top-pof)))

(def *protovis-json-file* "file:///Users/herdrick/Dropbox/clojure/hierarchical-classifier/visualize/protovis-3.2/flare.js")

(defn write-protovis-file [o]
  (spit *protovis-json-file* 
	(str "var flare = {" o "};")))

