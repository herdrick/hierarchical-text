(ns user (:use [incanter.core]
	     [incanter.stats :only (mean)]
	     [clojure.contrib.combinatorics]
	     [clojure.set]))

(def frequencies-m (memoize frequencies))

(def to-words (memoize (fn [file-or-files]
			 (cond (coll? file-or-files) (apply concat (map to-words file-or-files))
			       true (re-seq #"[a-z]+" (org.apache.commons.lang.StringUtils/lowerCase (slurp (str file-or-files))))))))

(def make-word-idx (memoize (fn [freqs-hash]
			      (map first (sort (fn [[k1 v1] [k2 v2]]
						 (compare v1 v2)) freqs-hash)))))				

(def relative-freq-file (memoize (fn [pof index]
				   (let [freqs (frequencies (to-words pof))]
				     (div (matrix (map #(or (freqs %) 0) index)) (count (to-words pof)))))))

(def relative-freq (memoize (fn [pof idx]
			      (if (instance? java.io.File pof)
				(relative-freq-file pof idx)
				(matrix (map (comp mean vector) ;todo: can this be (map mean <1stmatrix> <2ndmatrix>))  with a trans or two in there?  
					     (relative-freq (first pof) idx)  ; combine frequencies by taking their unweighted mean.  
					     (relative-freq (second pof) idx)))))))

(def euclidean (memoize (comp sqrt sum sq minus)))

(defn best-pairing [pofs]
  (let [freqs-hash (frequencies-m (to-words (sort (flatten pofs))))
	word-idx (make-word-idx freqs-hash)]
    (def current-word-idx word-idx)
    (first (sort (fn [[pof-1-1 pof-1-2] [pof-2-1 pof-2-2]]
		   (compare (euclidean (relative-freq pof-1-1 word-idx) (relative-freq pof-1-2 word-idx))
			    (euclidean (relative-freq pof-2-1 word-idx) (relative-freq pof-2-2 word-idx))))							 
		 (combinations pofs 2)))))

; makes an agglomerative hierarchical cluster of the pofs.
; pof = pairing or file.  pofs is a list of them. 
(defn cluster [pofs] 
  (if (= (count pofs) 1)
    pofs
    (let [best-pair (best-pairing pofs)]
      (cluster (conj (filter (complement #(some (set [%]) best-pair))
			     pofs)
		     best-pair)))))

(def *directory-string* "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/store/three-file-stash")
(def *txt-files* (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File *directory-string*) nil true)))


(defn interesting-words [pof word-idx]
  (map (fn [[idx freq]] [((apply vector word-idx) (int idx)) freq])
       (sort (fn [[idx1 freq1] [idx2 freq2]] (> (abs freq1) (abs freq2))) 
	     (map (comp flatten)  ;todo: wtf   this is broken, right?  
		  (trans [(range (length word-idx)) (minus (relative-freq pof word-idx)
							   (relative-freq-file *txt-files* word-idx))])))))

(def *word-idx* (let [freqs-hash (frequencies (to-words *txt-files*))]
			 (map first (sort (fn [[k1 v1] [k2 v2]]
					    (compare v1 v2)) freqs-hash))))

(defn view-sorted [m col com]
  ;(matrix (sort (fn [r1 r2] (com (nth r1 col) (nth r2 col)))
;		(map (comp flatten vector) m))))

  )

 
(def foobar "user")