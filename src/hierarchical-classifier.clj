;Ideas for doc comparisions:
;counts hash
;counts hash and TWC  YES NO!
;porportions 
;summation of all inverses of the Euclidian distances
[["north-america" [:usa :miami]]] 




(def DOC-COUNT 4)
(def DOC-OFFSET 24)

(def all-txt-files (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File "/Users/herdrick/Dropbox/blog") 
							       (into-array ["txt"]) true)))

(def txt-files (take DOC-COUNT (drop DOC-OFFSET all-txt-files)))

(defn file->seq [file]
  (re-seq #"[a-z]+" 
	  (org.apache.commons.lang.StringUtils/lowerCase (slurp (.toString file)))))

(def omni-doc (apply concat (map file->seq txt-files)))
 
(defn freqs [words]
  (reduce (fn [freqs obj] 
	    (merge-with + freqs {obj 1})) 
	  {} words))

;takes a list of strings
(def seq->relative-freq (memoize (fn [docu] 
				   ;(println (hash docu))
				   (let [freqs (freqs docu)
					 word-count (count docu)]
					;(print word-count)
				     (reduce (fn [rel-freqs key]
					       (conj rel-freqs [key (/ (float (freqs key)) word-count)])) ;would be clearer as (merge rel-freqs {key (/ (float (freqs key)) word-count)})   maybe
					     {} docu)))))
 
(def corpus-relfreq (seq->relative-freq omni-doc)) 


(use '(incanter core stats)) ;need this only for abs and mean

;returns the signature of distances between two documents' word freqs
;returned Map has a count = to the count of omni-relfreq, i.e. it's huge
;refactor this using merge-into
(defn rel-freq-distances [relfreqs1 relfreqs2 omni-relfreq]
  (reduce (fn [acc [word relfreq]] 
  	    (conj acc [word (abs (/ (- (get relfreqs1 word 0) (get relfreqs2 word 0))
				    relfreq))])) 	  
	  {}  
	  omni-relfreq))
	   
;scores a single Map of relative frequency distances (each is the frequency distance (between two docs) of a single word). this has an entry for each word in the entire corpus, i.e. it's huge 
;this sucks, but kinda works.  good enough.
;uh, i think the / part is needless because all our rel-freq-distances have the same length (= to the length of the corpus word frequency map) and this is only for comparison.  todo: fix by killing it. UPDATE: somehow, when I did that it changed my tree result.  Have no clue why.  Try looking into that after I get the tree-display thing going, to some degree.
(defn score [rel-freq-distances]
  ;(println "(count rel-freq-distances)=" (count rel-freq-distances))
  (/ (reduce + (vals rel-freq-distances)) (count rel-freq-distances))) 

;here is where the huge number of relfreq distances are created (in rel-freq-distances) and immediately reduced to a single number (in score)
(defn score-pair [all-word-relfreqs [[relfreq1 file-or-cat1] [relfreq2 file-or-cat2]] ] ; todo: make this a defn
  [(score (rel-freq-distances relfreq1 relfreq2 all-word-relfreqs)) file-or-cat1 file-or-cat2])

(use 'clojure.contrib.combinatorics) ; sadly, combinations don't produce lazy seqs 

 
(defn score-combos-n-sort [relfreqs omni-relfreq]
  (sort (fn [[n1 _ _] [n2 _ _]] (< n1 n2)) 
		  (map (partial score-pair omni-relfreq)
		       (combinations relfreqs 2))))



(defn combine-relfreqs [rf1 rf2]
  (merge-with #(mean [% %2]) rf1 rf2))  ; i'm just combining relfreqs taking their (unweighted) mean.  


(def relative-freq (memoize (fn [cat-or-file]
			      (if (and (coll? cat-or-file) (= (count cat-or-file) 2))
				(do (println "combining...")
				    (combine-relfreqs (relative-freq (first cat-or-file)) 
						      (relative-freq (second cat-or-file))))
				
				(if (= "class java.io.File" (str (type cat-or-file)))
				  (do (println "it's a file...")
				      (seq->relative-freq (file->seq cat-or-file)))
				  (new java.lang.Error (str "not file nor pair of freqs"))))))); this is a file instead of a collection of 

  

(def hundred-doc-relfreqs (map #(vector (seq->relative-freq (file->seq %)) %) txt-files))

;likely i shouldn't have made a general fn for this but instead just inlined it.  but doing it this way cleared my head - allowed me to think about this properly   
;DURRRRR this is filter-intersection: (filter #(some (set [2 3]) %) [[1 9] [3 4] [5  2 6]])
(defn filter-intersection [sequence sequences]
  (filter (complement #(some (set sequence) %)) sequences))

;(defn filter-intersection [sequence sequences]
;  (filter (fn [sequ] 
;	    (empty? (clojure.set/intersection (set sequ) 
;					      (set sequence)))) sequences))

;this is the recursive thing that... pretty much is the master function. 
(defn foo
  ([relfreqs omni-relfreq] (foo (score-combos-n-sort relfreqs corpus-relfreq) relfreqs omni-relfreq))     
  ([s-s relfreqs omni-relfreq] 
     (if (< (count s-s) 2) ; can't ever be 2, BTW.  3 choose 2 is 3, 2 choose 2 is 1. 
       s-s
       (let [relfreqs-new (conj (filter-intersection (rest (first s-s)) relfreqs) [(relative-freq (rest (first s-s))) (rest (first s-s))])
	     s-s-new (score-combos-n-sort relfreqs-new omni-relfreq)]
	 (foo s-s-new relfreqs-new omni-relfreq)))))

 



  






