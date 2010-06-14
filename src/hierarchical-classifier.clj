; somewhere in grep-able codespace i need to keep track of the idea that (file? o) is just (instance? java.io.File o).  This is good Java interop juju.
(ns hc (:use [incanter.core :only (abs sq sqrt)]
	     [incanter.stats :only (mean)]
	     [clojure.contrib.combinatorics :only (combinations)]))

(def frequencies-m (memoize frequencies))
(def count-m (memoize count))
(def set-m (memoize set))

(def to-words (memoize (fn [x]
			 (cond (coll? x) (apply concat (map to-words x))
			       true (re-seq #"[a-z]+" (org.apache.commons.lang.StringUtils/lowerCase (slurp (.toString x))))))))

(defn relative-freq-items [xs x]
  (/ (or (get (frequencies-m xs) x) 0)
     (count-m xs)))

(defn combine-relfreqs (comp mean vector)) ; i'm just combining relfreqs taking their (unweighted) mean.  

(def relative-freq (memoize (fn [pof word]
			      (if (instance? java.io.File pof)
				(relative-freq-items (to-words pof) word)
				(combine-relfreqs (relative-freq (first pof) word) 
						  (relative-freq (second pof) word))))))
				  
;euclidean distance
(defn euclid [pof1 pof2 word-list]
  (sqrt (reduce + (map (fn [word]
			 (sq (abs (- (relative-freq pof1 word) (relative-freq pof2 word)))))
		       word-list))))

(defn interesting-words [pof all-files]
  (sort #(> (abs (second %)) (abs (second %2)))
	(map (fn [word]
	       [word (- (relative-freq pof word) (relative-freq-items (to-words all-files) word))])
	     (set-m (to-words all-files)))))
     
(defn best-pairing [pofs word-list]
  (first (sort (fn [[first-pof1 first-pof2] [second-pof1 second-pof2]]
			   (compare (euclid first-pof1 first-pof2 word-list)
				    (euclid second-pof1 second-pof2 word-list)))							 
			 (combinations pofs 2))))

; makes an agglomerative hierarchical cluster of the pofs.
; pof = pairing or file
(defn cluster [pofs]
  (if (= (count pofs) 1) 
    pofs ;now it's only one pof
    (let [word-list (set-m (to-words (sort (flatten pofs)))) ;sort the result of flatten for more likely cache hit on to-words.
	  pofs-cleaned (filter (complement #(some (set [%]) (best-pairing pofs word-list))) pofs)]
      (cluster (conj pofs-cleaned (best-pairing pofs word-list))))))

(def directory-string "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/mixed")
(def txt-files (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File directory-string) nil true)))
;(def omni-doc (apply concat (to-words txt-files)))
;(def corpus-word-list (set omni-doc))
;(def corpus-relfreqs (relative-freq-items omni-doc word))


;here's how i'm calling this right now:
;(def stage-gradual-10 (cluster *docs-rfos* *corpus-word-list* *corpus-relfreqs*))
;(.replace (node (first stage-gradual-10)) *directory-string* "")
;(.replace (node (first (cluster *docs-pofs* *corpus-word-list* *standard-relfreq*))) *directory-string* "")
;(into-js-file (*1)
;(map (fn [pof] (filter (complement map?) pof)) (cluster *docs-pofs* *corpus-word-list* *corpus-relfreq*))
 
;(def bazz-4 (cluster *docs-pofs* *corpus-word-list* *corpus-relfreqs*))
;(.replace (node (first bazz-4)) *directory-string* "")  

;how i got those sonnets from their crappy format off that web page to where each is in it's own file:
					;(map (fn [[filename text]] (spit (str "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/sonnets/" filename) text)) (partition 2 (filter (complement empty?) (map #(.trim %) (re-seq #"(?s).+?\n\s*?\n" (slurp "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/sonnets.txt"))))))
