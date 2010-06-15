(ns user (:use [incanter.core :only (abs sq sqrt)]
	     [incanter.stats :only (mean)]
	     [clojure.contrib.combinatorics]
	     [clojure.set]))

(def frequencies-m (memoize frequencies))
(def count-m (memoize count))
(def set-m (memoize set))
(def sort-m (memoize set))

(def to-words (memoize (fn [file-or-files]
			 (cond (coll? file-or-files) (apply concat (map to-words file-or-files))
			       true (re-seq #"[a-z]+" (org.apache.commons.lang.StringUtils/lowerCase (slurp (str file-or-files))))))))

(def relative-freq-file (memoize (fn [pof]
				   (apply hash-map (flatten (map (fn [[word count]]
								   [word (/ count (count-m (frequencies-m (to-words pof))))])
								 (frequencies-m (to-words pof))))))))

(defn merge-general [f g m1 m2]
  (let [m1-only (difference (set (keys m1)) (set (keys m2)))
	m2-only (difference (set (keys m2)) (set (keys m1)))]
    (merge (merge-with f m1 m2)
	   (into {} (map (fn [k] [k (g (m1 k))]) m1-only))
	   (into {} (map (fn [k] [k (g (m2 k))]) m2-only)))))

(defn combine-relative-freqs [rf1 rf2]
  (merge-general (comp mean vector) (fn [val] (mean [val 0])) rf1 rf2))  ; i'm just combining relfreqs taking their (unweighted) mean.  

(def relative-freq (memoize (fn [pof]
			      (if (instance? java.io.File pof)
				(relative-freq-file pof)
				(combine-relative-freqs (relative-freq (first pof))  ; combine frequencies by taking their unweighted mean.  
							(relative-freq (second pof)))))))

(defn euclidean [pof1 pof2 word-list]
  (sqrt (reduce + (map (fn [word]
			 (sq (abs (- (or ((relative-freq pof1) word) 0) (or ((relative-freq pof2) word) 0)))))
		       word-list))))

(defn best-pairing [pofs]
  (let [word-list (set-m (to-words (sort-m (flatten pofs))))] ;sort the result of flatten for more likely cache hit on to-words.
    (first (sort (fn [[pof-1-1 pof-1-2] [pof-2-1 pof-2-2]]
		   (compare (euclidean pof-1-1 pof-1-2 word-list)
			    (euclidean pof-2-1 pof-2-2 word-list)))							 
		 (combinations pofs 2)))))

; makes an agglomerative hierarchical cluster of the pofs.
; pof = pairing or file.  pofs is a list of them.
(defn cluster [pofs]
  (if (= (count pofs) 1)
    pofs
    (cluster (conj (filter (complement #(some (set [%]) (best-pairing pofs)))
			   pofs)
		   (best-pairing pofs)))))

(defn interesting-words [pof all-files]
  (sort #(> (abs (second %)) (abs (second %2)))
	(map (fn [word]
	       [word (- (or ((relative-freq pof) word) 0) (or ((relative-freq-file all-files) word) 0))])
	     (set-m (to-words all-files)))))

(def directory-string "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/mixed")
(def txt-files (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File directory-string) nil true)))