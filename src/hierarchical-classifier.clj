;Ideas for doc comparisions:
;counts hash
;counts hash and TWC  YES NO!
;porportions 
;summation of all inverses of the Euclidian distances
[["north-america" [:usa :miami]]] 
 
; somewhere in grep-able codespace i need to keep track of the idea that (file? o) is just (instance? java.io.File o).  This is good Java interop juju.

(def DOC-COUNT 8)
(def DOC-OFFSET 0) 
(def INTERESTING-FEATURES-COUNT 3) 

;(def *directory-string* "/Users/herdrick/Dropbox/blog/to-classify")
;(def *all-txt-files* (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File directory-string) 
;							       (into-array ["txt" "html"]) true)))
(def *directory-string* "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/sonnets/")
(def *all-txt-files* (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File *directory-string*) nil true)))

(def *txt-files* (take DOC-COUNT (drop DOC-OFFSET *all-txt-files*)))

(def file->seq (memoize (fn [file]
			  (re-seq #"[a-z]+" 
				  (org.apache.commons.lang.StringUtils/lowerCase (slurp (.toString file)))))))
 
(def *omni-doc* (set (apply concat (map file->seq *txt-files*)))) 
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

;(def *standard-relfreq* (words->relative-freq (concat *omni-doc* *large-standard-doc*))) ;repeated work, could opt this  HA - didn't turn out to be repeated anyway.
(def *corpus-relfreqs* (words->relative-freq *omni-doc*)) 

;(use '(incanter core stats)) ;need this only for abs and mean 
(ns user (:require incanter.core))

;returns the euclidean distance between two documents
(defn euclid [relfreqs1 relfreqs2 word-list]
  (incanter.core/sqrt (reduce + (map (fn [word]
			 (incanter.core/sq (incanter.core/abs (- (get relfreqs1 word 0) (get relfreqs2 word 0)))))
		      word-list))))

(ns user (:require incanter.stats))
(defn combine-relfreqs [rf1 rf2]
  (merge-with #(incanter.stats/mean [% %2]) rf1 rf2))  ; i'm just combining relfreqs taking their (unweighted) mean.  

(def relative-freqs (memoize (fn [rfo]
			      (let [r-o-f (rfos-or-file rfo)] 
				(if (instance? java.io.File r-o-f)
				  (words->relative-freq (file->seq r-o-f))
				  (combine-relfreqs (relative-freqs (first r-o-f)) 
						    (relative-freqs (second r-o-f))))))))

(defn interesting-features [relfreqs omni-relfreq count]
  (map (fn [[word freq]]
  	 (str word " "(.substring (str freq) 0 6) ", ")) ;display first 6 chars of floating point number
       (take count (sort #(> (incanter.core/abs (second %)) (incanter.core/abs (second %2)))
			 (map (fn [[word freq]]
				[word (- freq (get omni-relfreq word))])
			      relfreqs)))))

;(use 'clojure.contrib.combinatorics) ;sadly, combinations don't produce lazy seqs 
(ns user (:require clojure.contrib.combinatorics))

;in the new pairings we create here, don't calculate interesting features - only the winning pair will have that done.
(defn score-pair [word-list [rfo1 rfo2]]
  (make-rfo {:score (euclid (relfreqs rfo1) (relfreqs rfo2) word-list) 
	     :rfos-or-file [(make-rfo {:score (score rfo1) :interesting (interesting rfo1) :rfos-or-file (rfos-or-file rfo1)}) ;making a mock rfo here preserving the values of rfo1.  lacks: relfreqs
			    (make-rfo {:score (score rfo2) :interesting (interesting rfo2) :rfos-or-file (rfos-or-file rfo2)})]})) 

(defn best-pairing [rfos word-list omni-relfreq]
  (let [combos (sort (fn [rfo1 rfo2] (compare (score rfo1) (score rfo2))) ; rfo1 and rfo2 are mock rfos, lacking relfreqs.  each represents a candidate pair - only the best scoring one will be made into a full rfo.
		     (map (partial score-pair word-list) 
			  (clojure.contrib.combinatorics/combinations rfos 2)))
	best-pair (first combos)
	relfreqs (relative-freqs best-pair)] 
    (make-rfo {:score (score best-pair) :relfreqs relfreqs :interesting (interesting-features relfreqs omni-relfreq INTERESTING-FEATURES-COUNT) :rfos-or-file (rfos-or-file best-pair)})))

(defn =rfos-ignore-relfreqs [rfo1 rfo2]
  (and (= (score rfo1) (score rfo2)) 
       (= (rfos-or-file rfo1) (rfos-or-file rfo2))))
	    
;(use 'clojure.walk)
(ns user (:require clojure.walk))
;this is the recursive thing that... pretty much is the master function. 
(defn cluster [rfos word-list omni-relfreq]
  (if (= (count rfos) 1) 
    (clojure.walk/postwalk-replace {(second (first rfos)) nil} rfos) ; this postwalk-replace (tree replace) is to axe the final matchup's relfreqs for readability
    (let [best-pairing-rfo (best-pairing rfos word-list omni-relfreq)
	  rfos-cleaned (filter (complement (fn [rfo]
					     (or (=rfos-ignore-relfreqs rfo (first (rfos-or-file best-pairing-rfo)))
						 (=rfos-ignore-relfreqs rfo (second (rfos-or-file best-pairing-rfo))))))
			       rfos)]
      (cluster (conj rfos-cleaned best-pairing-rfo) word-list omni-relfreq))))

;here's how i'm calling this right now:
;(.replace (node (first (cluster *docs-rfos* *corpus-word-list* *standard-relfreq*))) *directory-string* "")
;(into-js-file (*1)

(def *docs-rfos* (map (fn [file]
		      (let [relfreqs (words->relative-freq (file->seq file))]
			(make-rfo {:relfreqs relfreqs :interesting (interesting-features relfreqs *corpus-relfreqs* INTERESTING-FEATURES-COUNT) :rfos-or-file file}))) 
		    *txt-files*))

(def *infovis-js-file* "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/visualize/Spacetree/example1.js")

(ns user (:require clojure.contrib.duck-streams))

(defn into-js-file [o]
  (clojure.contrib.duck-streams/spit *infovis-js-file* 
				     (.replaceFirst (slurp *infovis-js-file*) 
							       "(?s)var json =.*;//end json data"    ;note regex flag to ignore line terminators. needed on some platforms but not all. Java is WODE!
							       (str "var json =" o ";//end json data"))))

;how i got those sonnets from their crappy format off that web page to where each is in it's own file:
;(map (fn [[filename text]] (spit (str "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/sonnets/" filename) text)) (partition 2 (filter (complement empty?) (map #(.trim %) (re-seq #"(?s).+?\n\s*?\n" (slurp "/Users/herdrick/Dropbox/clojure/hierarchical-classifier/data/sonnets.txt"))))))
