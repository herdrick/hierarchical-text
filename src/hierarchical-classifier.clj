(ns hc (:use [incanter.core :only (abs sq sqrt)]
	     [incanter.stats :only (mean)]
	     [clojure.contrib.combinatorics]
	     [clojure.set]))

(def to-words (fn [file-tree]
		(if (coll? file-tree)
		  (apply concat (map to-words (flatten file-tree)))
		  (re-seq #"[a-z]+" (org.apache.commons.lang.StringUtils/lowerCase (slurp (str file-tree)))))))

(def freqs-files (memoize (fn [pof]
			   (let [words (to-words pof)
				 word-count (count words)]
			     (apply hash-map (flatten (map (fn [[word count]]
							     [word (/ count word-count)])
							    (frequencies words))))))))

;merges two maps.  when both have a given key, use f to combine the vals.  use g on all other vals.
(defn merge-general [f g m1 m2]
  (let [m1-only (difference (set (keys m1)) (set (keys m2)))
	m2-only (difference (set (keys m2)) (set (keys m1)))]
    (merge (merge-with f m1 m2)
	   (into {} (map (fn [k] [k (g (m1 k))]) m1-only))
	   (into {} (map (fn [k] [k (g (m2 k))]) m2-only)))))

(defn combine-freqs [f1 f2]
  (merge-general (comp mean vector)
		 (fn [val] (mean [val 0])) f1 f2))  ; i'm just combining freqs taking their (unweighted) mean.  

(def freqs (memoize (fn [pof]
		     (if (instance? java.io.File pof)
		       (freqs-files pof)
		       (combine-freqs (freqs (first pof))  
				      (freqs (second pof)))))))

(def euclidean (memoize (fn [freqs1 freqs2 feature-list]
			  (sqrt (reduce + (map (fn [word]
						 (sq (- (or (freqs1 word) 0)
							(or (freqs2 word) 0))))
					       feature-list))))))
  
(defn best-pairing [pofs corpus-freqs]
   (let [word-list (keys corpus-freqs)]
     (first (sort (fn [[pof-1-1 pof-1-2] [pof-2-1 pof-2-2]]
		    (compare (euclidean (freqs pof-1-1) (freqs pof-1-2) word-list)
			     (euclidean (freqs pof-2-1) (freqs pof-2-2) word-list)))							 
		  (combinations pofs 2)))))

; make agglomerative hierarchical cluster of the pofs.
; pof = pairing or file.  pofs is a list of them. 
(defn cluster
  ([pofs] (cluster pofs (freqs-files (flatten pofs))))
  ([pofs corpus-freqs] 
     (if (= (count pofs) 1)
       (first pofs)
       (let [best-pair (best-pairing pofs corpus-freqs)]
	 (cluster (conj (filter (complement #(some (set [%]) best-pair))
				pofs)
			best-pair))))))

(defn interesting-words [pof corpus-freqs]
  (let [pof-freqs (freqs pof)]
    (sort #(> (abs (second %)) (abs (second %2)))
	  (map (fn [word]
		 [word (- (or (pof-freqs word) 0) (or (corpus-freqs word) 0))])
	       (keys corpus-freqs)))))
  
(def *directory-string* "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/store/nine-file-stash/")
(def *txt-files* (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File *directory-string*) nil false)))
   