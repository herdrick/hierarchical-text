(ns radical (:use [incanter.core :only (abs sq sqrt)]
		  [incanter.stats :only (mean)]
		  [clojure.contrib.combinatorics :only (combinations)]))
		 

(def frequencies-m (memoize frequencies))
(def count-m (memoize count))
(def set-m (memoize set))
(def flatten-m (memoize flatten))

(def to-words (memoize (fn [file-or-files]
			 (if (coll? file-or-files)
			   (apply concat (map to-words (set-m (flatten-m file-or-files))))
			   (re-seq #"[a-z]+" (org.apache.commons.lang.StringUtils/lowerCase (slurp (str file-or-files))))))))

(def word-list (memoize (fn [pof]
			  (set-m (to-words pof)))))

(def relative-freq-files (memoize (fn [pof word]
				    (/ (or (get (frequencies-m (to-words pof)) word) 0)
				       (count-m (to-words pof))))))

(def relative-freq (memoize (fn [pof word]
			      (if (instance? java.io.File pof)
				(relative-freq-files pof word)
				(mean (vector (relative-freq (first pof) word)  ; combine frequencies by taking their unweighted mean.  
					      (relative-freq (second pof) word)))))))

(def euclidean (memoize (fn [pof1 pof2 pofs]
			  (sqrt (reduce + (map (fn [word]
						 (sq (- (relative-freq pof1 word)
							(relative-freq pof2 word))))
					       (word-list pofs)))))))

(defn best-pairing [pofs]
  (first (sort (fn [[pof-1-1 pof-1-2] [pof-2-1 pof-2-2]]
		 (compare (euclidean pof-1-1 pof-1-2 pofs)
			  (euclidean pof-2-1 pof-2-2 pofs)))							 
	       (combinations pofs 2))))

; makes an agglomerative hierarchical cluster of the pofs.
; pof = pairing or file.  pofs is a list of them.
(defn cluster [pofs]
  (if (= (count pofs) 1) 
    (first pofs)
    (cluster (conj (filter (complement #(some (set [%]) (best-pairing pofs)))
			   pofs)
		   (best-pairing pofs)))))

(defn interesting-words [pof top-pof]
  (sort #(> (abs (second %)) (abs (second %2)))
	(map (fn [word]
	       [word (- (relative-freq pof word) (relative-freq-files top-pof word))])
	     (word-list top-pof))))

(def *directory-string* "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/store/five-file-stash/")
(def *txt-files* (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File *directory-string*) nil false)))
