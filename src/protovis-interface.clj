(ns hc)
(defn node [whatever]
  (defn leaf [rfo]
    (str "'" (.replace (str (rfos-or-file rfo)) "'" "") "'"  
	 " : 1")) 
  (defn pair [rfo]
    (str "'"
	 (apply str (drop-last 1 (apply str  ; drop 1 char at the end to kill tailing space
					(map (fn [[word freq]]
					       (str (.trim word) ":" (.substring (.replace (.trim (str freq)) "-" "#") 0 (if (.contains (str freq) "-") 6 5)) " "))  ;display first 6 chars of floating point number
					     (interesting rfo)))))
	 "': {"   
	 (node (first (rfos-or-file rfo))) 
	 " , " 
	 (node (second (rfos-or-file rfo))) "}"))
  
  (if (instance? java.io.File (rfos-or-file whatever))
    (leaf whatever)
    (pair whatever)))

(def *protovis-json-file* "file:///Users/herdrick/Dropbox/clojure/hierarchical-classifier/visualize/protovis-3.2/flare.js")

(defn write-protovis-file [o]
  (spit *protovis-json-file* 
	(str "var flare = {" o "};")))


