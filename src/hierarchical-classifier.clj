;Ideas for doc comparisions:
;counts hash
;counts hash and TWC  YES NO!
;porportions 
;summation of all inverses of the Euclidian distances
[["north-america" [:usa :miami]]] 
 
; somewhere in grep-able codespace i need to keep track of the idea that (file? o) is just (instance? java.io.File o).  This is good Java interop juju.
 
(ns hc (:use [incanter.core :only (abs sq sqrt)]
	       [incanter.stats :only (mean)]
	       [clojure.contrib.combinatorics :only (combinations)]))

(def *interesting-words-count* 3) 
(def *directory-string* "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/mixed")
(def *txt-files* (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File *directory-string*) nil true)))

(def file->seq (memoize (fn [file]
			  (re-seq #"[a-z]+" 
				  (org.apache.commons.lang.StringUtils/lowerCase (slurp (.toString file)))))))
 
(def *omni-doc* (apply concat (map file->seq *txt-files*))) 
(def *corpus-word-list* (set *omni-doc*))
;(def *large-standard-doc* (file->seq (new java.io.File "/Users/herdrick/Dropbox/clojure/spell-check/big.words")))

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


(defn make-rfo [{:keys [score relfreqs interesting rfos-or-file]}]
  [score relfreqs interesting rfos-or-file]) 

(def score first)
(def relfreqs second)
(def interesting #(nth % 2))
(def rfos-or-file  #(nth % 3))
(defn rfo= [rfo1 rfo2]
  (= (rfos-or-file rfo1) (rfos-or-file rfo2)))
  
;(def *standard-relfreq* (words->relative-freq (concat *omni-doc* *large-standard-doc*))) ;repeated work, could opt this  HA - didn't turn out to be repeated anyway.
(def *corpus-relfreqs* (words->relative-freq *omni-doc*)) 


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
				  (combine-relfreqs (relative-freqs (first r-o-f)) 
						    (relative-freqs (second r-o-f))))))))

(defn interesting-words [relfreqs omni-relfreq count]
  
	(take count (sort #(> (abs (second %)) (abs (second %2)))
			  (map (fn [[word freq]]
				 [word (-  (or (get relfreqs word) 0) freq)])
			       omni-relfreq))))

;in the new pairings we create here, don't calculate interesting words - only the winning pair will have that done.
(def score-pair (memoize (fn [word-list [rfo1 rfo2]]
			   (make-rfo {:score (euclid (relfreqs rfo1) (relfreqs rfo2) word-list) 
				      :rfos-or-file [(make-rfo {:score (score rfo1) :interesting (interesting rfo1) :rfos-or-file (rfos-or-file rfo1)}) ;making a mock rfo here preserving the values of rfo1. lacks: relfreqs
						     (make-rfo {:score (score rfo2) :interesting (interesting rfo2) :rfos-or-file (rfos-or-file rfo2)})]})))) 

(defn best-pairing [rfos word-list omni-relfreq]
  (let [combos (sort (fn [rfo1 rfo2] (compare (score rfo1) (score rfo2))) ; rfo1 and rfo2 are mock rfos, lacking relfreqs. each represents a candidate pair - only the best scoring one will be made into a full rfo.
		     (map (partial score-pair word-list) 
			  (combinations rfos 2)))
	best-pair (first combos)
	relfreqs (relative-freqs best-pair)] 
    ;(println relfreqs)
    (make-rfo {:score (score best-pair) :relfreqs relfreqs :interesting (interesting-words relfreqs omni-relfreq *interesting-words-count*) :rfos-or-file (rfos-or-file best-pair)})))


;this is the recursive thing that... pretty much is the master function. 
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
			(make-rfo {:relfreqs relfreqs :interesting (interesting-words relfreqs *corpus-relfreqs* *interesting-words-count*) :rfos-or-file file}))) 
		    *txt-files*))
;here's how i'm calling this right now:
;(.replace (node (first (cluster *docs-rfos* *corpus-word-list* *standard-relfreq*))) *directory-string* "")
;(into-js-file (*1)
;(map (fn [rfo] (filter (complement map?) rfo)) (cluster *docs-rfos* *corpus-word-list* *corpus-relfreq*))

;(def bazz-4 (cluster *docs-rfos* *corpus-word-list* *corpus-relfreqs*))
;(.replace (node (first bazz-4)) *directory-string* "")


;how i got those sonnets from their crappy format off that web page to where each is in it's own file:
;(map (fn [[filename text]] (spit (str "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/sonnets/" filename) text)) (partition 2 (filter (complement empty?) (map #(.trim %) (re-seq #"(?s).+?\n\s*?\n" (slurp "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/sonnets.txt"))))))
