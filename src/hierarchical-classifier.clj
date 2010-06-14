; somewhere in grep-able codespace i need to keep track of the idea that (file? o) is just (instance? java.io.File o).  This is good Java interop juju.
(ns user (:use [incanter.core :only (abs sq sqrt)]
	     [incanter.stats :only (mean)]
	     [clojure.contrib.combinatorics :only (combinations)]))

(def frequencies-m (memoize frequencies))
(def count-m (memoize count))
(def set-m (memoize set))
(def sort-m (memoize set))

(def to-words (memoize (fn [x]
			 (cond (coll? x) (apply concat (map to-words x))
			       true (re-seq #"[a-z]+" (org.apache.commons.lang.StringUtils/lowerCase (slurp (.toString x))))))))

(def relative-freq-words (memoize (fn [pof word]
				     (/ (or (get (frequencies-m (to-words pof)) word) 0)
					(count-m (to-words pof))))))
     
(def combine-relfreqs (comp mean vector)) ; i'm just combining relfreqs taking their (unweighted) mean.  

(def relative-freq (memoize (fn [pof word]
			      (if (instance? java.io.File pof)
				(relative-freq-items (to-words pof) word)
				(combine-relfreqs (relative-freq (first pof) word) 
						  (relative-freq (second pof) word))))))

(defn euclidean [pof1 pof2 word-list]
  (sqrt (reduce + (map (fn [word]
			 (sq (abs (- (relative-freq pof1 word) (relative-freq pof2 word)))))
		       word-list))))
     
(defn best-pairing [pofs word-list]
  (first (sort (fn [[pof-1-1 pof-1-2] [pof-2-1 pof-2-2]]
			   (compare (euclidean pof-1-1 pof-1-2 word-list)
				    (euclidean pof-2-1 pof-2-2 word-list)))							 
			 (combinations pofs 2))))

; makes an agglomerative hierarchical cluster of the pofs.
; pof = pairing or file
(defn cluster [pofs]
  (if (= (count pofs) 1) 
    pofs ;now it's only one pof
    (let [word-list (set-m (to-words (sort-m (flatten pofs)))) ;sort the result of flatten for more likely cache hit on to-words.
	  pofs-cleaned (filter (complement #(some (set [%]) (best-pairing pofs word-list))) pofs)]
      (cluster (conj pofs-cleaned (best-pairing pofs word-list))))))


(defn interesting-words [pof all-files]
  (sort #(> (abs (second %)) (abs (second %2)))
	(map (fn [word]
	       [word (- (relative-freq pof word) (relative-freq-words all-files word))])
	     (set-m (to-words all-files)))))


(def directory-string "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/mixed")
(def txt-files (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File directory-string) nil true)))
