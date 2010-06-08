; somewhere in grep-able codespace i need to keep track of the idea that (file? o) is just (instance? java.io.File o).  This is good Java interop juju.
(ns hc (:use [incanter.core :only (abs sq sqrt)]
	     [incanter.stats :only (mean)]
	     [clojure.contrib.combinatorics :only (combinations)]))


(def file->seq (memoize (fn [file]
			  (re-seq #"[a-z]+" 
				  (org.apache.commons.lang.StringUtils/lowerCase (slurp (.toString file)))))))

(defn freqs [words]
  (reduce (fn [freqs obj] 
	    (merge-with + freqs {obj 1})) 
	  {} words))

(def words->relative-freq (memoize (fn [docu]
				     (let [freqs (freqs docu)
					   word-count (count docu)]
				       (reduce (fn [rel-freqs key]
						 (conj rel-freqs [key (/ (float (freqs key)) word-count)])) ;would be clearer as (merge rel-freqs {key (/ (float (freqs key)) word-count)})   maybe
					       {} docu)))))

(def *directory-string* "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/mixed")

(def *txt-files* (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File *directory-string*) nil true)))

(def *omni-doc* (apply concat (map file->seq *txt-files*))) 

(def *corpus-word-list* (set *omni-doc*))

(def *corpus-relfreqs* (words->relative-freq *omni-doc*)) 

(def *interesting-words-count* 3)

;euclidean distance
(defn euclid [relfreqs1 relfreqs2 word-list]
  (sqrt (reduce + (map (fn [word]
			 (sq (abs (- (get relfreqs1 word 0) (get relfreqs2 word 0)))))
		      word-list))))
 
(defn combine-relfreqs [rf1 rf2]
  (merge-with #(mean [% %2]) rf1 rf2))  ; i'm just combining relfreqs taking their (unweighted) mean.  

(def relative-freqs (memoize (fn [tree]
			       (if (instance? java.io.File tree)
				 (words->relative-freq (file->seq tree))
				 (if (= (count tree) 2)
				   (combine-relfreqs (relative-freqs (first tree)) 
						     (relative-freqs (second tree)))
				   (throw (new Error "this should be two trees but isn't=" tree)))))))
 
(defn interesting-words [tree]
  (take *interesting-words-count* (sort #(> (abs (second %)) (abs (second %2)))
					(map (fn [[word freq]]
					       [word (- (or (get (relative-freqs tree) word) 0) freq)])
					     *corpus-relfreqs*))))
     
(defn best-pairing [trees word-list omni-relfreq]
  (let [combos (sort (fn [[first-tree1 first-tree2] [second-tree1 second-tree2]]
			   (compare (euclid (relative-freqs first-tree1) (relative-freqs first-tree2) word-list)
				    (euclid (relative-freqs second-tree1) (relative-freqs second-tree2) word-list)))							 
			 (combinations trees 2))]
    (first combos)))

;makes an agglomerative hierarchical cluster of the trees.
(defn cluster [trees word-list omni-relfreq]
  (if (= (count trees) 1) 
    trees ;now it's only one tree
    (let [best-pairing-tree (best-pairing trees word-list omni-relfreq)
	  trees-cleaned (filter (complement (fn [tree]
					     (or (= tree (first best-pairing-tree))
						 (= tree (second best-pairing-tree)))))
			       trees)]
      (cluster (conj trees-cleaned best-pairing-tree) word-list omni-relfreq))))




;here's how i'm calling this right now:
;(def stage-gradual-10 (cluster *docs-rfos* *corpus-word-list* *corpus-relfreqs*))
;(.replace (node (first stage-gradual-10)) *directory-string* "")
;(.replace (node (first (cluster *docs-trees* *corpus-word-list* *standard-relfreq*))) *directory-string* "")
;(into-js-file (*1)
;(map (fn [tree] (filter (complement map?) tree)) (cluster *docs-trees* *corpus-word-list* *corpus-relfreq*))
 
;(def bazz-4 (cluster *docs-trees* *corpus-word-list* *corpus-relfreqs*))
;(.replace (node (first bazz-4)) *directory-string* "")
  

;how i got those sonnets from their crappy format off that web page to where each is in it's own file:
;(map (fn [[filename text]] (spit (str "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/sonnets/" filename) text)) (partition 2 (filter (complement empty?) (map #(.trim %) (re-seq #"(?s).+?\n\s*?\n" (slurp "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/sonnets.txt"))))))
