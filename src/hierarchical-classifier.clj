;Ideas for doc comparisions:
;counts hash
;counts hash and TWC  YES NO!
;porportions 
;summation of all inverses of the Euclidian distances
[["north-america" [:usa :miami]]] 




(def DOC-COUNT 1000000)
(def DOC-OFFSET 0)
(def directory-string "/Users/herdrick/Dropbox/blog/to-classify")
(def all-txt-files (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File directory-string) 
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
				   (let [freqs (freqs docu)
					 word-count (count docu)]
				     (reduce (fn [rel-freqs key]
					       (conj rel-freqs [key (/ (float (freqs key)) word-count)])) ;would be clearer as (merge rel-freqs {key (/ (float (freqs key)) word-count)})   maybe
					     {} docu)))))
 
(def corpus-relfreq (seq->relative-freq omni-doc)) 


(use '(incanter core stats)) ;need this only for abs and mean


;returns the euclidean distance between two documents
(defn euclid [relfreqs1 relfreqs2 omni-relfreq]
  (sqrt (reduce + (map (fn [[word freq]]
			 (sq (abs (- (get relfreqs1 word 0) (get relfreqs2 word 0)))))
		       omni-relfreq))))


;todo: FALSE COMMENT here is where the huge number of relfreq distances are created (in rel-freq-distances) and immediately reduced to a single number (in score)
(defn score-pair [all-word-relfreqs [[relfreq1 file-or-cat1] [relfreq2 file-or-cat2]] ] ; todo: make this a defn
  [(euclid relfreq1 relfreq2 all-word-relfreqs) file-or-cat1 file-or-cat2])

(use 'clojure.contrib.combinatorics) ; sadly, combinations don't produce lazy seqs 

 
(defn score-combos-n-sort [relfreqs omni-relfreq]
  (sort (fn [[n1 _ _] [n2 _ _]] (< n1 n2)) 
		  (map (partial score-pair omni-relfreq)
		       (combinations relfreqs 2))))



(defn combine-relfreqs [rf1 rf2]
  (merge-with #(mean [% %2]) rf1 rf2))  ; i'm just combining relfreqs taking their (unweighted) mean.  


(def relative-freq (memoize (fn [cat-or-file]
			      (if (and (coll? cat-or-file) (= (count cat-or-file) 2))
				(do ;(println "combining...")
				    (combine-relfreqs (relative-freq (first cat-or-file)) 
						      (relative-freq (second cat-or-file))))
				
				(if (= "class java.io.File" (str (type cat-or-file)))
				  (do ;(println "it's a file...")
				      (seq->relative-freq (file->seq cat-or-file)))
				  (new java.lang.Error (str "not file nor pair of freqs")))))))

  

(def docs-relfreqs (map #(vector (seq->relative-freq (file->seq %)) %) txt-files))

(defn filter-intersection [sequence sequences]
  (filter (complement #(some (set sequence) %)) sequences))

;this is the recursive thing that... pretty much is the master function. 
(defn foo
  ([relfreqs omni-relfreq] (foo (score-combos-n-sort relfreqs corpus-relfreq) relfreqs omni-relfreq))     
  ([s-s relfreqs omni-relfreq] 
     (if (< (count s-s) 2) ; can't ever be 2, BTW.  3 choose 2 is 3, 2 choose 2 is 1. 
       s-s
       (let [relfreqs-new (conj (filter-intersection (rest (first s-s)) relfreqs) [(relative-freq (rest (first s-s))) (rest (first s-s))])
	     s-s-new (score-combos-n-sort relfreqs-new omni-relfreq)]
	 (foo s-s-new relfreqs-new omni-relfreq)))))

;here's how i'm calling this right now:
;(.replace (node (rest (first (foo docs-relfreqs corpus-relfreq)))) directory-string "")






