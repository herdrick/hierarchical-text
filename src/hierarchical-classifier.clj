; somewhere in grep-able codespace i need to keep track of the idea that (file? o) is just (instance? java.io.File o).  This is good Java interop juju.
(ns hc (:use [incanter.core :only (abs sq sqrt)]
	     [incanter.stats :only (mean)]
	     [clojure.contrib.combinatorics :only (combinations)]))

(def *directory-string* "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/mixed")

(def *txt-files* (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File *directory-string*) nil true)))

(def *omni-doc* (apply concat (map file->seq *txt-files*))) 

(def *corpus-word-list* (set *omni-doc*))

(def *corpus-relfreqs* (words->relative-freq *omni-doc*)) 


(defn make-rfo [{:keys [score relfreqs interesting rfos-or-file]}]
  [score relfreqs interesting rfos-or-file]) 
(def relfreqs second)
(def rfos-or-file  #(nth % 3))
(defn rfo= [rfo1 rfo2]
  (= (rfos-or-file rfo1) (rfos-or-file rfo2)))
  
(def *interesting-words-count* 3)

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


;euclidean distance
(defn euclid [relfreqs1 relfreqs2 word-list]
  (sqrt (reduce + (map (fn [word]
			 (sq (abs (- (get relfreqs1 word 0) (get relfreqs2 word 0)))))
		      word-list))))
 
(defn combine-relfreqs [rf1 rf2]
  (merge-with #(mean [% %2]) rf1 rf2))  ; i'm just combining relfreqs taking their (unweighted) mean.  

(def relative-freqs (memoize (fn [rfo]
			      (let [r-o-f (rfos-or-file rfo)] 
				(if (instance? java.io.File r-o-f)
				  (words->relative-freq (file->seq r-o-f))
				  (if (= (count r-o-f) 2)
				    (combine-relfreqs (relative-freqs (first r-o-f)) 
						      (relative-freqs (second r-o-f)))
				    (throw (new Error "this should be two rfos but isn't=" r-o-f))))))))
 
(defn interesting-words [rfo]
  (take *interesting-words-count* (sort #(> (abs (second %)) (abs (second %2)))
					(map (fn [[word freq]]
					       [word (- (or (get (relative-freqs rfo) word) 0) freq)])
					     *corpus-relfreqs*))))
 
(def conceive-rfo (memoize (fn [word-list [rfo1 rfo2]]
			   (make-rfo {:rfos-or-file [(make-rfo {:rfos-or-file (rfos-or-file rfo1)}) ;making a mock rfo here preserving the values of rfo1. lacks: relfreqs, interesting-words
						     (make-rfo {:rfos-or-file (rfos-or-file rfo2)})]})))) 
  
(defn best-pairing [rfos word-list omni-relfreq]
  (let [combos (sort (fn [[first-rfo1 first-rfo2] [second-rfo1 second-rfo2]]
			   (compare (euclid (relfreqs first-rfo1) (relfreqs first-rfo2) word-list)
				    (euclid (relfreqs second-rfo1) (relfreqs second-rfo2) word-list)))							 
			 (combinations rfos 2))
	best-pair (first combos)
	best-rfo (conceive-rfo word-list best-pair)]
    (make-rfo {:relfreqs (relative-freqs best-rfo) :rfos-or-file (rfos-or-file best-rfo)})))

;makes an agglomerative hierarchical cluster of the rfos.
(defn cluster [rfos word-list omni-relfreq]
  (if (= (count rfos) 1) 
    rfos
    (let [best-pairing-rfo (best-pairing rfos word-list omni-relfreq)
	  rfos-cleaned (filter (complement (fn [rfo]
					     (or (rfo= rfo (first (rfos-or-file best-pairing-rfo)))
						 (rfo= rfo (second (rfos-or-file best-pairing-rfo))))))
			       rfos)]
      (cluster (conj rfos-cleaned best-pairing-rfo) word-list omni-relfreq))))



(def *docs-rfos* (map (fn [file]
		      (let [relfreqs (words->relative-freq (file->seq file))]
			(make-rfo {:relfreqs relfreqs :rfos-or-file file}))) 
		    *txt-files*))




;here's how i'm calling this right now:
;(.replace (node (first (cluster *docs-rfos* *corpus-word-list* *standard-relfreq*))) *directory-string* "")
;(into-js-file (*1)
;(map (fn [rfo] (filter (complement map?) rfo)) (cluster *docs-rfos* *corpus-word-list* *corpus-relfreq*))
 
;(def bazz-4 (cluster *docs-rfos* *corpus-word-list* *corpus-relfreqs*))
;(.replace (node (first bazz-4)) *directory-string* "")
  

;how i got those sonnets from their crappy format off that web page to where each is in it's own file:
;(map (fn [[filename text]] (spit (str "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/sonnets/" filename) text)) (partition 2 (filter (complement empty?) (map #(.trim %) (re-seq #"(?s).+?\n\s*?\n" (slurp "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/sonnets.txt"))))))
