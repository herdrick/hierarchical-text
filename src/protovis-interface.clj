(ns hc)
(defn node [whatever]
  (defn leaf [rfo]
    (str "'" (.replace (str (rfos-or-file rfo)) "'" "") "'"  ; drop 2 chars at the end to kill tailing comma and space
	 " : 1")) 

  (defn pair [rfo]
    (str "'" (apply str (drop-last 2 (apply str (interesting rfo)))) "': {" 
	 (node (first (rfos-or-file rfo))) 
	 " , " 
	 (node (second (rfos-or-file rfo))) "}"))
  
  (if (instance? java.io.File (rfos-or-file whatever))
    (leaf whatever)
    (pair whatever)))

