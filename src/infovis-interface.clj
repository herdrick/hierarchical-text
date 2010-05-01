;{
;        id: "node02",
;        name: "the, it, a",
;        data: {"ethan" : "herdrick","newcolor" : "#444444"},
;        children: []
;}

(defn node [whatever]
  (if (= "class java.io.File" (str (type whatever)))
    (leaf whatever)
    (pair whatever)))

(defn leaf [file]
  (str "{id: 'leafnode" (rand) "', name: '" (str file) "', data: {}, children: [] }"))

(defn pair [two]
  (str "{id: 'pairnode" (rand) 
       "', name: 'summary here"  
       "', data: {}" 
       ", children: [" 
       (node (first two)) 
       " , " 
       (node (second two)) "] }"))

