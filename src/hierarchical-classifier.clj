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
 
(defn combine-relfreqs [rf1 rf2]
  (mean [rf1 rf2]))  ; i'm just combining relfreqs taking their (unweighted) mean.  

(def relative-freq (memoize (fn [pof word]
			      (if (instance? java.io.File pof)
				(get (words->relative-freq (file->seq pof)) word 0)
				(if (= (count pof) 2)
				  (combine-relfreqs (relative-freq (first pof) word) 
						    (relative-freq (second pof) word))
				  (throw (new Error "this should be two pofs but isn't=" pof)))))))

;euclidean distance
(defn euclid [pof1 pof2 word-list]
  (sqrt (reduce + (map (fn [word]
			 (sq (abs (- (relative-freq pof1 word) (relative-freq pof2 word)))))
		       word-list))))

(defn interesting-words [pof]
  (take *interesting-words-count* (sort #(> (abs (second %)) (abs (second %2)))
					(map (fn [[word freq]]
					       [word (- (relative-freq pof word) freq)])
					     *corpus-relfreqs*))))
     
(defn best-pairing [pofs word-list omni-relfreq]
  (let [combos (sort (fn [[first-pof1 first-pof2] [second-pof1 second-pof2]]
			   (compare (euclid first-pof1 first-pof2 word-list)
				    (euclid second-pof1 second-pof2 word-list)))							 
			 (combinations pofs 2))]
    (first combos)))

; makes an agglomerative hierarchical cluster of the pofs.
; pof = pairing or file
(defn cluster [pofs word-list omni-relfreq]
  (if (= (count pofs) 1) 
    pofs ;now it's only one pof
    (let [best-pairing-pof (best-pairing pofs word-list omni-relfreq)
	  pofs-cleaned (filter #(not (or (= % (first best-pairing-pof))
					  (= % (second best-pairing-pof))))
				pofs)]
      (cluster (conj pofs-cleaned best-pairing-pof) word-list omni-relfreq))))

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
