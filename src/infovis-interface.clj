(defn node [whatever]
  (defn leaf [rfo]
    ;(println (interesting rfo))
    (str "{id: 'leafnode" (rand) "', name: '" (apply str (drop-last 2 (apply str (interesting rfo)))) ":" (.replace (str (rfos-or-file rfo)) "'" "") "', data: {} , children: [] }")) ; (.replace (str (rfos-or-file rfo)) "'" "") 

  (defn pair [rfo]
    (str "{id: 'pairnode" (rand) "', name: '" 
	 (apply str (drop-last 2 (apply str (interesting rfo)))) ; drop 2 chars at the end to kill tailing comma and space 
	 "', data: {score: '" (score rfo) "'}" 
	 " , children: [" 
	 (node (first (rfos-or-file rfo))) 
	 " , " 
	 (node (second (rfos-or-file rfo))) "] }"))
  
  (if (instance? java.io.File (rfos-or-file whatever))
    (leaf whatever)
    (pair whatever)))

