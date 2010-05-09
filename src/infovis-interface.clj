(defn node [whatever]
  (defn leaf [rfo]
    (str "{id: 'leafnode" (rand) "', name: '" (.replace (str (rfos-or-file rfo)) "'" "") "', data: {}, children: [] }"))

  (defn pair [rfo]
    (str "{id: 'pairnode" (rand) 
	 "', name: '" (score rfo)   
	 "', data: {score: '" (score rfo)
	 "'}, children: [" 
	 (node (first (rfos-or-file rfo))) 
	 " , " 
	 (node (second (rfos-or-file rfo))) "] }"))
  
  (if (instance? java.io.File (rfos-or-file whatever))
    (leaf whatever)
    (pair whatever)))
