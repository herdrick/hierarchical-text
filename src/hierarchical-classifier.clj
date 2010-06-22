(ns user (:use [incanter.core]
	     [incanter.stats :only (mean)]
	     [clojure.contrib.combinatorics]
	     [clojure.set]))

(def frequencies-m (memoize frequencies))

(def flatten-sort (memoize (comp sort flatten)))

(def to-words (memoize (fn [file-or-files]
			 (cond (coll? file-or-files) (apply concat (map to-words file-or-files))
			       true (re-seq #"[a-z]+" (org.apache.commons.lang.StringUtils/lowerCase (slurp (str file-or-files))))))))

(def make-word-idx (memoize (fn [pofs]
			      (vec (map first (sort (fn [[k1 v1] [k2 v2]] ;all i need is a consistent sort, order doesn't otherwise matter
						      (compare v1 v2))
						    (frequencies (to-words pofs))))))))


(def relative-freq-files (memoize (fn [file-or-files all-files]
				    (prn "relative-freq-files " file-or-files)
				   (let [words (to-words file-or-files)] ;let clause for brevity
				     (div (matrix (map #(or ((frequencies-m words) %) 0)
						       (make-word-idx all-files))) (count words))))))

 
(def relative-freqs (memoize (fn [pof all-files]
			       ;(prn "relative-freqs " pof)
			       ;(prn "(type pof) " (type pof))
			      (if (instance? java.io.File pof)
				(relative-freq-files pof all-files)
				(do
				  (prn "(count pof)=" (count pof))
				  (matrix (map (comp mean vector) ; combine frequencies by taking their unweighted mean.
					       (relative-freqs (first pof) all-files)    
					       (relative-freqs (second pof) all-files))))))))

(def euclidean (memoize (comp sqrt sum sq minus)))

(defn best-pairing [pofs]
  (let [all-files (flatten-sort pofs)] ;this could be pushed all the way down to relative-freq-files, passing pofs around instead.  converting here to get cache hits on relative-freqs.  opt.
    (first (sort (fn [[pof-1-1 pof-1-2] [pof-2-1 pof-2-2]]
		   (compare (euclidean (relative-freqs pof-1-1 all-files) (relative-freqs pof-1-2 all-files))
			    (euclidean (relative-freqs pof-2-1 all-files) (relative-freqs pof-2-2 all-files))))							 
		   (combinations pofs 2)))))

; makes an agglomerative hierarchical cluster of the pofs.
; pof = pairing or file.  pofs is a list of them. 
(defn cluster [pofs] 
  (if (= (count pofs) 1)
    (first pofs)
    (let [best-pair (best-pairing pofs)]
      (cluster (conj (filter (complement #(some (set [%]) best-pair))
			     pofs)
		     best-pair)))))

(def *directory-string* "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/store/eight-file-stash/")
(def *txt-files* (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File *directory-string*) nil false)))

(defn interesting-words [pof pofs]
  ;(prn pof)
  (let [all-files (flatten-sort pofs)
	word-idx (make-word-idx all-files)] ;need this list to be a vector for random access use.
    (map (fn [[idx freq]] [(word-idx (int idx)) freq])
	 (sort (fn [[idx1 freq1] [idx2 freq2]] (> (abs freq1) (abs freq2))) 
	       (map identity ;sort won't take a matrix - this creates a list
		    (trans [(range (length word-idx)) (minus (relative-freqs pof all-files)
							     (relative-freq-files all-files all-files))]))))))
  
;(def *word-idx* (let [freqs-hash (frequencies (to-words *txt-files*))]
;			 (make-word-idx *txt-files*)))