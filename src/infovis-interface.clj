;{
;        id: "node02",
;        name: "the, it, a",
;        data: {"ethan" : "herdrick","newcolor" : "#444444"},
;        children: []
;}

(defn node [whatever]
  (defn leaf [file]
    (str "{id: 'leafnode" (rand) "', name: '" (.replace (str file) "'" "") "', data: {}, children: [] }"))

  (defn pair [the-pair]
    (def score first)
    (def left-branch second)
    (def right-branch #(nth % 2))
    (str "{id: 'pairnode" (rand) 
	 "', name: 'summary here"  
	 "', data: {score: '" (score the-pair)
	 "', children: [" 
	 (node (left-branch the-pair)) 
	 " , " 
	 (node (right-branch the-pair)) "] }"))
  (if (= "class java.io.File" (str (type whatever)))
    (leaf whatever)
    (pair whatever)))


