(ns radical (:use [incanter.core :only (abs sq sqrt)]
		  [incanter.stats :only (mean)]
		  [clojure.contrib.combinatorics :only (combinations)]))
		 
;hello lem
(def set-m (memoize set))
(def sort-m (memoize sort))
(def flatten-m (memoize flatten))
(def frequencies-m (memoize frequencies))
(def count-m (memoize count))
 
(def to-words (memoize (fn [file-tree]
			 (if (coll? file-tree)
			   (apply concat (map to-words (set-m (sort-m (flatten-m file-tree)))))
			   (re-seq #"[a-z]+" (org.apache.commons.lang.StringUtils/lowerCase (slurp (str file-tree))))))))

(def word-list (memoize (comp set-m to-words)))

(def freq-files (memoize (fn [pof word]
			   (/ (or (get (frequencies-m (to-words pof)) word) 0) 
			      (count-m (to-words pof))))))

(def freq (memoize (fn [pof word]
		     (if (instance? java.io.File pof)
		       (freq-files pof word)
		       (mean (vector (freq (first pof) word)  ; combine frequencies by taking their unweighted mean.  
				     (freq (second pof) word)))))))
 
(def euclidean (memoize (fn [pof1 pof2 pofs]
			  (sqrt (reduce + (map (fn [word]
						 (sq (- (freq pof1 word)
							(freq pof2 word))))
					       (word-list pofs)))))))

(def best-pairing (memoize (fn [pofs]
			     (first (sort (fn [[pof-1-1 pof-1-2] [pof-2-1 pof-2-2]]
					    (compare (euclidean pof-1-1 pof-1-2 pofs)
						     (euclidean pof-2-1 pof-2-2 pofs)))							 
					  (combinations pofs 2))))))

; makes an agglomerative hierarchical cluster of the pofs.
;pof = pairing or file (i.e. a file tree).  pofs is a list of them. 
(defn cluster [pofs]
  (if (= (count pofs) 1) 
    (first pofs)
    (cluster (conj (filter (complement #(some (set [%]) (best-pairing pofs)))
			   pofs)
		   (best-pairing pofs)))))

(defn interesting-words [pof top-pof]
  (sort #(> (abs (second %)) (abs (second %2)))
	(map (fn [word]
	       [word (- (freq pof word) (freq-files top-pof word))])
	     (word-list top-pof))))

(def *directory-string* "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/store/five-file-stash/")
(def *txt-files* (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File *directory-string*) nil false)))
