(ns user (:use [incanter.core]
	     [incanter.stats :only (mean)]
	     [clojure.contrib.combinatorics]
	     [clojure.set]))

(def frequencies-m (memoize frequencies))
(def sort-m (memoize sort))
(def flatten-m (memoize flatten))]

(def to-words (memoize (fn [file-tree]
			 (cond (coll? file-tree) (apply concat (map to-words (flatten file-tree)))
			       true (re-seq #"[a-z]+" (org.apache.commons.lang.StringUtils/lowerCase (slurp (str file-tree))))))))

(def make-word-idx (memoize (fn [pofs]
			      (vec (map first (sort (fn [[k1 v1] [k2 v2]] ;all i need is a consistent sort, order doesn't otherwise matter
						      (compare v1 v2))
						    (frequencies (to-words pofs))))))))

(def freq-files (memoize (fn [file-tree all-files]
				   (let [words (to-words file-tree)] ;let clause for brevity
				     (div (matrix (map #(or ((frequencies-m words) %) 0)
						       (make-word-idx all-files))) (count words))))))
; returns vector
(def freqs (memoize (fn [pof all-files]
			       (if (instance? java.io.File pof)
				 (freq-files pof all-files)
				 (map (comp mean vector) ; combine frequencies by taking their unweighted mean.
				      (freqs (first pof) all-files)    
				      (freqs (second pof) all-files))))))

(defn scores [pairings all-files]
  (let [f-vecs-1 (freqs-matrix first pairings all-files)
	f-vecs-2 (freqs-matrix second pairings all-files)
	diff-matrix (minus f-vecs-1 f-vecs-2)
	sums-of-squares-of-diffs (diag (mmult diff-matrix  (trans diff-matrix)))]
    (sqrt sums-of-squares-of-diffs))) ; sqrt is vectorized, as is minus.

(defn best-pairing [pofs]
  (let [pairings (combinations pofs 2)]
    (second (first (sort (apply map vector [(scores pairings pofs) pairings]))))))

(defn freqs-matrix [accessor pairings all-files]
  (map (comp #(freqs % all-files) accessor) pairings))
	 
; makes an agglomerative hierarchical cluster of the pofs.
; pof = pairing or file.  pofs is a list of them. 
(defn cluster [pofs] 
  (if (= (count pofs) 1)
    (first pofs)
    (let [best-pair (best-pairing-2 pofs)]
      (cluster (conj (filter (complement #(some (set [%]) best-pair))
			     pofs)
		     best-pair)))))

(defn interesting-words [pof pofs]
  (let [all-files (sort-m (flatten-m pofs))
	word-idx (make-word-idx all-files)] ;need this list to be a vector for random access use.
    (map (fn [[idx freq]] [(word-idx (int idx)) freq])
	 (sort (fn [[idx1 freq1] [idx2 freq2]] (> (abs freq1) (abs freq2))) 
	       (map identity ;sort won't take a matrix - this creates a list
		    (trans [(range (length word-idx)) (minus (freqs pof all-files)
							     (freq-files all-files all-files))]))))))

(def *directory-string* "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/store/five-file-stash/")
(def *txt-files* (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File *directory-string*) nil false)))
  
;(def *word-idx* (let [freqs-hash (frequencies (to-words *txt-files*))]
;			 (make-word-idx *txt-files*)))