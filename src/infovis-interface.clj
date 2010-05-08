;{
;        id: "node02",
;        name: "the, it, a",
;        data: {"ethan" : "herdrick","newcolor" : "#444444"},
;        children: []
;}

(defn node [whatever]
  (defn leaf [rfo]
    (str "{id: 'leafnode" (rand) "', name: '" (.replace (str (rfos-or-file rfo)) "'" "") "', data: {}, children: [] }"))

  (defn pair [rfo]
    ;(def left-branch second)
    ;(def right-branch #(nth % 2))
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



(node [0.2731772546734316 nil (new java.io.File "/Users/herdrick/Dropbox/blog/to-classify/Lisp and Google.txt")])