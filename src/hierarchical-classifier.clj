(ns ordinary (:use [incanter.core :only (abs sq sqrt)]
		   [incanter.stats :only (mean)]
		   [clojure.contrib.combinatorics]
		   [clojure.set]))

(def to-words (fn [file-or-files]
		(cond (coll? file-or-files) (apply concat (map to-words file-or-files))
		      true (re-seq #"[a-z]+" (org.apache.commons.lang.StringUtils/lowerCase (slurp (str file-or-files)))))))

(def freq-files (memoize (fn [pof]
			   (let [words (to-words pof)
				 freqs (frequencies words)
				 word-count (count words)]
			     (apply hash-map (flatten (map (fn [[word count]]
							     [word (/ count word-count)])
							   freqs)))))))

(defn merge-general [f g m1 m2]
  (let [m1-only (difference (set (keys m1)) (set (keys m2)))
	m2-only (difference (set (keys m2)) (set (keys m1)))]
    (merge (merge-with f m1 m2)
	   (into {} (map (fn [k] [k (g (m1 k))]) m1-only))
	   (into {} (map (fn [k] [k (g (m2 k))]) m2-only)))))

(defn combine-freqs [rf1 rf2]
  (merge-general (comp mean vector)
		 (fn [val] (mean [val 0])) rf1 rf2))  ; i'm just combining relfreqs taking their (unweighted) mean.  

(def freq (memoize (fn [pof]
		     (if (instance? java.io.File pof)
		       (freq-files pof)
		       (combine-freqs (freq (first pof))  ; combine frequencies by taking their unweighted mean.  
				      (freq (second pof)))))))

(def euclidean (memoize (fn [pof1 pof2 word-list]
			  (let [pof1-freq (freq pof1)
				pof2-freq (freq pof2)]
			    (sqrt (reduce + (map (fn [word]
						   (sq (- (or (pof1-freq word) 0)
							  (or (pof2-freq word) 0))))
						 word-list)))))))

(defn best-pairing [pofs corpus-freqs]
   (let [word-list (keys corpus-freqs)]
     (first (sort (fn [[pof-1-1 pof-1-2] [pof-2-1 pof-2-2]]
		    (compare (euclidean pof-1-1 pof-1-2 word-list)
			     (euclidean pof-2-1 pof-2-2 word-list)))							 
		  (combinations pofs 2)))))

; makes an agglomerative hierarchical cluster of the pofs.
; pof = pairing or file.  pofs is a list of them. 
(defn cluster
  ([pofs] (cluster pofs (freq-files (flatten pofs))))
  ([pofs corpus-freqs] 
     (if (= (count pofs) 1)
       (first pofs)
       (let [best-pair (best-pairing pofs corpus-freqs)]
	 (cluster (conj (filter (complement #(some (set [%]) best-pair))
				pofs)
			best-pair))))))

(defn interesting-words [pof corpus-freqs]
  (let [pof-freq (freq pof)]
    (sort #(> (abs (second %)) (abs (second %2)))
	  (map (fn [word]
		 [word (- (or (pof-freq word) 0) (or (corpus-freqs word) 0))])
	       (keys corpus-freqs)))))
  
(def *directory-string* "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/store/five-file-stash/")
(def *txt-files* (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File *directory-string*) nil false)))
